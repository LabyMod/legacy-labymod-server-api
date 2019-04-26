package net.labymod.server.common.configuration;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;

/**
 * The {@link LabyConfigurationProvider} provides the yaml configuration then there are {@link #initialize(File)},
 * {@link #addDefault(String, Object)} and {@link #collectValue(String)}.
 *
 * @author Manuel Kollus
 * @version 2.0
 * @since 2.0
 */
public interface LabyConfigurationProvider {

    void initialize( @NonNull File fíle );

    void addDefault( @NonNull String key, @NonNull Object value );

    void save( );

    @NonNull
    <T> T collectValue( @NonNull String key );
}
