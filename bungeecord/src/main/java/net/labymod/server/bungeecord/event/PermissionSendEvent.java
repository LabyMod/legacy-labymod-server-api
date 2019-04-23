package net.labymod.server.bungeecord.event;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.common.permission.Permission;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

import java.util.Map;

/**
 * The {@link PermissionSendEvent} extends {@link Event} there are triggered when the permission send
 * to the {@link ProxiedPlayer} client.
 *
 * @author Manuel Kollus
 * @version 1.0
 * @since 1.0
 */
public class PermissionSendEvent extends Event implements Cancellable {

    private ProxiedPlayer proxiedPlayer;
    private Map<Permission, Boolean> permissions;
    private boolean cancelled;

    public PermissionSendEvent( ProxiedPlayer proxiedPlayer, Map<Permission, Boolean> permissions ) {
        this.proxiedPlayer = proxiedPlayer;
        this.permissions = permissions;
        this.cancelled = false;
    }

    @NonNull
    public ProxiedPlayer proxiedPlayer( ) {
        return this.proxiedPlayer;
    }

    @NonNull
    public Map<Permission, Boolean> permissions( ) {
        return this.permissions;
    }

    @Override
    public boolean isCancelled( ) {
        return this.cancelled;
    }

    @Override
    public void setCancelled( boolean cancelled ) {
        this.cancelled = cancelled;
    }

}
