package net.labymod.serverapi;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Class created by qlow | Jan
 */
public class BungeecordLabyModConfig extends LabyModConfig {

    private Configuration configuration;

    public BungeecordLabyModConfig( File file ) {
        super( file );

        // Creating the file if it doesn't exist
        if ( !file.exists() )
            try {
                file.createNewFile();
            } catch ( IOException e ) {
                e.printStackTrace();
            }

        // Initializing the config
        init( file );
    }

    @Override
    public void init( File file ) {
        // Loading the config
        try {
            this.configuration = ConfigurationProvider.getProvider( YamlConfiguration.class ).load( file );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        // Adding the defaults
        addDefaults();

        // Saving the config after adding the defaults
        saveConfig();

        // Loading the values
        loadValues();
    }

    @Override
    public Object getValue( String key ) {
        return configuration.get( key );
    }

    @Override
    public void addDefault( String key, Object value ) {
        if ( !configuration.contains( key ) )
            configuration.set( key, value );
    }

    @Override
    public void saveConfig() {
        try {
            ConfigurationProvider.getProvider( YamlConfiguration.class ).save( configuration, file );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
