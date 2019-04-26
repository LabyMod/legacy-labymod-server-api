package net.labymod.server.common.addon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.common.addon.model.AddonModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * The {@link StandardAddonCollector} implement the {@link AddonCollector} to collect
 * all {@link AddonModel} as {@link List} from {@link JsonObject}.
 *
 * @author Manuel Kollus
 * @version 2.0
 * @since 2.0
 */
final class StandardAddonCollector implements AddonCollector {

    @Override
    public List<AddonModel> collectAddons( @NonNull JsonObject jsonObject ) {
        if ( !jsonObject.has( "addons" ) || !jsonObject.get( "addons" ).isJsonArray() ) {
            return Collections.emptyList();
        }

        List<AddonModel> addons = new ArrayList<>();

        for ( JsonElement arrayElement : jsonObject.get( "addons" ).getAsJsonArray() ) {
            if ( !arrayElement.isJsonObject() )
                continue;

            JsonObject arrayObject = arrayElement.getAsJsonObject();

            if ( !arrayObject.has( "uuid" ) || !arrayObject.get( "uuid" ).isJsonPrimitive() || !arrayObject.get( "uuid" ).getAsJsonPrimitive().isString()
                    || !arrayObject.has( "name" ) || !arrayObject.get( "name" ).isJsonPrimitive() || !arrayObject.get( "name" ).getAsJsonPrimitive().isString() )
                continue;

            UUID uuid;

            try {
                uuid = UUID.fromString( arrayObject.get( "uuid" ).getAsString() );
            } catch ( IllegalArgumentException ignored ) {
                continue;
            }

            addons.add( AddonModel.newBuilder()
                    .withName( arrayObject.get( "name" ).getAsString() )
                    .withUniqueId( uuid )
                    .create() );
        }

        return addons;
    }
}
