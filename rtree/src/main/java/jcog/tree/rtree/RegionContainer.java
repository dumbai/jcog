package jcog.tree.rtree;

/** TODO generic contains, intersect, etc comparator ability via one method */
@FunctionalInterface
public interface RegionContainer<T> {

    boolean contains(T x, HyperRegion b, Spatialization/*<T, ?>*/ model);

}
