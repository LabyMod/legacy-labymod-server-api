package net.labymod.server.bungeecord.event;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.common.addon.model.AddonModel;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

import java.util.List;

/**
 * The {@link LabyPlayerJoinEvent} extends {@link Event} there are triggered when the {@link ProxiedPlayer} join
 * the proxy server.
 *
 * @author Manuel Kollus
 * @version 2.0
 * @since 2.0
 */
public class LabyPlayerJoinEvent extends Event {

    private ProxiedPlayer proxiedPlayer;
    private List<AddonModel> addons;
    private String modificationVersion;
    private int chunkCachingVersion;
    private boolean chunkCachingEnabled;

    public LabyPlayerJoinEvent( @NonNull ProxiedPlayer proxiedPlayer, @NonNull List<AddonModel> addons,
                                @NonNull String modificationVersion,
                                int chunkCachingVersion,
                                boolean chunkCachingEnabled ) {
        this.proxiedPlayer = proxiedPlayer;
        this.addons = addons;
        this.modificationVersion = modificationVersion;
        this.chunkCachingVersion = chunkCachingVersion;
        this.chunkCachingEnabled = chunkCachingEnabled;
    }

    @NonNull
    public ProxiedPlayer proxiedPlayer( ) {
        return this.proxiedPlayer;
    }

    @NonNull
    public List<AddonModel> addons( ) {
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
}
