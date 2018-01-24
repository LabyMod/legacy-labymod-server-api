package net.labymod.serverapi.bukkit.event;

import com.google.gson.JsonElement;
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
public class MessageReceiveEvent extends Event {

    @Getter
    private final static HandlerList handlerList = new HandlerList();

    private Player player;
    private String messageKey;
    private JsonElement jsonElement;

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

}
