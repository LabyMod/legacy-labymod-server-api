package net.labymod.serverapi.bukkit.chunkcache;

import com.comphenix.protocol.ProtocolManager;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import net.labymod.serverapi.bukkit.chunkcache.cache.ChunkCache;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class PlayerState {
    private final Set<Integer> allowed = new HashSet<>();
    private final Map<Integer, ChunkCache> states = new HashMap<>();

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
            }
        }

        return send;
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
            ChunkCache cache = states.remove( hash );
            if ( cache == null || mask[i] ) {
                continue; // We do not need to send this to the player, yay! Just saved some traffic
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
            try {
                v.iterator().next().sendTo( proto, player, v );
            } catch ( InvocationTargetException e ) {
                ChunkCachingInstance.log( "Failed to execute ChunkSend to " + entry.getKey().getSimpleName() );
                e.printStackTrace();
            }
        }
    }

    public void clear() {
        states.clear();
    }
}
