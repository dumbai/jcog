package jcog.pri;

/** most lightweight, single-thread impl */
public class MutablePri implements Prioritizable {

	protected float pri;

	@Override
	public void pri(float p) {
		this.pri = p;
	}

	@Override
	public boolean delete() {
		float p = this.pri;
		if (p == p) {
			this.pri = Float.NaN;
			return true;
		}
		return false;
	}

	@Override
	public final float pri() {
		return pri;
	}
}