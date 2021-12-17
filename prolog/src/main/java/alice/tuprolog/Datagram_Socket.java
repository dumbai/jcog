package alice.tuprolog;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Collection;

public class Datagram_Socket extends AbstractSocket {

	private final DatagramSocket socket;

	
	public Datagram_Socket(DatagramSocket socket) {
		super();
		this.socket = socket;
	}

	@Override
	public boolean isClientSocket() {
		
		return false;
	}

	@Override
	public boolean isServerSocket() {
		
		return false;
	}

	@Override
	public boolean isDatagramSocket() {
		
		return true;
	}

	@Override
	public DatagramSocket getSocket() {
		
		return socket;
	}

	@Override
	public InetAddress getAddress() {
		return socket.isBound() ? socket.getInetAddress() : null;
	}

	@Override
	boolean unify(Term t, Collection<Var> varsUnifiedArg1, Collection<Var> varsUnifiedArg2) {
		t = t.term();
        if (t instanceof Var) {
            return t.unify(this, varsUnifiedArg1, varsUnifiedArg2);
        } else if (t instanceof AbstractSocket && ((AbstractSocket) t).isDatagramSocket()) {
        	InetAddress addr= ((AbstractSocket) t).getAddress();
            return socket.getInetAddress().toString().equals(addr.toString());
        } else {
            return false;
        }
	}
	
	@Override
	public String toString(){
		return socket.toString();
	}

}