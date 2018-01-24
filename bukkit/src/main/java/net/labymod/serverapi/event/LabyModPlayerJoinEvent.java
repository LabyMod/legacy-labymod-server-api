package net.labymod.serverapi.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Class created by qlow | Jan
 */
@AllArgsConstructor
@Getter
public class LabyModPlayerJoinEvent extends Event {

    @Getter
    private final static HandlerList handlerList = new HandlerList();

    private Player player;
    private String modVersion;

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

}
