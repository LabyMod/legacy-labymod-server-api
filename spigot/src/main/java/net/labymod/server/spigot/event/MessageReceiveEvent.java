package net.labymod.server.spigot.event;

import com.google.gson.JsonElement;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * The {@link MessageReceiveEvent} extends {@link Event} there are triggered when the {@link Player} become
 * an message from server.
 *
 * @author Manuel Kollus
 * @version 2.0
 * @since 2.0
 */
public class MessageReceiveEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private Player player;
    private String messageKey;
    private JsonElement jsonElement;

    public MessageReceiveEvent( @NonNull Player player, @NonNull String messageKey,
                                @NonNull JsonElement jsonElement ) {
        this.player = player;
        this.messageKey = messageKey;
        this.jsonElement = jsonElement;
    }

    @NonNull
    public Player player( ) {
        return this.player;
    }

    @NonNull
    public String messageKey( ) {
        return this.messageKey;
    }

    @NonNull
    public JsonElement jsonElement( ) {
        return this.jsonElement;
    }

    @Override
    public HandlerList getHandlers( ) {
        return HANDLER_LIST;
    }
}
