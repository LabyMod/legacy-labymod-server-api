package net.labymod.serverapi;

/**
 * Class created by qlow | Jan
 */
public enum Permission {

    // Permissions that are disabled by default
    IMPROVED_LAVA( "Improved Lava", false ),
    CROSSHAIR_SYNC( "Crosshair sync", false ),
    REFILL_FIX( "Refill fix", false ),

    // GUI permissions
    GUI_ALL( "LabyMod GUI", true ),
    GUI_POTION_EFFECTS( "Potion Effects", true ),
    GUI_ARMOR_HUD( "Armor HUD", true ),
    GUI_ITEM_HUD( "Item HUD", true ),

    // Permissions that are enabled by default
    BLOCKBUILD( "Blockbuild", true ),
    TAGS( "Tags", true ),
    CHAT( "Chat features", true ),
    ANIMATIONS( "Animations", true ),
    SATURATION_BAR( "Saturation bar", true );

    private String displayName;
    private boolean defaultEnabled;

    /**
     * @param displayName    the permission's display-name
     * @param defaultEnabled whether or not this permission is enabled/activated by default
     */
    Permission( String displayName, boolean defaultEnabled ) {
        this.displayName = displayName;
        this.defaultEnabled = defaultEnabled;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public boolean isDefaultEnabled() {
        return this.defaultEnabled;
    }
}
