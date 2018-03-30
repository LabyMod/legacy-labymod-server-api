package net.labymod.serverapi.bukkit.chunkcache.cache;

import com.comphenix.protocol.ProtocolManager;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public abstract class Chunk8Cache extends ChunkCache {
    private final Object map;

    Chunk8Cache( int hash, int x, int z, Object map ) {
        super( hash, x, z );
        this.map = map;
    }


    Object getMap() {
        return map;
    }

    @Override
    public abstract void sendTo( ProtocolManager proto, Player player, Collection<ChunkCache> chunks ) throws InvocationTargetException;
}
