package net.labymod.serverapi.bungee;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.NonNull;
import net.labymod.serverapi.LabyModAPI;
import net.labymod.serverapi.LabyModConfig;
import net.labymod.serverapi.Permission;
import net.labymod.serverapi.bungee.event.MessageSendEvent;
import net.labymod.serverapi.bungee.event.PermissionsSendEvent;
import net.labymod.serverapi.bungee.listener.PlayerJoinListener;
import net.labymod.serverapi.bungee.listener.PluginMessageListener;
import net.labymod.serverapi.discord.RichPresence;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.protocol.packet.PluginMessage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * Class created by qlow | Jan
 */
public class LabyModPlugin extends Plugin {

    @Getter
    private static LabyModPlugin instance;

    private static final JsonParser JSON_PARSER = new JsonParser();

    @Getter
    private LabyModConfig labyModConfig;

    @Getter
    private LabyModAPI api = new LabyModAPI();

    @Override
    public void onEnable() {
        instance = this;

        // Creating the data folder
        if ( !getDataFolder().exists() )
            getDataFolder().mkdir();

        // Initializing the config
        this.labyModConfig = new BungeecordLabyModConfig( new File( getDataFolder(), "config.yml" ) );

        // Registering the listeners
        getProxy().getPluginManager().registerListener( this, new PlayerJoinListener() );
        getProxy().getPluginManager().registerListener( this, new PluginMessageListener() );
    }

    /**
     * Sends the modified permissions to the given player
     *
     * @param player the player the permissions should be sent to
     */
    public void sendPermissions( ProxiedPlayer player ) {
        Map<Permission, Boolean> modifiedPermissions = new HashMap<>( labyModConfig.getPermissions() );

        // Calling the Bukkit event
        PermissionsSendEvent sendEvent = new PermissionsSendEvent( player, modifiedPermissions, false );
        getProxy().getPluginManager().callEvent( sendEvent );

        // Sending the packet
        if ( !sendEvent.isCancelled() )
            player.unsafe().sendPacket( new PluginMessage( "LMC", api.getBytesToSend( modifiedPermissions ), false ) );
    }

    /**
     * Sends a JSON server-message to the player
     *
     * @param player          the player the message should be sent to
     * @param messageKey      the message's key
     * @param messageContents the message's contents
     */
    public void sendServerMessage( ProxiedPlayer player, String messageKey, JsonElement messageContents ) {
        messageContents = cloneJson( messageContents );

        // Calling the Bukkit event
        MessageSendEvent sendEvent = new MessageSendEvent( player, messageKey, messageContents, false );
        getProxy().getPluginManager().callEvent( sendEvent );

        // Sending the packet
        if ( !sendEvent.isCancelled() )
            player.unsafe().sendPacket( new PluginMessage( "LMC", api.getBytesToSend( messageKey, messageContents.toString() ), false ) );
    }
    
    /**
     * Sends the Discord Rich Presence message to the player
     * 
     * @param player		the player the rich presence should be sent to
     * @param richPresence	the presence object
     */
    public void sendRichPresence (@NonNull final ProxiedPlayer player, RichPresence richPresence ) {
    	sendServerMessage(player, "discord_rpc", richPresence.toJson());
    	
    	richPresence.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object object) {
				if(player.isConnected()) {
					observable.deleteObserver(this);
				}
				
				sendServerMessage(player, "discord_rpc", ((RichPresence) observable).toJson());
			}});
    }

    /**
     * Clones a JsonElement
     *
     * @param cloneElement the element that should be cloned
     * @return the cloned element
     */
    public JsonElement cloneJson( JsonElement cloneElement ) {
        try {
            return JSON_PARSER.parse( cloneElement.toString() );
        } catch ( JsonParseException ex ) {
            ex.printStackTrace();
            return null;
        }
    }

}
