package net.labymod.server.bungeecord;

import com.google.gson.JsonElement;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.bungeecord.configuration.PermissionsConfigurationProvider;
import net.labymod.server.bungeecord.event.MessageSendEvent;
import net.labymod.server.bungeecord.event.PermissionSendEvent;
import net.labymod.server.bungeecord.listener.PluginMessageListener;
import net.labymod.server.bungeecord.listener.PostLoginListener;
import net.labymod.server.common.buffer.ByteBuffers;
import net.labymod.server.common.util.MapToByteArrayConverter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.protocol.packet.PluginMessage;

import java.io.File;

public class LabyModBungeeCordPlugin extends Plugin {

    private static LabyModBungeeCordPlugin instance;

    private PermissionsConfigurationProvider permissionConfiguration;

    @NonNull
    public static LabyModBungeeCordPlugin instance( ) {
        return instance;
    }

    @Override
    public void onEnable( ) {
        this.initialize();
    }

    private void initialize( ) {
        if ( !this.getDataFolder().exists() ) {
            this.getDataFolder().mkdir();
        }

        instance = this;

        this.permissionConfiguration = new PermissionsConfigurationProvider( new File( this.getDataFolder(), "permissions.yml" ) );

        this.getProxy().getPluginManager().registerListener( this, new PostLoginListener( this ) );
        this.getProxy().getPluginManager().registerListener( this, new PluginMessageListener() );
    }

    public void sendPermissions( @NonNull ProxiedPlayer proxiedPlayer ) {
        PermissionSendEvent permissionSendEvent = new PermissionSendEvent(
                proxiedPlayer,
                this.permissionConfiguration.permissions() );
        this.getProxy().getPluginManager().callEvent( permissionSendEvent );

        if ( !permissionSendEvent.isCancelled() ) {
            proxiedPlayer.unsafe().sendPacket( new PluginMessage( "LMC",
                    MapToByteArrayConverter.convert( this.permissionConfiguration.permissions() ),
                    false ) );
        }
    }

    public void sendMessageToServer( @NonNull ProxiedPlayer proxiedPlayer, @NonNull String messageKey,
                                     @NonNull JsonElement jsonElement ) {
        MessageSendEvent messageSendEvent = new MessageSendEvent( proxiedPlayer, messageKey, jsonElement );
        this.getProxy().getPluginManager().callEvent( messageSendEvent );

        ByteBuffers byteBuffers = ByteBuffers.standard();

        if ( !messageSendEvent.isCancelled() )
            proxiedPlayer.unsafe().sendPacket( new PluginMessage( "LMC",
                    byteBuffers.bytesToSend( messageKey, jsonElement.toString() ),
                    false ) );
    }
}
