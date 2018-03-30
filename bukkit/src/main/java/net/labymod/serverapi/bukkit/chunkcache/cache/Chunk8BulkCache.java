package net.labymod.serverapi.bukkit.chunkcache.cache;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import net.labymod.serverapi.bukkit.LabyModPlugin;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class Chunk8BulkCache extends Chunk8Cache {

    private final boolean skylight;

    public Chunk8BulkCache( int hash, int x, int z, Object map, boolean skylight ) {
        super( hash, x, z, map );
        this.skylight = skylight;
    }

    private boolean isSkylight() {
        return skylight;
    }

    @Override
    public void sendTo( ProtocolManager proto, Player player, Collection<ChunkCache> chunks ) throws InvocationTargetException {
        PacketContainer container = new PacketContainer( PacketType.Play.Server.MAP_CHUNK_BULK );
        container.getModifier().write( 4, LabyModPlugin.getInstance().getPacketUtils().getWorldHandle( player.getWorld() ) );
        int[] cX = new int[chunks.size()];
        int[] cZ = new int[chunks.size()];
        Object[] maps = (Object[]) Array.newInstance(LabyModPlugin.getInstance().getPacketUtils().getChunkMapClass(), chunks.size());
        int i = 0;
        for ( ChunkCache chunk : chunks ) {
            cX[i] = chunk.getX();
            cZ[i] = chunk.getZ();
            maps[i] = ((Chunk8Cache) chunk).getMap();
            i++;
        }
        container.getIntegerArrays().write( 0, cX );
        container.getIntegerArrays().write( 1, cZ );
        container.getModifier().write( 2, maps );
        container.getBooleans().write( 0, isSkylight() );
        proto.sendServerPacket( player, container );
    }
}
