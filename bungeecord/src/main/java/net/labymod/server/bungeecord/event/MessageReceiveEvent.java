package net.labymod.server.bungeecord.event;

import com.google.gson.JsonElement;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

/**
 * The {@link MessageReceiveEvent} extends {@link Event} there are triggered when the {@link ProxiedPlayer} become
 * an message from server.
 *
 * @author Manuel Kollus
 * @version 1.0
 * @since 1.0
 */
public class MessageReceiveEvent extends Event {

    private ProxiedPlayer proxiedPlayer;
    private String messageKey;
    private JsonElement jsonElement;

    public MessageReceiveEvent( @NonNull ProxiedPlayer proxiedPlayer, @NonNull String messageKey,
                                @NonNull JsonElement jsonElement ) {
        this.proxiedPlayer = proxiedPlayer;
        this.messageKey = messageKey;
        this.jsonElement = jsonElement;
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
}
