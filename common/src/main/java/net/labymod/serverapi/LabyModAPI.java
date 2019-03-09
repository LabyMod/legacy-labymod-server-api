package net.labymod.serverapi;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Class created by qlow | Jan
 */
public final class LabyModAPI {

    /**
     * Gets the bytes we should send to a player to update the permissions
     *
     * @param permissions a map containing the permissions
     * @return the byte array that should be the payload
     */
    public byte[] getBytesToSend( Map<Permission, Boolean> permissions ) {
        // Creating a json object we will put the permissions in
        JsonObject object = new JsonObject();

        // Adding the permissions to the json object
        for ( Map.Entry<Permission, Boolean> permissionEntry : permissions.entrySet() ) {
            object.addProperty( permissionEntry.getKey().name(), permissionEntry.getValue() );
        }

        // Returning the byte array
        return getBytesToSend( "PERMISSIONS", object.toString() );
    }

    /**
     * Gets the bytes that are required to send the given message
     *
     * @param messageKey      the message's key
     * @param messageContents the message's contents
     * @return the byte array that should be the payload
     */
    public byte[] getBytesToSend( String messageKey, String messageContents ) {
        // Getting an empty buffer
        ByteBuf byteBuf = Unpooled.buffer();

        // Writing the message-key to the buffer
        writeString( byteBuf, messageKey );

        // Writing the contents to the buffer
        writeString( byteBuf, messageContents );

        // Copying the buffer's bytes to the byte array
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes( bytes );

        // Returning the byte array
        return bytes;
    }

    /**
     * Writes a varint to the given byte buffer
     *
     * @param buf   the byte buffer the int should be written to
     * @param input the int that should be written to the buffer
     */
    private void writeVarIntToBuffer( ByteBuf buf, int input ) {
        while ( (input & -128) != 0 ) {
            buf.writeByte( input & 127 | 128 );
            input >>>= 7;
        }

        buf.writeByte( input );
    }

    /**
     * Writes a string to the given byte buffer
     *
     * @param buf    the byte buffer the string should be written to
     * @param string the string that should be written to the buffer
     */
    private void writeString( ByteBuf buf, String string ) {
        byte[] abyte = string.getBytes( Charset.forName( "UTF-8" ) );

        if ( abyte.length > Short.MAX_VALUE ) {
            throw new EncoderException( "String too big (was " + string.length() + " bytes encoded, max " + Short.MAX_VALUE + ")" );
        } else {
            writeVarIntToBuffer( buf, abyte.length );
            buf.writeBytes( abyte );
        }
    }

    /**
     * Reads a varint from the given byte buffer
     *
     * @param buf the byte buffer the varint should be read from
     * @return the int read
     */
    public int readVarIntFromBuffer( ByteBuf buf ) {
        int i = 0;
        int j = 0;

        byte b0;
        do {
            b0 = buf.readByte();
            i |= (b0 & 127) << j++ * 7;
            if ( j > 5 ) {
                throw new RuntimeException( "VarInt too big" );
            }
        } while ( (b0 & 128) == 128 );

        return i;
    }

    /**
     * Reads a string from the given byte buffer
     *
     * @param buf       the byte buffer the string should be read from
     * @param maxLength the string's max-length
     * @return the string read
     */
    public String readString( ByteBuf buf, int maxLength ) {
        int i = this.readVarIntFromBuffer( buf );

        if ( i > maxLength * 4 ) {
            throw new DecoderException( "The received encoded string buffer length is longer than maximum allowed (" + i + " > " + maxLength * 4 + ")" );
        } else if ( i < 0 ) {
            throw new DecoderException( "The received encoded string buffer length is less than zero! Weird string!" );
        } else {
            byte[] bytes = new byte[i];
            buf.readBytes( bytes );

            String s = new String( bytes, Charset.forName( "UTF-8" ) );
            if ( s.length() > maxLength ) {
                throw new DecoderException( "The received string length is longer than maximum allowed (" + i + " > " + maxLength + ")" );
            } else {
                return s;
            }
        }
    }

}
