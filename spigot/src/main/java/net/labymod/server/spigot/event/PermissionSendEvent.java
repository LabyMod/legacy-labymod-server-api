package net.labymod.server.spigot.event;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.common.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Map;

/**
 * The {@link PermissionSendEvent} extends {@link Event} there are triggered when the permission send
 * to the {@link Player} client.
 *
 * @author Manuel Kollus
 * @version 1.0
 * @since 1.0
 */
public class PermissionSendEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private Player player;
    private Map<Permission, Boolean> permissions;
    private boolean cancelled;

    public PermissionSendEvent( Player player, Map<Permission, Boolean> permissions ) {
        this.player = player;
        this.permissions = permissions;
        this.cancelled = false;
    }

    @NonNull
    public Player player( ) {
        return this.player;
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

    @Override
    public HandlerList getHandlers( ) {
        return HANDLER_LIST;
    }
}
