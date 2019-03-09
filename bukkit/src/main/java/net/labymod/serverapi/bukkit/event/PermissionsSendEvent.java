package net.labymod.serverapi.bukkit.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.labymod.serverapi.Permission;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.EnumMap;
import java.util.Map;

/**
 * Class created by qlow | Jan
 */
@AllArgsConstructor
@Getter
public class PermissionsSendEvent extends Event implements Cancellable {

    @Getter
    private final static HandlerList handlerList = new HandlerList();

    private Player player;
    private Map<Permission, Boolean> permissions = new EnumMap<>( Permission.class );
    @Setter
    private boolean cancelled;

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

}
