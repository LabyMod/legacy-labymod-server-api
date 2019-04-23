package net.labymod.server.spigot.utils;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class PacketUtils {

    private String version;

    private Class<?> packetClass;

    private Class<?> packetPlayOutCustomPayloadClass;
    private Constructor<?> customPayloadConstructor;
    private boolean customPayloadHasBytes;

    private Class<?> packetDataSerializerClass;
    private Constructor<?> packetDataSerializerConstructor;

    private Method getHandleMethod;
    private Field playerConnectionField;
    private Field networkManagerField;

    public PacketUtils( ) {
        this.version = Bukkit.getServer().getClass().getPackage().getName().replace( ".", "," ).split( "," )[3];

        try {
            this.packetClass = getNmsClass( "Packet" );
            this.packetPlayOutCustomPayloadClass = getNmsClass( "PacketPlayOutCustomPayload" );
            this.networkManagerField = getNmsClass( "PlayerConnection" ).getDeclaredField( "networkManager" );
        } catch ( ClassNotFoundException | NoSuchFieldException cause ) {
            Bukkit.getLogger().log( Level.WARNING, cause.getMessage() );
        }

        if ( this.packetPlayOutCustomPayloadClass != null ) {
            for ( Constructor<?> constructors : packetPlayOutCustomPayloadClass.getDeclaredConstructors() ) {
                if ( constructors.getParameterTypes().length == 2 && constructors.getParameterTypes()[1] == byte[].class ) {
                    this.customPayloadHasBytes = true;
                    this.customPayloadConstructor = constructors;
                } else if ( constructors.getParameterTypes().length == 2 && constructors.getParameterTypes()[1].getSimpleName().equals( "PacketDataSerializer" ) ) {
                    customPayloadConstructor = constructors;
                }
            }

            if ( !this.customPayloadHasBytes ) {
                try {
                    this.packetDataSerializerClass = getNmsClass( "PacketDataSerializer" );
                    this.packetDataSerializerConstructor = packetDataSerializerClass.getDeclaredConstructor( ByteBuf.class );
                } catch ( ClassNotFoundException | NoSuchMethodException cause ) {
                    Bukkit.getLogger().log( Level.WARNING, cause.getMessage() );
                }
            }
        }
    }

    public Object getPlayerHandle( Player player ) {
        try {
            if ( this.getHandleMethod == null ) {
                this.getHandleMethod = player.getClass().getMethod( "getHandle" );
            }

            return this.getHandleMethod.invoke( player );
        } catch ( NoSuchMethodException | IllegalAccessException | InvocationTargetException cause ) {
            Bukkit.getLogger().log( Level.WARNING, cause.getMessage() );
        }

        return null;
    }

    public Object getPlayerConnection( @NonNull Object nmsPlayer ) {
        try {
            if ( playerConnectionField == null ) {
                playerConnectionField = nmsPlayer.getClass().getField( "playerConnection" );
            }

            return playerConnectionField.get( nmsPlayer );
        } catch ( IllegalAccessException | NoSuchFieldException cause ) {
            Bukkit.getLogger().log( Level.WARNING, cause.getMessage() );
        }

        return null;
    }

    public void sendPacket( @NonNull Player player, @NonNull Object packet ) {
        try {
            Object nmsPlayer = getPlayerHandle( player );
            Object playerConnection = getPlayerConnection( nmsPlayer );
            playerConnection.getClass().getMethod( "sendPacket", packetClass ).invoke( playerConnection, packet );
        } catch ( IllegalAccessException | InvocationTargetException | NoSuchMethodException cause ) {
            Bukkit.getLogger().log( Level.WARNING, cause.getMessage() );
        }
    }

    public Object getPluginMessagePacket( @NonNull String channel, @NonNull byte[] bytes ) {
        try {
            return this.customPayloadConstructor.newInstance( channel, customPayloadHasBytes ? bytes :
                    packetDataSerializerConstructor.newInstance( Unpooled.wrappedBuffer( bytes ) ) );
        } catch ( NullPointerException | InstantiationException | IllegalAccessException | InvocationTargetException cause ) {
            Bukkit.getLogger().log( Level.WARNING, cause.getMessage() );
        }

        return null;
    }

    @NonNull
    private Class<?> getNmsClass( @NonNull String nmsClassName ) throws ClassNotFoundException {
        return Class.forName( "net.minecraft.server." + version + "." + nmsClassName );
    }
}
