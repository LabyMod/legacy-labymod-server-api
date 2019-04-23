package net.labymod.server.spigot.listener;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.labymod.server.common.buffer.ByteBuffers;
import net.labymod.server.spigot.event.LabyPlayerJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Collections;

public class LabyModPluginMessageListener implements PluginMessageListener {

    private static final ByteBuffers BYTE_BUFFERS = ByteBuffers.standard();

    @Override
    public void onPluginMessageReceived( String channel, Player player, byte[] bytes ) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer( bytes );

        String version = BYTE_BUFFERS.readString( byteBuf );

        if ( !player.isOnline() ) {
            return;
        }

        Bukkit.getPluginManager().callEvent( new LabyPlayerJoinEvent( player, Collections.emptyList(),
                version,
                0,
                false ) );
    }
}
