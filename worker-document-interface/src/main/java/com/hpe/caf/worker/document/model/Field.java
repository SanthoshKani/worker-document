package com.hpe.caf.worker.document.model;

import java.util.List;
import javax.annotation.Nonnull;

/**
 * Represents a field of the document. A field has a name and can have any number of values. The order of the field values is not
 * maintained so it should not be relied upon.
 */
public interface Field extends DocumentWorkerObject
{
    /**
     * Adds the specified value to this field.<p>
     * The value will be stored using the UTF-8 encoding.
     *
     * @param data the value to be stored
     */
    void add(String data);

    /**
     * Adds the specified binary blob value to this field.
     *
     * @param data the data to be stored
     */
    void add(byte[] data);

    /**
     * Associates data stored in the remote data store with this field.<p>
     * The Worker Framework has the concept of a remote data store and fields can hold data in the data store as an alternative to passing
     * it around using the standard messaging system. This is recommended if the data is large.
     *
     * @param dataRef the reference to the data in the remote data store
     */
    void addReference(String dataRef);

    /**
     * Removes all values from this field.
     */
    void clear();

    /**
     * Returns the document that this field is associated with.
     *
     * @return the document that this field is associated with
     */
    @Nonnull
    Document getDocument();

    /**
     * Retrieves the name of the field.
     *
     * @return the name of the field
     */
    @Nonnull
    String getName();

    /**
     * Returns all the non-reference field values that contain valid UTF-8 encoded strings.
     *
     * @return the non-reference string field values
     */
    @Nonnull
    List<String> getStringValues();

    /**
     * Returns a collection of all of the values that are currently associated with this field.
     *
     * @return an object which can be used to iterate over the field values
     */
    @Nonnull
    FieldValues getValues();

    /**
     * Returns true if the field has been modified from its original state.
     *
     * @return true if the field has been modified from its original state
     */
    boolean hasChanges();

    /**
     * Returns true if this field has some values associated with it.
     *
     * @return true if this field has some values associated with it
     */
    boolean hasValues();

    /**
     * Resets the field back to its original state, undoing any changes made to it using the add() or clear() methods.
     */
    void reset();
}