package net.labymod.server.common.addon;

import com.google.gson.JsonObject;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.common.addon.model.Addon;

import java.util.List;

/**
 * The {@link AddonCollector} collect all {@link Addon} as {@link List} from {@link JsonObject}.
 *
 * @author Manuel Kollus
 * @version 1.0
 * @since 1.0
 */
public interface AddonCollector {

    @NonNull
    @CheckReturnValue
    static AddonCollector standard( ) {
        return new StandardAddonCollector();
    }

    List<Addon> collectAddons( @NonNull JsonObject jsonObject );
}
