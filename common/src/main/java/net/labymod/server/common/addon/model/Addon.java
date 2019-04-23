package net.labymod.server.common.addon.model;

import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Objects;
import java.util.UUID;

/**
 * The {@link Addon} implements the {@link AddonBuilder}. These is an object which are set and
 * get the {@link #name} or {@link #uniqueId}.
 *
 * @author Manuel Kollus
 * @version 1.0
 * @since 1.0
 */
public class Addon implements AddonBuilder {

    private String name;
    private UUID uniqueId;

    @NonNull
    public String name( ) {
        return this.name;
    }

    @NonNull
    public AddonBuilder withName( @NonNull String name ) {
        this.name = name;
        return this;
    }

    @NonNull
    public UUID uniqueId( ) {
        return this.uniqueId;
    }

    @NonNull
    public AddonBuilder withUniqueId( @NonNull UUID uniqueId ) {
        this.uniqueId = uniqueId;
        return this;
    }

    @NonNull
    @CheckReturnValue
    public Addon build( ) {
        return this;
    }

    @Override
    public boolean equals( Object other ) {
        if ( other == this ) {
            return true;
        }
        if ( other == null || other.getClass() != this.getClass() ) {
            return false;
        }

        Addon addon = (Addon) other;

        return this.name.equals( addon.name ) &&
                this.uniqueId.equals( addon.uniqueId );
    }

    @Override
    public int hashCode( ) {
        return Objects.hash( this.name, this.uniqueId );
    }

    @Override
    public String toString( ) {
        return "Addon{" +
                "name='" + this.name + '\'' +
                ", uniqueId=" + this.uniqueId +
                '}';
    }
}
