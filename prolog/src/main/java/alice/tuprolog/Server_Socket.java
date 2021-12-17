package alice.tuprolog;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Collection;


public class Server_Socket extends AbstractSocket{
	private final ServerSocket socket;
	public Server_Socket(ServerSocket s){
		socket=s;
	}
	@Override
	public ServerSocket getSocket(){
		return socket;
	}
	@Override
	public boolean isClientSocket() {
		return false;
	}

	@Override
	public boolean isServerSocket() {
		return true;
	}


	@Override
	boolean unify(Term t, Collection<Var> varsUnifiedArg1, Collection<Var> varsUnifiedArg2) {
		t = t.term();
        if (t instanceof Var) {
            return t.unify(this, varsUnifiedArg1, varsUnifiedArg2);
        } else if (t instanceof AbstractSocket && ((AbstractSocket) t).isServerSocket()) {
        	InetAddress addr= ((AbstractSocket) t).getAddress();
            return socket.getInetAddress().toString().equals(addr.toString());
        } else {
            return false;
        }
	}
	
	@Override
	public InetAddress getAddress() {
		return socket.isBound() ? socket.getInetAddress() : null;
	}
	@Override
	public boolean isDatagramSocket() {
		
		return false;
	}
	
	@Override
	public String toString(){
		return socket.toString();
	}


}