package net.labymod.server.common.addon;

import com.google.gson.JsonObject;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.common.addon.model.AddonModel;

import java.util.List;

/**
 * The {@link AddonCollector} collect all {@link AddonModel} as {@link List} from {@link JsonObject}.
 *
 * @author Manuel Kollus
 * @version 2.0
 * @since 2.0
 */
public interface AddonCollector {

    @NonNull
    @CheckReturnValue
    static AddonCollector standard( ) {
        return new StandardAddonCollector();
    }

    List<AddonModel> collectAddons( @NonNull JsonObject jsonObject );
}
