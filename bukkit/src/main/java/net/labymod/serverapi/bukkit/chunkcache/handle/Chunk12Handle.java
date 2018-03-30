package net.labymod.serverapi.bukkit.chunkcache.handle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.labymod.serverapi.bukkit.LabyModPlugin;
import net.labymod.serverapi.bukkit.chunkcache.ChunkCachingInstance;
import net.labymod.serverapi.bukkit.chunkcache.PlayerState;
import net.labymod.serverapi.bukkit.chunkcache.cache.Chunk12Cache;
import net.labymod.serverapi.bukkit.chunkcache.cache.ChunkCache;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;
import java.util.List;

public class Chunk12Handle extends ChannelOutboundHandlerAdapter {

    private final Player player;
    private final PlayerState playerState;

    public Chunk12Handle( Player player, PlayerState playerState ) {
        this.player = player;

        this.playerState = playerState;
    }

    @Override
    public void write( ChannelHandlerContext ctx, Object msg, ChannelPromise promise ) {
        try {
            if ( !(msg instanceof ByteBuf) ) {
                ctx.write( msg );
            } else {
                ByteBuf buf = (ByteBuf) msg;

                if ( buf.readableBytes() < 4 ) {
                    ctx.write( msg );
                    return;
                }

                int index = buf.readerIndex();

                int packetId = readVarInt( buf );
                if ( packetId != 32 ) {
                    buf.readerIndex( index );
                    ctx.write( buf );
                    return;
                }
                int x = buf.readInt();
                int z = buf.readInt();
                int hash = ChunkHandle.hashBuf( buf.nioBuffer() );
                buf.readerIndex( index );

                ChunkCache[] caches = new ChunkCache[]{new Chunk12Cache( hash, x, z, buf )};
                List<Integer> maps = playerState.handleSending( caches );

                if ( maps == null || maps.isEmpty() ) {
                    ByteBuffer pmsg = ByteBuffer.allocate( 16 ); // ByteByteShortIntIntInt
                    pmsg.putInt( 1 ); // The first 4 bytes are only necessary for 1.8
                    pmsg.putInt( hash );
                    pmsg.putInt( x );
                    pmsg.putInt( z );
                    LabyModPlugin.getInstance().getPacketUtils().sendPluginMessage( player, ChunkCachingInstance.PM_CHANNEL, pmsg.array() );
                    // In this case, do not call ctx.write -> message discarded for now
                } else {
                    ctx.write( buf );
                }
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    private static int readVarInt( ByteBuf buf ) {
        int var1 = 0;
        int var2 = 0;
        byte var3;
        do {
            var3 = buf.readByte();
            var1 |= (var3 & 0x7F) << var2++ * 7;
            if ( var2 > 5 ) {
                throw new RuntimeException( "VarInt too big" );
            }
        } while ( (var3 & 0x80) == 128 );
        return var1;
    }
}
