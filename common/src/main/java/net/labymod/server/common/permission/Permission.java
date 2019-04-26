package net.labymod.server.common.permission;

/**
 * The {@link Permission} represents all permissions when are a player join
 * an network and where are checked if the permission is enabled.
 *
 * <p>If not an permission enabled, then the LabyMod module was disabled.</p>
 *
 * @author Manuel Kollus
 * @version 2.0
 * @since 2.0
 */
public enum Permission {

    IMPROVED_LAVA( "Improved Lava", false ),
    CROSSHAIR_SYNC( "Crosshair sync", false ),
    REFILL_FIX( "Refill fix", false ),
    GUI_ALL( "LabyMod GUI", true ),
    GUI_POTION_EFFECTS( "Potion Effects", true ),
    GUI_ARMOR_HUD( "Armor HUD", true ),
    GUI_ITEM_HUD( "Item HUD", true ),
    BLOCKBUILD( "Blockbuild", true ),
    TAGS( "Tags", true ),
    CHAT( "Chat features", true ),
    ANIMATIONS( "Animations", true ),
    SATURATION_BAR( "Saturation bar", true );

    private String name;
    private boolean defaultEnabled;

    Permission( String name, boolean defaultEnabled ) {
        this.name = name;
        this.defaultEnabled = defaultEnabled;
    }

    public String getName( ) {
        return this.name;
    }

    public boolean isDefaultEnabled( ) {
        return this.defaultEnabled;
    }
}
