package net.labymod.server.bungeecord.configuration;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.common.permission.Permission;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

/**
 * The {@link PermissionsConfigurationProvider} extends {@link BungeeCordConfigurationProvider} there are
 * {@link #addDefault(String, Object)} and {@link #collectPermissionConfiguration()}.
 *
 * @author Manuel Kollus
 * @version 1.0
 * @since 1.0
 */
public class PermissionsConfigurationProvider extends BungeeCordConfigurationProvider {

    private static final Map<Permission, Boolean> PERMISSIONS = new EnumMap<>( Permission.class );

    public PermissionsConfigurationProvider( @NonNull File file ) {
        super( file );

        this.addDefaults();
        this.save();
        this.collectPermissionConfiguration();
    }

    private void addDefaults( ) {
        for ( Permission permission : Permission.values() ) {
            this.addDefault( "permissions." + permission.name(), permission.isDefaultEnabled() );
        }
    }

    private void collectPermissionConfiguration( ) {
        for ( Permission permission : Permission.values() ) {
            PERMISSIONS.put( permission, this.collectValue( "permissions." + permission.name() ) );
        }
    }

    @NonNull
    public Map<Permission, Boolean> permissions( ) {
        return PERMISSIONS;
    }
}
