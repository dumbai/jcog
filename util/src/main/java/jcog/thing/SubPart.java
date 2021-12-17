package jcog.thing;

/** part of a part.  simplified instance for management by the super-part */
public interface SubPart<T> {

    SubPart[] EmptyArray = new SubPart[0];

    default void startIn(T t) {

    }

    default void stopIn(T t) {

    }

}
