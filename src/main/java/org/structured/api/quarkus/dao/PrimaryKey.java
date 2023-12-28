package org.structured.api.quarkus.dao;


import org.structured.api.quarkus.reflection.ClassReflector;

/**
 * Interface for Entity's that need Sequence generator.
 */
public interface PrimaryKey<PK> {

    String ID = "id";

    /**
     * The entity id getter
     *
     * @return returns the id.
     */
    PK getId();

    /**
     * The entity id setter
     *
     * @param id to set.
     */
    void setId(PK id);

    /**
     * Updates the current object from the given source.
     * The method works in tandem with the @Write annotation
     * Only the marked fields will be updated, null values rules are respected.
     * Also NotNull annotation is checked.
     *
     * @param source to update from
     * @return current update object
     */
    default <T extends PrimaryKey<PK>> T update(T source) {
        return ClassReflector.ofObject(source)
                .update((T) this, source);
    }
}
