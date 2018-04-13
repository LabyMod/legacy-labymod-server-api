package net.labymod.serverapi.bukkit.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.Getter;
import net.labymod.serverapi.bukkit.LabyModPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * Class created by qlow | Jan
 */
public class PacketUtils {

    @Getter
    private String version;

    private Class<?> packetClass;

    private Class<?> packetPlayOutCustomPayloadClass;
    private Constructor<?> customPayloadConstructor;
    private boolean customPayloadHasBytes;

    private Class<?> packetDataSerializerClass;
    private Constructor<?> packetDataSerializerConstructor;

    private Method getPlayerHandleMethod;
    private Method getWorldHandleMethod;
    private Field playerConnectionField;
    @Getter
    private Field networkManagerField;
    private Field channelField;

    @Getter
    private Class<?> chunkMapClass;
    private Field chunkMapA;
    private Field chunkMapB;

    public PacketUtils() {
        this.version = Bukkit.getServer().getClass().getPackage().getName().replace( ".", "," ).split( "," )[3];

        try {
            this.packetClass = getNmsClass( "Packet" );
            this.packetPlayOutCustomPayloadClass = getNmsClass( "PacketPlayOutCustomPayload" );
            this.networkManagerField = getNmsClass( "PlayerConnection" ).getDeclaredField( "networkManager" );
        } catch ( ClassNotFoundException | NoSuchFieldException e ) {
            e.printStackTrace();
        }

        if ( this.packetPlayOutCustomPayloadClass != null ) {
            for ( Constructor<?> constructors : packetPlayOutCustomPayloadClass.getDeclaredConstructors() ) {
                if ( constructors.getParameterTypes().length == 2 && constructors.getParameterTypes()[1] == byte[].class ) {
                    customPayloadHasBytes = true;
                    customPayloadConstructor = constructors;
                } else if ( constructors.getParameterTypes().length == 2 && constructors.getParameterTypes()[1].getSimpleName().equals( "PacketDataSerializer" ) ) {
                    customPayloadConstructor = constructors;
                }
            }

            if ( !customPayloadHasBytes ) {
                try {
                    packetDataSerializerClass = getNmsClass( "PacketDataSerializer" );
                    packetDataSerializerConstructor = packetDataSerializerClass.getDeclaredConstructor( ByteBuf.class );
                } catch ( Exception ex ) {
                    ex.printStackTrace();
                    LabyModPlugin.getInstance().getLogger().severe( "Couldn't find a valid constructor for PacketPlayOutCustomPayload. Disabling the plugin." );
                    Bukkit.getPluginManager().disablePlugin( LabyModPlugin.getInstance() );
                }
            }
        }

        if ( version.equalsIgnoreCase( "v1_8_R3" ) ) {
            try {
                chunkMapClass = getNmsClass( "PacketPlayOutMapChunk$ChunkMap" );
            } catch ( ClassNotFoundException e ) {
                LabyModPlugin.getInstance().getLogger().log( Level.WARNING, "Failed to init ChunkMap handle. Disabling ChunkCache", e );
                LabyModPlugin.getInstance().getLabyModConfig().setChunkCachingEnabled( false );
            }
        }
    }

    /**
     * Handle for net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk.ChunkMap
     *
     * @param chunkMap ChunkMap object
     * @return val of a
     */
    public byte[] chunkMapA( Object chunkMap ) {
        try {
            if ( chunkMapA == null )
                chunkMapA = chunkMap.getClass().getDeclaredField( "a" );

            // Getting the player's nms-handle
            return (byte[]) chunkMapA.get( chunkMap );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        return new byte[0];
    }

    /**
     * Handle for net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk.ChunkMap
     *
     * @param chunkMap ChunkMap object
     * @return val of b
     */
    public int chunkMapB( Object chunkMap ) {
        try {
            if ( chunkMapB == null )
                chunkMapB = chunkMap.getClass().getDeclaredField( "b" );

            // Getting the player's nms-handle
            return chunkMapB.getInt( chunkMap );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        return 0;
    }

    /**
     * Gets the player's nms-handle
     *
     * @param player the bukkit-player
     * @return the nms-handle
     */
    public Object getPlayerHandle( Player player ) {
        try {
            if ( getPlayerHandleMethod == null )
                getPlayerHandleMethod = player.getClass().getMethod( "getHandle" );

            // Getting the player's nms-handle
            return getPlayerHandleMethod.invoke( player );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Gets the world's nms-handle
     *
     * @param world the bukkit-world
     * @return the nms-handle
     */
    public Object getWorldHandle( World world ) {
        try {
            if ( getWorldHandleMethod == null )
                getWorldHandleMethod = world.getClass().getMethod( "getHandle" );

            // Getting the player's nms-handle
            return getWorldHandleMethod.invoke( world );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Gets the player's connection
     *
     * @param nmsPlayer the player's nms-handle
     * @return the player-connection
     */
    public Object getPlayerConnection( Object nmsPlayer ) {
        try {
            if ( playerConnectionField == null )
                playerConnectionField = nmsPlayer.getClass().getField( "playerConnection" );

            // Getting the player's connection
            return playerConnectionField.get( nmsPlayer );
        } catch ( IllegalAccessException | NoSuchFieldException e ) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Gets the player's netty channel
     *
     * @param player the bukkit-player
     * @return the player-netty channel
     */
    public Channel getChannel( Player player ) {
        return getChannel( getPlayerHandle( player ) );
    }

    /**
     * Gets the player's netty channel
     *
     * @param nmsPlayer the player's nms-handle
     * @return the player-netty channel
     */
    public Channel getChannel( Object nmsPlayer ) {
        Object connection = getPlayerConnection( nmsPlayer );
        try {
            if ( networkManagerField == null )
                networkManagerField = connection.getClass().getField( "networkManager" );

            // Getting the player's connection
            Object networkManager = networkManagerField.get( connection );

            if ( channelField == null )
                channelField = networkManager.getClass().getField( "channel" );

            // Getting the player's connection
            return (Channel) channelField.get( networkManager );
        } catch ( IllegalAccessException | NoSuchFieldException e ) {
            throw new RuntimeException( e );
        }
    }

    /**
     * Sends a packet to the given player
     *
     * @param player the player the packet should be sent to
     * @param packet the packet that should be sent to the player
     */
    public void sendPacket( Player player, Object packet ) {
        try {
            // Getting the player's nms-handle
            Object nmsPlayer = getPlayerHandle( player );

            // Getting the player's connection
            Object playerConnection = getPlayerConnection( nmsPlayer );

            // Sending the packet
            playerConnection.getClass().getMethod( "sendPacket", packetClass ).invoke( playerConnection, packet );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    /**
     * Sends a plugin message to a player
     *
     * @param player  the player the packet should be sent to
     * @param channel the channel to use
     * @param buffer  payload
     */
    public void sendPluginMessage( Player player, String channel, byte[] buffer ) {
        sendPacket( player, getPluginMessagePacket( channel, buffer ) );
    }

    /**
     * Gets a constructed plugin message packet
     *
     * @param channel the channel-name
     * @param bytes   the bytes that should be sent with the packet
     * @return a plugin-message packet
     */
    public Object getPluginMessagePacket( String channel, byte[] bytes ) {
        try {
            return customPayloadConstructor.newInstance( channel, customPayloadHasBytes ? bytes : packetDataSerializerConstructor.newInstance( Unpooled.wrappedBuffer( bytes ) ) );
        } catch ( NullPointerException | InstantiationException | IllegalAccessException | InvocationTargetException e ) {
            LabyModPlugin.getInstance().getLogger().severe( "Couldn't construct a custom-payload packet (Channel: " + channel + "):" );
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Gets a nms-class
     *
     * @param nmsClassName the nms-class name
     * @return the multi-version compatible name of the class including the package
     * @throws ClassNotFoundException if the class wasn't found
     */
    public Class<?> getNmsClass( String nmsClassName ) throws ClassNotFoundException {
        return Class.forName( "net.minecraft.server." + version + "." + nmsClassName );
    }

    /**
     * Sets a field's value
     *
     * @param targetObject the target object
     * @param fieldName    the field's name
     * @param value        the value
     */
    public void setField( Object targetObject, String fieldName, Object value ) {
        try {
            Field field = targetObject.getClass().getDeclaredField( fieldName );
            field.setAccessible( true );

            field.set( targetObject, value );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }


}
