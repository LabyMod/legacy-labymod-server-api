package net.labymod.server.bungeecord.listener;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.bungeecord.LabyModBungeeCordPlugin;
import net.labymod.server.common.permission.Permission;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * The {@link PostLoginEvent} implement {@link Listener} this event are triggered when the {@link ProxiedPlayer}
 * join an proxy server then the {@link LabyModBungeeCordPlugin} send all {@link Permission} to client.
 *
 * @author Manuel Kollus
 * @version 2.0
 * @since 2.0
 */
public class PostLoginListener implements Listener {

    private LabyModBungeeCordPlugin labyModBungeeCordPlugin;

    public PostLoginListener( @NonNull LabyModBungeeCordPlugin labyModBungeeCordPlugin ) {
        this.labyModBungeeCordPlugin = labyModBungeeCordPlugin;
    }

    @EventHandler
    public void onPostLogin( PostLoginEvent event ) {
        ProxiedPlayer proxiedPlayer = event.getPlayer();

        this.labyModBungeeCordPlugin.sendPermissions( proxiedPlayer );
    }
}
