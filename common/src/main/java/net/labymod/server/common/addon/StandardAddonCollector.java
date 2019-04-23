package net.labymod.server.common.addon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.labymod.server.common.addon.model.Addon;
import net.labymod.server.common.addon.model.AddonBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * The {@link StandardAddonCollector} implement the {@link AddonCollector} to collect
 * all {@link Addon} as {@link List} from {@link JsonObject}.
 *
 * @author Manuel Kollus
 * @version 1.0
 * @since 1.0
 */
final class StandardAddonCollector implements AddonCollector {

    @Override
    public List<Addon> collectAddons( @NonNull JsonObject jsonObject ) {
        if ( !jsonObject.has( "addons" ) || !jsonObject.get( "addons" ).isJsonArray() ) {
            return Collections.emptyList();
        }

        List<Addon> addons = new ArrayList<>();

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

            addons.add( AddonBuilder.begin().withUniqueId( uuid ).withName( arrayObject.get( "name" ).getAsString() ).build() );
        }

        return addons;
    }
}
