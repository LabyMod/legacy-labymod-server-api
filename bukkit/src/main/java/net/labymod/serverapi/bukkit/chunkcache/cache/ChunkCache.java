package net.labymod.serverapi.bukkit.chunkcache.cache;

import com.comphenix.protocol.ProtocolManager;
import net.labymod.serverapi.bukkit.chunkcache.handle.ChunkPos;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public abstract class ChunkCache {

    private final long storedAt;

    private final int hash;
    private final ChunkPos chunkPos;

    ChunkCache( int hash, int x, int z ) {
        this.storedAt = System.currentTimeMillis();
        this.hash = hash;
        this.chunkPos = new ChunkPos( x, z );
    }

    public long getStoredAt() {
        return storedAt;
    }

    public int getHash() {
        return hash;
    }

    public int getX() {
        return chunkPos.getX();
    }

    public int getZ() {
        return chunkPos.getZ();
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public abstract void sendTo( ProtocolManager proto, Player player, Collection<ChunkCache> chunks ) throws InvocationTargetException;
}
