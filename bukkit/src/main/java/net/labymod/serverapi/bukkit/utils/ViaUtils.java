package net.labymod.serverapi.bukkit.utils;

import us.myles.ViaVersion.api.Via;

import java.util.UUID;

public class ViaUtils {

    public static int getVersion( UUID uuid ) {
        return Via.getAPI().getPlayerVersion( uuid );
    }

}
