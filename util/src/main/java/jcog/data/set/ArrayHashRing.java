package jcog.data.set;

/** fixed capacity ArrayHashSet. new items remove oldest item. combination of Set and Deque
 * TODO add ability to addAt/remove from both ends, like Deque<>
 * TODO use actual ring-buffer instead of List<> for faster removal from the start */
public class ArrayHashRing<X> extends ArrayHashSet<X> {

    int capacity;

    public ArrayHashRing(int capacity) {
        this.capacity = capacity;
    }

    public ArrayHashRing<X> capacity(int capacity) {
        if (this.capacity!=capacity && size() > capacity) {
            int toRemove = size() - capacity;
            pop(toRemove);
        }
        this.capacity = capacity;
        return this;
    }

    @Override
    protected void addedUnique(X x) {
        pop(size()+1-capacity);
        super.addedUnique(x);
    }

    public void pop(int n) {
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                X x = list.remove(0);
                boolean removed = set.remove(x);
                assert (removed) : "tried to remove " + x + " but couldnt";
            }
        }
    }


}
