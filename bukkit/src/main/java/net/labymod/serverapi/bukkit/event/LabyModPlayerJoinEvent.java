package net.labymod.serverapi.bukkit.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.labymod.serverapi.Addon;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

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
    private boolean chunkCachingEnabled;
    private List<Addon> addons;

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

}
