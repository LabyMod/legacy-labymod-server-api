package net.labymod.server.common.buffer;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

/**
 * The {@link StandardByteBuffers} implements {@link ByteBuffers} there are implement
 * all methods for handling with the {@link ByteBuf}.
 *
 * @author Manuel Kollus
 * @version 1.0
 * @since 1.0
 */
final class StandardByteBuffers implements ByteBuffers {

    @NonNull
    @Override
    public byte[] bytesToSend( @NonNull String key, @NonNull String messageContent ) {
        ByteBuf byteBuf = Unpooled.buffer();

        this.writeString( byteBuf, key );
        this.writeString( byteBuf, messageContent );

        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes( bytes );

        return bytes;
    }

    @Override
    public int readVarIntFromBuffer( @NonNull ByteBuf byteBuf ) {
        int readingNumber = 0;
        int result = 0;

        byte readableBytes;
        do {
            readableBytes = byteBuf.readByte();
            readingNumber |= ( readableBytes & 127 ) << result++ * 7;

        } while ( ( readableBytes & 128 ) == 128 );

        return readingNumber;
    }

    @NonNull
    @Override
    public String readString( @NonNull ByteBuf byteBuf ) {
        int varIntFromBuffer = this.readVarIntFromBuffer( byteBuf );
        byte[] buffer = new byte[varIntFromBuffer];

        byteBuf.readBytes( buffer, 0, varIntFromBuffer );

        return new String( buffer, StandardCharsets.UTF_8 );
    }

    private void writeString( @NonNull ByteBuf byteBuf, @NonNull String message ) {
        byte[] bytes = message.getBytes( StandardCharsets.UTF_8 );

        this.writeVarIntToBuffer( byteBuf, bytes.length );
        byteBuf.writeBytes( bytes );
    }

    private void writeVarIntToBuffer( @NonNull ByteBuf byteBuf, int input ) {
        while ( ( input & -128 ) != 0 ) {
            byteBuf.writeByte( input & 127 | 128 );
            input >>>= 7;
        }

        byteBuf.writeByte( input );
    }
}