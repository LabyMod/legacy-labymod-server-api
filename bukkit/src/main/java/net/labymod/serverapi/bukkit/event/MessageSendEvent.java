package net.labymod.serverapi.bukkit.event;

import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Class created by qlow | Jan
 */
@AllArgsConstructor
@Getter
public class MessageSendEvent extends Event implements Cancellable {

    @Getter
    private final static HandlerList handlerList = new HandlerList();

    private Player player;
    private String messageKey;
    private JsonElement jsonElement;
    @Setter
    private boolean cancelled;

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

}
