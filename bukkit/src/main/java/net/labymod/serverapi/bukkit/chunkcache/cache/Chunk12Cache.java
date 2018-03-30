package net.labymod.serverapi.bukkit.chunkcache.cache;

import com.comphenix.protocol.ProtocolManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.labymod.serverapi.bukkit.LabyModPlugin;
import org.bukkit.entity.Player;

import java.util.Collection;

public class Chunk12Cache extends ChunkCache {
    private final ByteBuf buf;

    public Chunk12Cache( int hash, int x, int z, ByteBuf buf ) {
        super( hash, x, z );
        this.buf = buf;
    }

    @Override
    public void sendTo( ProtocolManager proto, Player player, Collection<ChunkCache> chunks ) {
        Channel channel = LabyModPlugin.getInstance().getPacketUtils().getChannel( player );
        ChannelHandlerContext context = channel.pipeline().context( "laby_chunks" );
        for ( ChunkCache chunk : chunks ) {
            Chunk12Cache ch12 = (Chunk12Cache) chunk;

            context.write( ch12.buf );
        }
    }
}