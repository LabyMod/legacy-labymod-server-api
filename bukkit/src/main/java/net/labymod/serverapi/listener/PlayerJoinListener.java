package net.labymod.serverapi.listener;

import net.labymod.serverapi.LabyModPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Class created by qlow | Jan
 */
public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin( PlayerJoinEvent event ) {
        Player player = event.getPlayer();

        // Sending the permissions
        LabyModPlugin.getInstance().sendPermissions( player );
    }

}
