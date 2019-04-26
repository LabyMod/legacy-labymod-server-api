package net.labymod.server.spigot.event;

import com.google.gson.JsonElement;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * The {@link MessageSendEvent} extends {@link Event} there are triggered when an message send to {@link Player}.
 *
 * @author Manuel Kollus
 * @version 2.0
 * @since 2.0
 */
public class MessageSendEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private Player player;
    private String messageKey;
    private JsonElement jsonElement;
    private boolean cancelled;

    public MessageSendEvent( @NonNull Player player, @NonNull String messageKey,
                             @NonNull JsonElement jsonElement ) {
        this.player = player;
        this.messageKey = messageKey;
        this.jsonElement = jsonElement;
        this.cancelled = false;
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
