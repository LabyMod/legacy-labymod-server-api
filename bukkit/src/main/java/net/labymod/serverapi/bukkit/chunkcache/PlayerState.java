package net.labymod.serverapi.bukkit.chunkcache;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import io.netty.channel.Channel;
import net.labymod.serverapi.bukkit.LabyModPlugin;
import net.labymod.serverapi.bukkit.chunkcache.cache.Chunk8Cache;
import net.labymod.serverapi.bukkit.chunkcache.cache.ChunkCache;
import net.labymod.serverapi.bukkit.chunkcache.handle.ChunkPos;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerState {
    private final Set<ChunkPos> allowed = new HashSet<>();
    private final Map<ChunkPos, ChunkCache> statesByCoord = new ConcurrentHashMap<>();
    private final ListMultimap<Integer, ChunkCache> statesByHash = MultimapBuilder.hashKeys().linkedListValues().build();

    /**
     * This will check whether chunks need to be sent instantly or not (will be cached)
     */
    public List<Integer> handleSending( ChunkCache[] caches ) {
        List<Integer> send = new LinkedList<>();

        for ( int i = 0; i < caches.length; i++ ) {
            ChunkCache cache = caches[i];
            if ( allowed.remove( cache.getChunkPos() ) ) {
                send.add( i ); // Send the chunk directly
                ChunkCachingInstance.log( "2. Allow sending of %d/%d\n", cache.getChunkPos().getX(), cache.getChunkPos().getZ() );
            } else { // Else cache it for later use
                statesByCoord.put( cache.getChunkPos(), cache );
                statesByHash.put( cache.getHash(), cache );
                ChunkCachingInstance.log( "1. Adding %d/%d to cache\n", cache.getChunkPos().getX(), cache.getChunkPos().getZ() );
            }
        }

        return send;
    }

    public boolean handleSignSending( PacketContainer packet ) {
        BlockPosition blockPosition = packet.getBlockPositionModifier().read( 1 );
        int chunkX = blockPosition.getX() >> 4;
        int chunkZ = blockPosition.getZ() >> 4;
        ChunkPos chunkPos = new ChunkPos( chunkX, chunkZ );
        ChunkCache cache = statesByCoord.get( chunkPos );
        if ( cache instanceof Chunk8Cache ) {
            ((Chunk8Cache) cache).getSignUpdates().add( packet.getHandle() );
        }
        return cache != null;
    }

    /**
     * Will handle incoming responses from the client, where mask specifies whether
     * the client has the packet cached or not. If true, the corresponding chunk with
     * the hash specified in hashes will be sent to the client. Else, it will be discarded.
     */
    public void handleRequest( ProtocolManager proto, Player player, boolean[] mask, int[] hashes ) {
        int need = 0;
        ByteBuffer buffer = null;
        Multimap<Class<? extends ChunkCache>, ChunkCache> targets = LinkedListMultimap.create();
        for ( int i = 0; i < mask.length; i++ ) {
            int hash = hashes[i];
            List<ChunkCache> caches = statesByHash.removeAll( hash );
            if ( caches == null || caches.isEmpty() ) {
                continue;
            }
            boolean first = true;
            if (caches.size() > 1) {
                buffer = ByteBuffer.allocate( 4 + (caches.size() - 1) * 12 ); // Byte Byte Short (3 * Int per Chunk)
                buffer.put( (byte) 1 ); // BulkChunk
                buffer.put( (byte) (1) );
                buffer.putShort( (short) (caches.size() - 1) );
            }

            // Flush all chunks with given hash and send them!
            for ( ChunkCache cache : caches ) {
                statesByCoord.remove( cache.getChunkPos() );
                if ( mask[i] ) {
                    flushSigns( player, cache );
                    ChunkCachingInstance.log( "3. Player has chunk %d/%d already\n", cache.getChunkPos().getX(), cache.getChunkPos().getZ() );
                    continue; // We do not need to send this chunk to the player, yay! Just saved some traffic
                }
                if ( first ) {
                    need++;
                    allowed.add( cache.getChunkPos() );

                    targets.put( cache.getClass(), cache );
                    first = false;
                } else if (buffer != null) {
                    buffer.putInt( cache.getHash() );
                    buffer.putInt( cache.getX() );
                    buffer.putInt( cache.getZ() );
                }
                ChunkCachingInstance.log( "3. Player needs chunk %d/%d\n", cache.getChunkPos().getX(), cache.getChunkPos().getZ() );
            }
        }
        ChunkCachingInstance.log( "Player %s is in need of %d of %d chunks", player.getName(), need, mask.length );

        for ( Map.Entry<Class<? extends ChunkCache>, Collection<ChunkCache>> entry : targets.asMap().entrySet() ) {
            Collection<ChunkCache> v = entry.getValue();
            if ( v.size() == 0 ) {
                continue;
            }
            v.forEach( cache -> {
                try {
                    cache.sendTo( proto, player, v );
                } catch ( InvocationTargetException e ) {
                    ChunkCachingInstance.log( "Failed to execute ChunkSend to " + entry.getKey().getSimpleName() );
                    e.printStackTrace();
                }
                flushSigns( player, cache );
            } );
        }
        if ( buffer != null) {
            LabyModPlugin.getInstance().getPacketUtils().sendPluginMessage( player, ChunkCachingInstance.PM_CHANNEL, buffer.array() );
        }

    }

    private void flushSigns( Player player, ChunkCache cache ) {
        if ( cache instanceof Chunk8Cache ) { // Send missing sign updates!
            Channel channel = LabyModPlugin.getInstance().getPacketUtils().getChannel( player );
            for ( Object signUpdate : ((Chunk8Cache) cache).getSignUpdates() ) {
                channel.write( signUpdate );
            }
            channel.flush();
        }
    }

    public void clear() {
        statesByCoord.clear();
        statesByHash.clear();
        allowed.clear();
    }

    public void clearOlder( long millis ) {
        Iterator<Map.Entry<Integer, ChunkCache>> iterator = statesByHash.entries().iterator();
        while ( iterator.hasNext() ) {
            ChunkCache cache = iterator.next().getValue();
            if ( cache.getStoredAt() < millis ) {
                iterator.remove();
                statesByCoord.remove( cache.getChunkPos() );
            }
        }
    }
}
