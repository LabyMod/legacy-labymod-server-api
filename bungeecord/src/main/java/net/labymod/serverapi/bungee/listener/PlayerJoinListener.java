package net.labymod.serverapi.bungee.listener;

import net.labymod.serverapi.bungee.LabyModPlugin;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Class created by qlow | Jan
 */
public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin( PostLoginEvent event ) {
        // Sending the permissions
        LabyModPlugin.getInstance().sendPermissions( event.getPlayer() );
    }

}
