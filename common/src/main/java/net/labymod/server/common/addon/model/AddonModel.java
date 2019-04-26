package net.labymod.server.common.addon.model;

import java.util.Objects;
import java.util.UUID;

/**
 * The {@link AddonModel} is an object which are set and get the {@link #name} or {@link #uniqueId} of the addon.
 *
 * @author Manuel Kollus
 * @version 2.0
 * @since 2.0
 */
public final class AddonModel {

    public static final class Builder {

        private String name;
        private UUID uniqueId;

        private Builder( ) {
            this.name = "undefined";
            this.uniqueId = UUID.randomUUID();
        }

        public Builder withName( String name ) {
            this.name = Objects.requireNonNull( name, "name cannot be null!" );
            return this;
        }

        public Builder withUniqueId( UUID uniqueId ) {
            this.uniqueId = Objects.requireNonNull( uniqueId, "uniqueId cannot be null!" );
            return this;
        }

        public AddonModel create( ) {
            return new AddonModel(
                    this.name,
                    this.uniqueId );
        }
    }

    public static Builder newBuilder( ) {
        return new Builder();
    }

    private String name;
    private UUID uniqueId;

    private AddonModel(
            String name,
            UUID uniqueId ) {
        this.name = Objects.requireNonNull( name, "name cannot be null!" );
        this.uniqueId = Objects.requireNonNull( uniqueId, "uniqueId cannot be null!" );
    }

    public String name( ) {
        return this.name;
    }

    public UUID uniqueId( ) {
        return this.uniqueId;
    }

    @Override
    public boolean equals( Object other ) {
        if ( this == other ) {
            return true;
        }

        if ( !( other instanceof AddonModel ) ) {
            return false;
        }

        AddonModel that = (AddonModel) other;

        return Objects.equals( this.name, that.name ) &&
                Objects.equals( this.uniqueId, that.uniqueId );
    }

    @Override
    public int hashCode( ) {
        return Objects.hash(
                this.name,
                this.uniqueId );
    }

    @Override
    public String toString( ) {
        return "AddonModel{" +
                "name='" + this.name + '\'' +
                ", uniqueId=" + this.uniqueId +
                '}';
    }
}
