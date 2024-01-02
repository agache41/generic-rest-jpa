package org.structured.api.quarkus.dataAccess;


import org.structured.api.quarkus.reflection.ClassReflector;

/**
 * <pre>
 *  Base Interface for entities, encapsulating the primary key getter and setter.
 *
 *  The Interface also provides the update method as a inner method of the bean.
 *  Typical usage :
 *
 *      T destination;
 *      T source;
 *      destination.update(source);
 *
 * </pre>
 *
 * @param <PK> the type parameter
 */
public interface PrimaryKey<PK> {

    /**
     * <pre>
     * The constant "id".
     * </pre>
     */
    String ID = "id";

    /**
     * <pre>
     * The entity id getter
     * </pre>
     *
     * @return returns the id.
     */
    PK getId();

    /**
     * <pre>
     * The entity id setter
     * </pre>
     *
     * @param id to set.
     */
    void setId(PK id);

    /**
     * <pre>
     * Updates the current object from the given source.
     * The method works in tandem with the {@link org.structured.api.quarkus.reflection.Write @Write } annotation
     * Only the marked fields will be updated, null values rules are respected.
     * {@link  jakarta.validation.constraints.NotNull jakarta.validation.constraints.@NotNull } annotation on the fields is checked.
     * </pre>
     *
     * @param <T>    the type parameter
     * @param source to update from
     * @return current update object
     */
    default <T extends PrimaryKey<PK>> T update(T source) {
        return ClassReflector.ofObject(source)
                             .update((T) this, source);
    }
}
