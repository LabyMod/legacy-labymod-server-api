package net.labymod.serverapi.bukkit.chunkcache.cache;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class Chunk8Cache extends ChunkCache {
    private final Object map;
    private final List<PacketContainer> signUpdates = new LinkedList<>();

    Chunk8Cache( int hash, int x, int z, Object map ) {
        super( hash, x, z );
        this.map = map;
    }


    Object getMap() {
        return map;
    }

    public List<PacketContainer> getSignUpdates() {
        return signUpdates;
    }

    @Override
    public abstract void sendTo( ProtocolManager proto, Player player, Collection<ChunkCache> chunks ) throws InvocationTargetException;
}
