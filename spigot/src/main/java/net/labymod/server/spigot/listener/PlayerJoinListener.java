package net.labymod.server.spigot.listener;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.common.permission.Permission;
import net.labymod.server.spigot.LabyModSpigotPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * The {@link PlayerJoinEvent} implement {@link Listener} this event are triggered when the {@link Player}
 * join a spigot server then the {@link LabyModSpigotPlugin} send all {@link Permission} to client.
 *
 * @author Manuel Kollus
 * @version 2.0
 * @since 2.0
 */
public class PlayerJoinListener implements Listener {

    private LabyModSpigotPlugin labyModSpigotPlugin;

    public PlayerJoinListener( @NonNull LabyModSpigotPlugin labyModSpigotPlugin ) {
        this.labyModSpigotPlugin = labyModSpigotPlugin;
    }

    @EventHandler
    public void onPlayerJoin( PlayerJoinEvent event ) {
        Player player = event.getPlayer();

        this.labyModSpigotPlugin.sendPermissions( player );
    }
}
