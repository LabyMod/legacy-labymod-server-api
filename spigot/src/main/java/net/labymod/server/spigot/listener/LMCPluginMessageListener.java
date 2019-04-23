package net.labymod.server.spigot.listener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.labymod.server.common.addon.AddonCollector;
import net.labymod.server.common.buffer.ByteBuffers;
import net.labymod.server.spigot.event.LabyPlayerJoinEvent;
import net.labymod.server.spigot.event.MessageReceiveEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class LMCPluginMessageListener implements PluginMessageListener {

    private static final AddonCollector ADDON_COLLECTOR = AddonCollector.standard();
    private static final ByteBuffers BYTE_BUFFERS = ByteBuffers.standard();
    private static final JsonParser JSON_PARSER = new JsonParser();

    @Override
    public void onPluginMessageReceived( String channel, Player player, byte[] bytes ) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer( bytes );

        if ( !player.isOnline() ) {
            return;
        }

        String messageKey = BYTE_BUFFERS.readString( byteBuf );
        String messageContent = BYTE_BUFFERS.readString( byteBuf );

        JsonElement jsonElement = JSON_PARSER.parse( messageContent );

        if ( !messageKey.equals( "INFO" ) && !jsonElement.isJsonObject() ) {
            Bukkit.getPluginManager().callEvent( new MessageReceiveEvent( player, messageKey, jsonElement ) );
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

        Bukkit.getPluginManager().callEvent( new LabyPlayerJoinEvent( player,
                ADDON_COLLECTOR.collectAddons( jsonObject ),
                jsonObject.get( "version" ).getAsString(),
                chunkCachingVersion,
                chunkCachingEnabled ) );
    }
}
