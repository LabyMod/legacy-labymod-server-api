package net.labymod.server.common.buffer;

import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.netty.buffer.ByteBuf;

/**
 * The {@link ByteBuffers} is an helper for using with the {@link ByteBuf}.
 *
 * @author Manuel Kollus
 * @version 2.0
 * @since 2.0
 */
public interface ByteBuffers {

    @NonNull
    @CheckReturnValue
    static ByteBuffers standard( ) {
        return new StandardByteBuffers();
    }

    @NonNull
    byte[] bytesToSend( @NonNull String key, @NonNull String messageContent );

    int readVarIntFromBuffer( @NonNull ByteBuf byteBuf );

    @NonNull
    String readString( @NonNull ByteBuf byteBuf );

}
