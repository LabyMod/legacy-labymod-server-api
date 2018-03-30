package net.labymod.serverapi.bukkit.chunkcache.cache;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class Chunk8SingleCache extends Chunk8Cache {
    private final boolean groundUpContinuous;

    public Chunk8SingleCache( int hash, int x, int z, Object map, boolean groundUpContinuous ) {
        super( hash, x, z, map );
        this.groundUpContinuous = groundUpContinuous;
    }

    private boolean isGroundUpContinuous() {
        return groundUpContinuous;
    }

    @Override
    public void sendTo( ProtocolManager proto, Player player, Collection<ChunkCache> chunks ) throws InvocationTargetException {
        for ( ChunkCache chunk : chunks ) {
            Chunk8SingleCache ch8 = (Chunk8SingleCache) chunk;

            // sky0 packets always came from a single SEND_CHUNK packet
            PacketContainer container = new PacketContainer( PacketType.Play.Server.MAP_CHUNK );
            container.getIntegers().write( 0, ch8.getX() );
            container.getIntegers().write( 1, ch8.getZ() );
            container.getModifier().write( 2, ch8.getMap() );
            container.getBooleans().write( 0, ch8.isGroundUpContinuous() );
            proto.sendServerPacket( player, container );
        }
    }
}
