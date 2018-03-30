package net.labymod.serverapi.bukkit.chunkcache.cache;

import com.comphenix.protocol.ProtocolManager;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public abstract class ChunkCache {

    private final long storedAt;

    private final int hash;
    private final int x;
    private final int z;

    ChunkCache( int hash, int x, int z ) {
        this.storedAt = System.currentTimeMillis();
        this.hash = hash;
        this.x = x;
        this.z = z;
    }

    public long getStoredAt() {
        return storedAt;
    }

    public int getHash() {
        return hash;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public abstract void sendTo( ProtocolManager proto, Player player, Collection<ChunkCache> chunks ) throws InvocationTargetException;
}
