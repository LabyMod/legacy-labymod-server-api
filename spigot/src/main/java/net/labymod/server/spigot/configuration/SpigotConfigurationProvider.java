package net.labymod.server.spigot.configuration;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.common.configuration.LabyConfigurationProvider;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * The {@link SpigotConfigurationProvider} implement the {@link LabyConfigurationProvider} there
 * are providing the {@link FileConfiguration} from the Bukkit api.
 *
 * @author Manuel Kollus
 * @version 1.0
 * @since 1.0
 */
public class SpigotConfigurationProvider implements LabyConfigurationProvider {

    private File configurationFile;
    private FileConfiguration fileConfiguration;

    public SpigotConfigurationProvider( File configurationFile ) {
        if ( !configurationFile.exists() ) {
            try {
                configurationFile.createNewFile();
            } catch ( IOException cause ) {
                Bukkit.getLogger().log( Level.WARNING, cause.getMessage() );
            }
        }

        this.initialize( configurationFile );
    }

    @Override
    public void initialize( @NonNull File configurationFile ) {
        this.configurationFile = configurationFile;
        this.fileConfiguration = YamlConfiguration.loadConfiguration( configurationFile );
        this.fileConfiguration.options().copyDefaults( true );
    }

    @Override
    public void addDefault( @NonNull String key, @NonNull Object value ) {
        if ( !this.fileConfiguration.contains( key ) ) {
            this.fileConfiguration.set( key, value );
        }
    }

    @Override
    public void save( ) {
        try {
            this.fileConfiguration.save( this.configurationFile );
        } catch ( IOException cause ) {
            Bukkit.getLogger().log( Level.WARNING, cause.getMessage() );
        }
    }

    @NonNull
    @Override
    public <T> T collectValue( @NonNull String key ) {
        return (T) this.fileConfiguration.get( key );
    }
}
