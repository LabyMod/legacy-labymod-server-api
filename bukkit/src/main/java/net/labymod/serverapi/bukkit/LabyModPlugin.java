package net.labymod.serverapi.bukkit;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import net.labymod.serverapi.LabyModAPI;
import net.labymod.serverapi.LabyModConfig;
import net.labymod.serverapi.Permission;
import net.labymod.serverapi.bukkit.chunkcache.ChunkCachingInstance;
import net.labymod.serverapi.bukkit.event.LabyModPlayerJoinEvent;
import net.labymod.serverapi.bukkit.event.MessageReceiveEvent;
import net.labymod.serverapi.bukkit.event.MessageSendEvent;
import net.labymod.serverapi.bukkit.event.PermissionsSendEvent;
import net.labymod.serverapi.bukkit.listener.PlayerJoinListener;
import net.labymod.serverapi.bukkit.utils.PacketUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Class created by qlow | Jan
 */
public class LabyModPlugin extends JavaPlugin {

    @Getter
    private static LabyModPlugin instance;

    @Getter
    private final static JsonParser jsonParser = new JsonParser();

    @Getter
    private LabyModConfig labyModConfig;

    @Getter
    private LabyModAPI api = new LabyModAPI();

    @Getter
    private PacketUtils packetUtils;

    @Override
    public void onEnable() {
        instance = this;

        // Initializing packet utils
        this.packetUtils = new PacketUtils();

        // Creating the data folder
        if ( !getDataFolder().exists() )
            getDataFolder().mkdir();

        // Initializing the config
        this.labyModConfig = new BukkitLabyModConfig( new File( getDataFolder(), "config.yml" ) );

        // Registering the listeners
        Bukkit.getPluginManager().registerEvents( new PlayerJoinListener(), this );

        // Registering the incoming plugin messages listeners
        getServer().getMessenger().registerIncomingPluginChannel( this, "LABYMOD", new PluginMessageListener() {
            @Override
            public void onPluginMessageReceived( String channel, final Player player, byte[] bytes ) {
                // Converting the byte array into a byte buffer
                ByteBuf buf = Unpooled.wrappedBuffer( bytes );

                try {
                    // Reading the version from the buffer
                    final String version = api.readString( buf, Short.MAX_VALUE );

                    // Calling the event synchronously
                    Bukkit.getScheduler().runTask( LabyModPlugin.this, new Runnable() {
                        @Override
                        public void run() {
                            // Checking whether the player is still online
                            if ( !player.isOnline() )
                                return;

                            // Calling the LabyModPlayerJoinEvent
                            Bukkit.getPluginManager().callEvent( new LabyModPlayerJoinEvent( player, version ) );
                        }
                    } );
                } catch ( RuntimeException ex ) {
                }
            }
        } );

        getServer().getMessenger().registerIncomingPluginChannel( this, "LMC", new PluginMessageListener() {
            @Override
            public void onPluginMessageReceived( String channel, final Player player, byte[] bytes ) {
                // Converting the byte array into a byte buffer
                ByteBuf buf = Unpooled.wrappedBuffer( bytes );

                try {
                    // Reading the message key
                    final String messageKey = api.readString( buf, Short.MAX_VALUE );
                    final String messageContents = api.readString( buf, Short.MAX_VALUE );
                    final JsonElement jsonMessage = jsonParser.parse( messageContents );

                    // Calling the event synchronously
                    Bukkit.getScheduler().runTask( LabyModPlugin.this, new Runnable() {
                        @Override
                        public void run() {
                            // Checking whether the player is still online
                            if ( !player.isOnline() )
                                return;

                            // Calling the LabyModPlayerJoinEvent
                            Bukkit.getPluginManager().callEvent( new MessageReceiveEvent( player, messageKey, jsonMessage ) );
                        }
                    } );
                } catch ( RuntimeException ex ) {
                }
            }
        } );

        if ( labyModConfig.isChunkCachingEnabled() && Bukkit.getPluginManager().isPluginEnabled( "ProtocolLib" ) ) {
            try {
                new ChunkCachingInstance();
            } catch ( Exception e ) {
                getLogger().log( Level.WARNING, "Failed to initialize ChunkCaching", e );
            }
        } else {
            getLogger().log( Level.INFO, "ChunkCaching disabled (disabled or missing ProtocolLib)");
        }
    }

    @Override
    public void onDisable() {
        // Unregistering the plugin-message listeners
        getServer().getMessenger().unregisterIncomingPluginChannel( this, "LABYMOD" );
        getServer().getMessenger().unregisterIncomingPluginChannel( this, "LMC" );
    }

    /**
     * Sends the modified permissions to the given player
     *
     * @param player the player the permissions should be sent to
     */
    public void sendPermissions( Player player ) {
        Map<Permission, Boolean> modifiedPermissions = new HashMap<>();
        modifiedPermissions.putAll( labyModConfig.getPermissions() );

        // Calling the Bukkit event
        PermissionsSendEvent sendEvent = new PermissionsSendEvent( player, modifiedPermissions, false );
        Bukkit.getPluginManager().callEvent( sendEvent );

        // Sending the packet
        if ( !sendEvent.isCancelled() && sendEvent.getPermissions().size() > 0 )
            packetUtils.sendPacket( player, packetUtils.getPluginMessagePacket( "LMC", api.getBytesToSend( modifiedPermissions ) ) );
    }

    /**
     * Sends a JSON server-message to the player
     *
     * @param player          the player the message should be sent to
     * @param messageKey      the message's key
     * @param messageContents the message's contents
     */
    public void sendServerMessage( Player player, String messageKey, JsonElement messageContents ) {
        messageContents = cloneJson( messageContents );

        // Calling the Bukkit event
        MessageSendEvent sendEvent = new MessageSendEvent( player, messageKey, messageContents, false );
        Bukkit.getPluginManager().callEvent( sendEvent );

        // Sending the packet
        if ( !sendEvent.isCancelled() )
            packetUtils.sendPacket( player, packetUtils.getPluginMessagePacket( "LMC", api.getBytesToSend( messageKey, messageContents.toString() ) ) );
    }

    /**
     * Clones a JsonElement
     *
     * @param cloneElement the element that should be cloned
     * @return the cloned element
     */
    public JsonElement cloneJson( JsonElement cloneElement ) {
        try {
            return jsonParser.parse( cloneElement.toString() );
        } catch ( JsonParseException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

}
