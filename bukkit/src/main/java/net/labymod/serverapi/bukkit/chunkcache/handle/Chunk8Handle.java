package net.labymod.serverapi.bukkit.chunkcache.handle;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.labymod.serverapi.bukkit.LabyModPlugin;
import net.labymod.serverapi.bukkit.chunkcache.ChunkCachingInstance;
import net.labymod.serverapi.bukkit.chunkcache.PlayerState;
import net.labymod.serverapi.bukkit.chunkcache.cache.Chunk8BulkCache;
import net.labymod.serverapi.bukkit.chunkcache.cache.Chunk8Cache;
import net.labymod.serverapi.bukkit.chunkcache.cache.Chunk8SingleCache;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.util.List;

public class Chunk8Handle implements ChunkHandle {

    @SuppressWarnings("deprecation")
    @Override
    public boolean handle( Player player, PacketType type, PlayerState playerState, PacketContainer pckt ) {

        if ( type == PacketType.Play.Server.MAP_CHUNK ) { // Just a single chunk (easy)
            Object chunkMap = pckt.getModifier().read( 2 );
            boolean groundUpContinuous = pckt.getBooleans().read( 0 );
            if ( !groundUpContinuous || LabyModPlugin.getInstance().getPacketUtils().chunkMapB( chunkMap ) != 0 ) { // We need to let empty chunks through every time! (They unload chunks)
                Chunk8Cache[] caches = new Chunk8Cache[]{new Chunk8SingleCache( ChunkHandle.hashSingle( LabyModPlugin.getInstance().getPacketUtils().chunkMapA( chunkMap ) ), pckt.getIntegers().read( 0 ), pckt.getIntegers().read( 1 ), chunkMap, groundUpContinuous )};

                List<Integer> maps = playerState.handleSending( caches );
                if ( maps == null || maps.isEmpty() ) {
                    ByteBuffer buffer = ByteBuffer.allocate( 16 ); // ByteByteShortIntIntInt
                    buffer.put( (byte) 0 ); // SingleChunk
                    buffer.put( (byte) (groundUpContinuous ? 1 : 0) );
                    buffer.putShort( (short) 1 );
                    buffer.putInt( caches[0].getHash() );
                    buffer.putInt( caches[0].getX() );
                    buffer.putInt( caches[0].getZ() );
                    LabyModPlugin.getInstance().getPacketUtils().sendPluginMessage( player, ChunkCachingInstance.PM_CHANNEL, buffer.array() );
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        } else if ( type == PacketType.Play.Server.MAP_CHUNK_BULK ) { // A lot of chunks (difficult)

            // Int Arrays describing the positions of the chunks
            int[] cX = pckt.getIntegerArrays().read( 0 );
            int[] cZ = pckt.getIntegerArrays().read( 1 );

            // Actual chunk data (just like in MAP_CHUNK packets)
            Object[] chunkMaps = (Object[]) pckt.getModifier().read( 2 );
            if ( cX.length != cZ.length || cZ.length != chunkMaps.length ) {
                throw new RuntimeException( "Uhoh, how did I end up with different array sizes? " + cX.length + ", " + cZ.length + ", " + chunkMaps.length ); // With a normal MC server, this does not happen at all
            }

            boolean isSkylight = pckt.getBooleans().read( 0 );
            Chunk8Cache[] caches = new Chunk8Cache[chunkMaps.length];

            for ( int i = 0; i < caches.length; i++ ) {
                caches[i] = new Chunk8BulkCache( ChunkHandle.hashSingle( LabyModPlugin.getInstance().getPacketUtils().chunkMapA( chunkMaps[i] ) ), cX[i], cZ[i], chunkMaps[i], isSkylight );
            }

            List<Integer> maps = playerState.handleSending( caches );

            if ( maps.size() == chunkMaps.length ) {
                // We can just send the packet (the client requires every chunk)
                return false; // With "false" meaning "no cancel"
            }

            ByteBuffer buffer = ByteBuffer.allocate( 4 + ((caches.length - maps.size()) * 12) ); // Byte Byte Short (3 * Int per Chunk)
            buffer.put( (byte) 1 ); // BulkChunk
            buffer.put( (byte) (isSkylight ? 1 : 0) );
            buffer.putShort( (short) (caches.length - maps.size()) );

            if ( maps.isEmpty() ) {
                for ( Chunk8Cache cache : caches ) {
                    buffer.putInt( cache.getHash() );
                    buffer.putInt( cache.getX() );
                    buffer.putInt( cache.getZ() );
                }
                ChunkCachingInstance.log( "Requesting %d (all) Chunks to player %s", caches.length - maps.size(), player.getName() );
                LabyModPlugin.getInstance().getPacketUtils().sendPluginMessage( player, ChunkCachingInstance.PM_CHANNEL, buffer.array() );
                return true; // Cancel sending of BulkPacket
            } else {
                // This will prepare the new array of chunks to send

                int ncX[] = new int[maps.size()];
                int ncZ[] = new int[maps.size()];
                Object output[] = new Object[maps.size()];

                int i = 0;
                for ( Integer pos : maps ) {
                    ncX[i] = cX[pos];
                    ncZ[i] = cZ[pos];
                    output[i] = chunkMaps[pos];
                    caches[pos] = null;

                    i++;
                }
                for ( int l = 0; l < caches.length; l++ ) {
                    if ( caches[i] == null ) {
                        continue;
                    }

                    buffer.putInt( caches[l].getHash() );
                    buffer.putInt( cX[l] );
                    buffer.putInt( cZ[l] );
                }

                // Apply changes
                pckt.getIntegerArrays().write( 0, ncX );
                pckt.getIntegerArrays().write( 1, ncZ );
                pckt.getModifier().write( 2, output );
                LabyModPlugin.getInstance().getPacketUtils().sendPluginMessage( player, ChunkCachingInstance.PM_CHANNEL, buffer.array() );
                ChunkCachingInstance.log( "Requesting %d Chunks to player %s", caches.length - maps.size(), player.getName() );
                return false; // Send modified bulkchunk packet
            }
        } else {
            return false;
        }
    }
}
