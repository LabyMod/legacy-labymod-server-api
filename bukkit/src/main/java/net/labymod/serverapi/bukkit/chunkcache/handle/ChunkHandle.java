package net.labymod.serverapi.bukkit.chunkcache.handle;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.labymod.serverapi.bukkit.chunkcache.ChunkCachingInstance;
import net.labymod.serverapi.bukkit.chunkcache.PlayerState;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;

public interface ChunkHandle {

    boolean handle( Player player, PacketType type, PlayerState playerState, PacketContainer packet );

    static int hashSingle( byte[] data ) {
        return ChunkCachingInstance.hashFactory.hash32().hash( data, 0, data.length, -42421337 );
    }

    static int hashBuf( ByteBuffer buf ) {
        return ChunkCachingInstance.hashFactory.hash32().hash( buf, -42421337 );
    }
}
