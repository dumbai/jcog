package jcog.pri;

/**
 * whether it has been deleted (read-only)
 */
public interface Deleteable {

    /**
     * the result of this should be that pri() is not finite (ex: NaN)
     * returns false if already deleted (allowing overriding subclasses to know if they shold also delete)
     */
    boolean delete();

    boolean isDeleted();

}