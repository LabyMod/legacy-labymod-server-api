package net.labymod.server.bungeecord.event;

import com.google.gson.JsonElement;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

/**
 * The {@link MessageSendEvent} extends {@link Event} there are triggered when an message send to {@link ProxiedPlayer}.
 *
 * @author Manuel Kollus
 * @version 1.0
 * @since 1.0
 */
public class MessageSendEvent extends Event implements Cancellable {

    private ProxiedPlayer proxiedPlayer;
    private String messageKey;
    private JsonElement jsonElement;
    private boolean cancelled;

    public MessageSendEvent( @NonNull ProxiedPlayer proxiedPlayer, @NonNull String messageKey,
                             @NonNull JsonElement jsonElement ) {
        this.proxiedPlayer = proxiedPlayer;
        this.messageKey = messageKey;
        this.jsonElement = jsonElement;
        this.cancelled = false;
    }

    @NonNull
    public ProxiedPlayer proxiedPlayer( ) {
        return this.proxiedPlayer;
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
}
