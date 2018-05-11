package net.labymod.serverapi;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Class created by qlow | Jan
 */
public abstract class LabyModConfig {

    protected File file;

    @Getter
    private Map<Permission, Boolean> permissions = new HashMap<>();

    @Getter
    @Setter
    private boolean chunkCachingEnabled;

    @Getter
    @Setter
    private boolean labyControlEnabled;

    public LabyModConfig( File file ) {
        this.file = file;
    }

    /**
     * Called once to initialize the config
     *
     * @param file the config-file
     */
    public abstract void init( File file );

    /**
     * Gets a key's value
     *
     * @param key the key the value should be resolved from
     * @return the value according to this key or <code>null</code> if there is no value according to this key
     */
    public abstract Object getValue( String key );

    /**
     * Gets a key's value
     *
     * @param key the key the value should be resolved from
     * @return the value according to this key or <code>null</code> if there is no value according to this key
     */
    public abstract boolean getBooleanValue( String key );

    /**
     * Adds a default to the config
     *
     * @param key   the key
     * @param value the default value
     */
    public abstract void addDefault( String key, Object value );

    /**
     * Saves the config
     */
    public abstract void saveConfig();

    /**
     * Adds the config's defaults
     */
    public void addDefaults() {
        // Iterating through all permissions
        for ( Permission permission : Permission.values() ) {
            // Putting the default value in
            addDefault( "permissions." + permission.name(), permission.isDefaultEnabled() );
        }

        addDefault( "chunkcache" , true);
        addDefault( "labyperms" , true);
    }

    /**
     * Loads the config values after adding the defaults
     */
    public void loadValues() {
        // Iterating through all permissions
        for ( Permission permission : Permission.values() ) {
            Object value = getValue( "permissions." + permission.name() );

            // Checking whether there is a value according to this permission
            if ( value instanceof Boolean ) {
                // Checking whether the permission value was modified
                permissions.put( permission, ( Boolean ) value );
            } else {
                permissions.put( permission, permission.isDefaultEnabled() );
            }
        }

        chunkCachingEnabled = getBooleanValue( "chunkcache" );
        labyControlEnabled = getBooleanValue( "labyperms" );
    }

}
