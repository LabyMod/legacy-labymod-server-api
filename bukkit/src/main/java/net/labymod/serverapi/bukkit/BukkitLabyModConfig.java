package net.labymod.serverapi.bukkit;

import net.labymod.serverapi.LabyModConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Class created by qlow | Jan
 */
public class BukkitLabyModConfig extends LabyModConfig {

    private FileConfiguration fileConfiguration;

    public BukkitLabyModConfig( File file ) {
        super( file );

        // Creating the file if it doesn't exist
        if ( !file.exists() )
            try {
                file.createNewFile();
            } catch ( IOException cause ) {
                Bukkit.getLogger().warning( cause.getMessage() );
            }

        // Loading the config
        this.fileConfiguration = YamlConfiguration.loadConfiguration( file );

        // Initializing the config
        init( file );
    }

    @Override
    public void init( File file ) {
        // Applying options to the config
        fileConfiguration.options().copyDefaults( true );

        // Adding the defaults
        addDefaults();

        // Saving the config after adding the defaults
        saveConfig();

        // Loading the values
        loadValues();
    }

    @Override
    public Object getValue( String key ) {
        return fileConfiguration.get( key );
    }

    @Override
    public void addDefault( String key, Object value ) {
        fileConfiguration.addDefault( key, value );
    }

    @Override
    public void saveConfig() {
        try {
            fileConfiguration.save( file );
        } catch ( IOException cause ) {
            Bukkit.getLogger().warning( cause.getMessage() );
        }
    }
}
