package net.labymod.server.bungeecord.configuration;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.common.configuration.LabyConfigurationProvider;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * The {@link BungeeCordConfigurationProvider} implement the {@link LabyConfigurationProvider} there
 * are providing the {@link Configuration} from the BungeeCord api.
 *
 * @author Manuel Kollus
 * @version 1.0
 * @since 1.0
 */
public class BungeeCordConfigurationProvider implements LabyConfigurationProvider {

    private File configurationFile;
    private Configuration configuration;

    BungeeCordConfigurationProvider( @NonNull File configurationFile ) {
        if ( !configurationFile.exists() ) {
            try {
                configurationFile.createNewFile();
            } catch ( IOException cause ) {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, cause.getMessage() );
            }
        }

        this.initialize( configurationFile );
    }

    @Override
    public void initialize( @NonNull File configurationFile ) {
        ConfigurationProvider configurationProvider = ConfigurationProvider.getProvider( YamlConfiguration.class );

        try {
            this.configurationFile = configurationFile;
            this.configuration = configurationProvider.load( configurationFile );
        } catch ( IOException cause ) {
            ProxyServer.getInstance().getLogger().log( Level.WARNING, cause.getMessage() );
        }
    }

    @Override
    public void addDefault( @NonNull String key, @NonNull Object value ) {
        if ( !this.configuration.contains( key ) ) {
            this.configuration.set( key, value );
        }
    }

    @Override
    public void save( ) {
        ConfigurationProvider configurationProvider = ConfigurationProvider.getProvider( YamlConfiguration.class );

        try {
            configurationProvider.save( this.configuration, this.configurationFile );
        } catch ( IOException cause ) {
            ProxyServer.getInstance().getLogger().log( Level.WARNING, cause.getMessage() );
        }
    }

    @NonNull
    @Override
    public <T> T collectValue( @NonNull String key ) {
        return (T) this.configuration.get( key );
    }
}
