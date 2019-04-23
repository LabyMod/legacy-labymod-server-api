package net.labymod.server.spigot.event;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.common.addon.model.Addon;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * The {@link LabyPlayerJoinEvent} extends {@link Event} there are triggered when the {@link Player} join
 * the proxy server.
 *
 * @author Manuel Kollus
 * @version 1.0
 * @since 1.0
 */
public class LabyPlayerJoinEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private Player player;
    private List<Addon> addons;
    private String modificationVersion;
    private int chunkCachingVersion;
    private boolean chunkCachingEnabled;

    public LabyPlayerJoinEvent( @NonNull Player player, @NonNull List<Addon> addons,
                                @NonNull String modificationVersion,
                                int chunkCachingVersion,
                                boolean chunkCachingEnabled ) {
        this.player = player;
        this.addons = addons;
        this.modificationVersion = modificationVersion;
        this.chunkCachingVersion = chunkCachingVersion;
        this.chunkCachingEnabled = chunkCachingEnabled;
    }

    @NonNull
    public Player player( ) {
        return this.player;
    }

    @NonNull
    public List<Addon> addons( ) {
        return this.addons;
    }

    @NonNull
    public String modificationVersion( ) {
        return this.modificationVersion;
    }

    public int chunkCachingVersion( ) {
        return this.chunkCachingVersion;
    }

    public boolean chunkCachingEnabled( ) {
        return this.chunkCachingEnabled;
    }

    @Override
    public HandlerList getHandlers( ) {
        return HANDLER_LIST;
    }
}
