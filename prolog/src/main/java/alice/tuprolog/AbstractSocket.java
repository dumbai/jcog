package alice.tuprolog;

import jcog.data.list.Lst;

import java.net.InetAddress;
import java.util.Map;

public abstract class AbstractSocket extends Term{
	public abstract boolean isClientSocket();
	
	public abstract boolean isServerSocket();
	
	public abstract boolean isDatagramSocket();
	
	public abstract Object getSocket();
	
	public abstract InetAddress getAddress();


	@Override
	public boolean isEmptyList() {
		return false;
	}

	@Override
	public boolean isConstant() {
		return true;
	}
	@Override
	public boolean isAtom() {
		return false;
	}

	@Override
	public boolean isCompound() {
		return false;
	}

	@Override
	public boolean isAtomic() {
		return true;
	}

	@Override
	public boolean isList() {
		return false;
	}

	@Override
	public boolean isGround() {
		return true;
	}

	@Override
	public boolean isGreater(Term t) {
		
		return false;
	}
	@Override
	public boolean isGreaterRelink(Term t, Lst<String> vorder) {
		
		return false;
	}

	@Override
	public boolean isEqual(Term t) {
		return t == this;
	}


	@Override
	Term copy(Map<Var, Var> vMap, Map<Term, Var> substMap) {
		return this;
	}


}


