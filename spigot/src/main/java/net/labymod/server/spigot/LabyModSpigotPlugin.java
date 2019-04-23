package net.labymod.server.spigot;

import com.google.gson.JsonElement;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.common.buffer.ByteBuffers;
import net.labymod.server.common.util.MapToByteArrayConverter;
import net.labymod.server.spigot.configuration.PermissionsConfigurationProvider;
import net.labymod.server.spigot.event.MessageSendEvent;
import net.labymod.server.spigot.event.PermissionSendEvent;
import net.labymod.server.spigot.listener.LMCPluginMessageListener;
import net.labymod.server.spigot.listener.LabyModPluginMessageListener;
import net.labymod.server.spigot.listener.PlayerJoinListener;
import net.labymod.server.spigot.utils.PacketUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class LabyModSpigotPlugin extends JavaPlugin {

    private static LabyModSpigotPlugin instance;

    private PermissionsConfigurationProvider permissionsConfigurationProvider;
    private PacketUtils packetUtils;

    @NonNull
    public static LabyModSpigotPlugin instance( ) {
        return instance;
    }

    @Override
    public void onEnable( ) {
        this.initialize();
    }

    @Override
    public void onDisable( ) {
        this.getServer().getMessenger().unregisterIncomingPluginChannel( this, "LABYMOD" );
        this.getServer().getMessenger().unregisterIncomingPluginChannel( this, "LMC" );
    }

    private void initialize( ) {
        instance = this;

        this.permissionsConfigurationProvider = new PermissionsConfigurationProvider( new File( getDataFolder(), "permissions.yml" ) );
        this.packetUtils = new PacketUtils();

        this.getServer().getPluginManager().registerEvents( new PlayerJoinListener( this ), this );
        this.getServer().getMessenger().registerIncomingPluginChannel( this, "LABYMOD", new LabyModPluginMessageListener() );
        this.getServer().getMessenger().registerIncomingPluginChannel( this, "LMC", new LMCPluginMessageListener() );
    }

    public void sendPermissions( @NonNull Player player ) {
        PermissionSendEvent permissionSendEvent = new PermissionSendEvent(
                player,
                this.permissionsConfigurationProvider.permissions() );
        this.getServer().getPluginManager().callEvent( permissionSendEvent );

        if ( !permissionSendEvent.isCancelled() ) {
            packetUtils.sendPacket( player, packetUtils.getPluginMessagePacket(
                    "LMC", MapToByteArrayConverter.convert( this.permissionsConfigurationProvider.permissions() ) ) );
        }
    }

    public void sendMessageToServer( @NonNull Player player, @NonNull String messageKey,
                                     @NonNull JsonElement jsonElement ) {
        MessageSendEvent messageSendEvent = new MessageSendEvent( player, messageKey, jsonElement );
        this.getServer().getPluginManager().callEvent( messageSendEvent );

        ByteBuffers byteBuffers = ByteBuffers.standard();

        if ( !messageSendEvent.isCancelled() ) {
            packetUtils.sendPacket( player, packetUtils.getPluginMessagePacket( "LMC",
                    byteBuffers.bytesToSend( messageKey, jsonElement.toString() ) ) );
        }
    }
}
