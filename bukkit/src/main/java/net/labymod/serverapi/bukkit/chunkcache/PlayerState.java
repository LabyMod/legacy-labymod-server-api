package net.labymod.serverapi.bukkit.chunkcache;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import net.labymod.serverapi.bukkit.chunkcache.cache.Chunk8Cache;
import net.labymod.serverapi.bukkit.chunkcache.cache.ChunkCache;
import net.labymod.serverapi.bukkit.chunkcache.handle.ChunkPos;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerState {
    private final Set<ChunkPos> allowed = new HashSet<>();
    private final Map<ChunkPos, ChunkCache> statesByCoord = new ConcurrentHashMap<>();

    /**
     * This will check whether chunks need to be sent instantly or not (will be cached)
     */
    public List<Integer> handleSending( ChunkCache[] caches ) {
        List<Integer> send = new LinkedList<>();

        for ( int i = 0; i < caches.length; i++ ) {
            ChunkCache cache = caches[i];
            if ( allowed.remove( cache.getChunkPos() ) ) {
                send.add( i ); // Send the chunk directly
                ChunkCachingInstance.debug( "2. Allow sending of %d/%d\n", cache.getChunkPos().getX(), cache.getChunkPos().getZ() );
            } else { // Else cache it for later use
                statesByCoord.put( cache.getChunkPos(), cache );
                ChunkCachingInstance.debug( "1. Adding %d/%d to cache\n", cache.getChunkPos().getX(), cache.getChunkPos().getZ() );
            }
        }

        return send;
    }

    public boolean handleSignSending( PacketContainer packet ) {
        BlockPosition blockPosition = packet.getBlockPositionModifier().read( 0 );
        int chunkX = blockPosition.getX() >> 4;
        int chunkZ = blockPosition.getZ() >> 4;
        ChunkPos chunkPos = new ChunkPos( chunkX, chunkZ );
        ChunkCache cache = statesByCoord.get( chunkPos );
        if ( cache instanceof Chunk8Cache ) {
            ((Chunk8Cache) cache).getSignUpdates().add( packet );
        }
        return cache != null;
    }

    /**
     * Will handle incoming responses from the client, where mask specifies whether
     * the client has the packet cached or not. If true, the corresponding chunk with
     * the hash specified in hashes will be sent to the client. Else, it will be discarded.
     */
    public void handleRequest( ProtocolManager proto, Player player, boolean[] mask, int[][] coords ) {
        int need = 0;
        Multimap<Class<? extends ChunkCache>, ChunkCache> targets = LinkedListMultimap.create();
        for ( int i = 0; i < mask.length; i++ ) {
            ChunkPos pos = new ChunkPos( coords[i][0], coords[i][1] );
            ChunkCache cache = statesByCoord.remove( pos );
            if ( cache == null ) {
                continue;
            }
            // Flush all chunks with given hash and send them!
            if ( mask[i] ) {
                flushSigns( proto, player, cache );
                ChunkCachingInstance.debug( "3. Player has chunk %d/%d already\n", cache.getChunkPos().getX(), cache.getChunkPos().getZ() );
                continue; // We do not need to send this chunk to the player, yay! Just saved some traffic
            }
            need++;
            allowed.add( cache.getChunkPos() );

            targets.put( cache.getClass(), cache );
            ChunkCachingInstance.debug( "3. Player needs chunk %d/%d\n", cache.getChunkPos().getX(), cache.getChunkPos().getZ() );
        }
        ChunkCachingInstance.debug( "Player %s is in need of %d of %d chunks", player.getName(), need, mask.length );

        if ( need == 0 ) {
            return;
        }

        // This groups by class -> creates one BulkChunkPacket instead of several others
        for ( Map.Entry<Class<? extends ChunkCache>, Collection<ChunkCache>> entry : targets.asMap().entrySet() ) {
            Collection<ChunkCache> v = entry.getValue();
            if ( v.isEmpty() ) {
                continue;
            }
            v.stream().findAny().ifPresent( ( cache ) -> {
                try {
                    cache.sendTo( proto, player, v );
                } catch ( InvocationTargetException e ) {
                    ChunkCachingInstance.debug( "Failed to execute ChunkSend to " + entry.getKey().getSimpleName() );
                    e.printStackTrace();
                }
            } );
            for ( ChunkCache cache : v ) {
                flushSigns( proto, player, cache );
            }
        }
    }

    private void flushSigns( ProtocolManager proto, Player player, ChunkCache cache ) {
        if ( cache instanceof Chunk8Cache ) { // Send missing sign updates!
            for ( Object signUpdate : ((Chunk8Cache) cache).getSignUpdates() ) {
                try {
                    proto.sendServerPacket( player, (PacketContainer) signUpdate );
                } catch ( InvocationTargetException e ) {
                    ChunkCachingInstance.debug( "Failed to execute SignSend to " + player.getDisplayName() );
                    e.printStackTrace();
                }
            }
        }
    }

    public void clear() {
        statesByCoord.clear();
        allowed.clear();
    }

    public void clearOlder( long millis ) {
        Iterator<Map.Entry<ChunkPos, ChunkCache>> iterator = statesByCoord.entrySet().iterator();
        while ( iterator.hasNext() ) {
            ChunkCache cache = iterator.next().getValue();
            if ( cache.getStoredAt() < millis ) {
                iterator.remove();
                statesByCoord.remove( cache.getChunkPos() );
            }
        }
    }
}
