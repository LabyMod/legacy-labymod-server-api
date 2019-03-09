package net.labymod.serverapi.bungee.listener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.labymod.serverapi.Addon;
import net.labymod.serverapi.bungee.LabyModPlugin;
import net.labymod.serverapi.bungee.event.LabyModPlayerJoinEvent;
import net.labymod.serverapi.bungee.event.MessageReceiveEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;

/**
 * Class created by qlow | Jan
 */
public class PluginMessageListener implements Listener {

    private static final JsonParser JSON_PARSER = new JsonParser();

    @EventHandler
    public void onPluginMessage( PluginMessageEvent event ) {
        if ( !( event.getSender() instanceof ProxiedPlayer ) )
            return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        // The LABYMOD plugin channel is higly deprecated and shouldn't be used - we just listen to it to retrieve old labymod clients.
        if ( event.getTag().equals( "LABYMOD" ) ) {
            // Converting the byte array into a byte buffer
            ByteBuf buf = Unpooled.wrappedBuffer( event.getData() );
            String version = LabyModPlugin.getInstance().getApi().readString( buf, Short.MAX_VALUE );
            ProxyServer.getInstance().getPluginManager().callEvent( new LabyModPlayerJoinEvent( player, version, false, 0, new ArrayList<Addon>() ) );
        }

        if ( event.getTag().equals( "LMC" ) ) {
            // Converting the byte array into a byte buffer
            ByteBuf buf = Unpooled.wrappedBuffer( event.getData() );

            String messageKey = LabyModPlugin.getInstance().getApi().readString( buf, Short.MAX_VALUE );
            String messageContents = LabyModPlugin.getInstance().getApi().readString( buf, Short.MAX_VALUE );
            JsonElement jsonMessage = JSON_PARSER.parse( messageContents );

            // Listening to the INFO (join) message
            if ( messageKey.equals( "INFO" ) && jsonMessage.isJsonObject() ) {
                JsonObject jsonObject = jsonMessage.getAsJsonObject();
                String version = jsonObject.has( "version" )
                        && jsonObject.get( "version" ).isJsonPrimitive()
                        && jsonObject.get( "version" ).getAsJsonPrimitive().isString() ? jsonObject.get( "version" ).getAsString() : "Unknown";

                boolean chunkCachingEnabled = false;
                int chunkCachingVersion = 0;

                if ( jsonObject.has( "ccp" ) && jsonObject.get( "ccp" ).isJsonObject() ) {
                    JsonObject chunkCachingObject = jsonObject.get( "ccp" ).getAsJsonObject();

                    if ( chunkCachingObject.has( "enabled" ) )
                        chunkCachingEnabled = chunkCachingObject.get( "enabled" ).getAsBoolean();

                    if ( chunkCachingObject.has( "version" ) )
                        chunkCachingVersion = chunkCachingObject.get( "version" ).getAsInt();
                }

                ProxyServer.getInstance().getPluginManager().callEvent( new LabyModPlayerJoinEvent( player, version,
                        chunkCachingEnabled, chunkCachingVersion, Addon.getAddons( jsonObject ) ) );
                return;
            }

            // Calling the LabyModPlayerJoinEvent
            ProxyServer.getInstance().getPluginManager().callEvent( new MessageReceiveEvent( player, messageKey, jsonMessage ) );
        }
    }
}
