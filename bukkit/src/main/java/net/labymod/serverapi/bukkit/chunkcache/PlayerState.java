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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerState {
    private final Set<Integer> allowed = new HashSet<>();
    private final ListMultimap<Integer, ChunkCache> states = MultimapBuilder.hashKeys().linkedListValues().build();
    private final Map<ChunkPos, List<Object>> activeCoords = new ConcurrentHashMap<>();

    /**
     * This will check whether chunks need to be sent instantly or not (will be cached)
     */
    public List<Integer> handleSending( ChunkCache[] caches ) {
        List<Integer> send = new LinkedList<>();

        for ( int i = 0; i < caches.length; i++ ) {
            ChunkCache cache = caches[i];
            if ( allowed.remove( cache.getHash() ) ) {
                send.add( i ); // Send the chunk directly
            } else { // Else cache it for later use
                states.put( cache.getHash(), cache );
                if (cache instanceof Chunk8Cache) {
                    activeCoords.put( cache.getChunkPos(), ((Chunk8Cache) cache).getSignUpdates() );
                }
            }
        }

        return send;
    }

    public boolean handleSignSending( PacketContainer packet ) {
        BlockPosition blockPosition = packet.getBlockPositionModifier().read( 1 );
        int chunkX = blockPosition.getX() >> 4;
        int chunkZ = blockPosition.getZ() >> 4;
        ChunkPos chunkPos = new ChunkPos( chunkX, chunkZ );
        List<Object> list = activeCoords.get( chunkPos );
        if (list != null) {
            list.add( packet.getHandle() );
        }
        return list != null;
    }

    /**
     * Will handle incoming responses from the client, where mask specifies whether
     * the client has the packet cached or not. If true, the corresponding chunk with
     * the hash specified in hashes will be sent to the client. Else, it will be discarded.
     */
    public void handleRequest( ProtocolManager proto, Player player, boolean[] mask, int[] hashes ) {
        int need = 0;
        Multimap<Class<? extends ChunkCache>, ChunkCache> targets = LinkedListMultimap.create();
        for ( int i = 0; i < mask.length; i++ ) {
            int hash = hashes[i];
            List<ChunkCache> caches = states.get( hash );
            if ( caches == null || caches.isEmpty() ) {
                continue;
            }
            ChunkCache cache = caches.remove( 0 );

            if ( cache == null ) {
                continue;
            }
            activeCoords.remove( cache.getChunkPos() );
            if ( mask[i] ) {
                flushSigns( player, cache );
                continue; // We do not need to send this chunk to the player, yay! Just saved some traffic
            }
            need++;
            allowed.add( cache.getHash() );

            targets.put( cache.getClass(), cache );
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
        states.clear();
    }

    public void clearOlder( long millis ) {
        Iterator<Map.Entry<Integer, ChunkCache>> iterator = states.entries().iterator();
        while ( iterator.hasNext() ) {
            ChunkCache cache = iterator.next().getValue();
            if ( cache.getStoredAt() < millis ) {
                iterator.remove();
            }
        }
    }
}
