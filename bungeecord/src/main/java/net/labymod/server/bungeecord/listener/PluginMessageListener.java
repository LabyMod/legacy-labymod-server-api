package net.labymod.server.bungeecord.listener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.labymod.server.bungeecord.event.LabyPlayerJoinEvent;
import net.labymod.server.bungeecord.event.MessageReceiveEvent;
import net.labymod.server.common.addon.AddonCollector;
import net.labymod.server.common.buffer.ByteBuffers;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Collections;

/**
 * The {@link PluginMessageEvent} implements {@link Listener} there are triggered when an message incoming.
 *
 * @author Manuel Kollus
 * @version 2.0
 * @since 2.0
 */
public class PluginMessageListener implements Listener {

    private static final AddonCollector ADDON_COLLECTOR = AddonCollector.standard();
    private static final ByteBuffers BYTE_BUFFERS = ByteBuffers.standard();
    private static final JsonParser JSON_PARSER = new JsonParser();

    @EventHandler
    public void onPluginMessage( PluginMessageEvent event ) {
        if ( !( event.getSender() instanceof ProxiedPlayer ) ) {
            return;
        }

        ProxiedPlayer proxiedPlayer = (ProxiedPlayer) event.getSender();

        if ( event.getTag().equalsIgnoreCase( "LABYMOD" ) ) {
            this.requestPlayer( proxiedPlayer, Unpooled.wrappedBuffer( event.getData() ) );
        }

        if ( event.getTag().equalsIgnoreCase( "LMC" ) ) {
            this.receiveMessage( proxiedPlayer, Unpooled.wrappedBuffer( event.getData() ) );
        }
    }

    private void requestPlayer( @NonNull ProxiedPlayer proxiedPlayer, @NonNull ByteBuf byteBuf ) {
        String version = BYTE_BUFFERS.readString( byteBuf );
        ProxyServer.getInstance().getPluginManager().callEvent( new LabyPlayerJoinEvent( proxiedPlayer,
                Collections.emptyList(), version, 0, false ) );
    }

    private void receiveMessage( @NonNull ProxiedPlayer proxiedPlayer, @NonNull ByteBuf byteBuf ) {
        String messageKey = BYTE_BUFFERS.readString( byteBuf );
        String messageContent = BYTE_BUFFERS.readString( byteBuf );

        JsonElement jsonElement = JSON_PARSER.parse( messageContent );

        if ( !messageKey.equals( "INFO" ) && !jsonElement.isJsonObject() ) {
            ProxyServer.getInstance().getPluginManager().callEvent(
                    new MessageReceiveEvent( proxiedPlayer, messageKey, jsonElement ) );
            return;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        boolean chunkCachingEnabled = false;
        int chunkCachingVersion = 0;

        if ( jsonObject.has( "ccp" ) && jsonObject.get( "ccp" ).isJsonObject() ) {
            JsonObject chunkCachingObject = jsonObject.get( "ccp" ).getAsJsonObject();

            if ( chunkCachingObject.has( "enabled" ) )
                chunkCachingEnabled = chunkCachingObject.get( "enabled" ).getAsBoolean();

            if ( chunkCachingObject.has( "version" ) )
                chunkCachingVersion = chunkCachingObject.get( "version" ).getAsInt();
        }

        ProxyServer.getInstance().getPluginManager().callEvent( new LabyPlayerJoinEvent( proxiedPlayer,
                ADDON_COLLECTOR.collectAddons( jsonObject ),
                jsonObject.get( "version" ).getAsString(),
                chunkCachingVersion,
                chunkCachingEnabled ) );
    }
}
