package net.labymod.server.common.util;

import com.google.gson.JsonObject;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.common.buffer.ByteBuffers;
import net.labymod.server.common.permission.Permission;

import java.util.Map;

/**
 * The {@link MapToByteArrayConverter} converts an map as to byte array.
 *
 * @author Manuel Kollus
 * @version 1.0
 * @since 1.0
 */
public class MapToByteArrayConverter {

    @NonNull
    public static byte[] convert( @NonNull Map<Permission, Boolean> permissions ) {
        JsonObject jsonObject = new JsonObject();

        for ( Map.Entry<Permission, Boolean> permissionEntry : permissions.entrySet() ) {
            jsonObject.addProperty( permissionEntry.getKey().name(), permissionEntry.getValue() );
        }

        ByteBuffers byteBuffers = ByteBuffers.standard();

        return byteBuffers.bytesToSend( "PERMISSIONS", jsonObject.toString() );
    }
}
