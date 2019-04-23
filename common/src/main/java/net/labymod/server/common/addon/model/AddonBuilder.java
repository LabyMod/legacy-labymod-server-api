package net.labymod.server.common.addon.model;

import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.UUID;

/**
 * The {@link AddonBuilder} building the {@link Addon} object.
 *
 * @author Manuel Kollus
 * @version 1.0
 * @since 1.0
 */
public interface AddonBuilder {

    @NonNull
    @CheckReturnValue
    static AddonBuilder begin( ) {
        return new Addon();
    }

    @NonNull
    String name( );

    @NonNull
    AddonBuilder withName( @NonNull String name );

    @NonNull
    UUID uniqueId( );

    @NonNull
    AddonBuilder withUniqueId( @NonNull UUID uniqueId );

    @NonNull
    @CheckReturnValue
    Addon build( );
}
