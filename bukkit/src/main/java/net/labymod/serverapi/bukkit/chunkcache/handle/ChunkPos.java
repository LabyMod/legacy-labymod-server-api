package net.labymod.serverapi.bukkit.chunkcache.handle;

import java.util.Objects;

public class ChunkPos {
    private final int x;
    private final int z;

    public ChunkPos( int x, int z ) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        ChunkPos chunkPos = (ChunkPos) o;
        return x == chunkPos.x &&
                z == chunkPos.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash( x, z );
    }
}
