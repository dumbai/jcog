package jcog.tree.interval;

/** defines an interval between two comparable values */
public class Between<K extends Comparable<? super K>> implements Comparable<Between<K>> {
	
	 public final K low;
    public final K high;
	
	public Between( K low,  K high){
        this.low = low;
        this.high = high;
	}

	
	final K getHigh() {
		return high;
	}



    
	final K getLow() {
		return low;
	}








    final boolean contains( K p){
		return low.compareTo(p) <= 0 && high.compareTo(p) > 0;
	}
	
	/**
	 * Returns true if this Interval wholly contains i.
	 */
    final boolean contains( Between<K> i){
		return contains(i.low) && contains(i.high);
	}
	
	final boolean overlaps( K low,  K high){
		return  this.low.compareTo(high) <= 0 &&
				this.high.compareTo(low) > 0;
	}
	
	final boolean overlaps( Between<K> i){
		return overlaps(i.low,i.high);
	}
	
	@Override
	public final String toString() {
		return String.format("[%s..%s]", low, high);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Between<?> between)) return false;

		return low.equals(between.low) && high.equals(between.high);

	}

	@Override
	public int hashCode() {
		int result = low.hashCode();
		result = 31 * result + high.hashCode();
		return result;
	}

	@Override
	public int compareTo( Between<K> x) {
		int leftC = low.compareTo(x.low);
		if (leftC != 0) return leftC;
		int rightC = high.compareTo(x.high);
		return rightC;
	}
}