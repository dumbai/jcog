package alice.tuprolog.lib;

import alice.tuprolog.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;





/**
 * 
 * @author Mirco Bordoni
 * 
 * This library implements TCP socket synchronous and asynchronous communication between Prolog hosts.
 *
 */

public class SocketLibrary extends PrologLib {
	private static final long serialVersionUID = 1L;
	private final String addrRegex;
	private final LinkedList<ThreadReader> readers;			
	private LinkedList<ServerSocket> serverSockets;		
	private LinkedList<Socket> clientSockets;			

	public SocketLibrary() {
		addrRegex = "[\\. :]";	
		readers = new LinkedList<>();
		serverSockets= new LinkedList<>();
		clientSockets= new LinkedList<>();
	}

	

	/* SocketLib UDP extension by Adelina Benedetti */
	
	

	
	public boolean udp_socket_open_2(Struct Address, Term Socket) throws PrologError
	{
		if (!(Socket.term() instanceof Var)) {
			throw PrologError.instantiation_error(prolog, 1);
		}

		
		Pattern p = Pattern.compile(addrRegex);
		String[] split = p.split(Address.name());
		if (split.length != 5)
			throw PrologError.instantiation_error(prolog, 1);
		byte[] address = new byte[4];
		for (int i = 0; i < split.length - 1; i++) {
			address[i] = Byte.parseByte(split[i]);
		}
		int port = Integer.parseInt(split[split.length - 1]);

		try {
			DatagramSocket s=new DatagramSocket(port, InetAddress.getByAddress(address));

			Socket.unify(prolog, new Datagram_Socket(s));
		} catch (IOException e) {
			e.printStackTrace();
			throw PrologError.instantiation_error(prolog, 1);
		}

		return true;
	}
	
	
	
	
	public boolean udp_send_3(Term Socket, Term Data, Struct AddressTo) throws PrologError
	{
		if (!(Socket.term() instanceof Var)) {
			throw PrologError.instantiation_error(prolog, 1);
		}

		
		Pattern p = Pattern.compile(addrRegex);
		String[] split = p.split(AddressTo.name());
		if (split.length != 5)
			throw PrologError.instantiation_error(prolog, 1);
		byte[] address = new byte[4];
		for (int i = 0; i < split.length - 1; i++) {
			address[i] = Byte.parseByte(split[i]);
		}
		int port = Integer.parseInt(split[split.length - 1]);
		DatagramSocket s = ((Datagram_Socket) Socket.term()).getSocket();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(Data);
             oos.flush();
             byte[] Buf= baos.toByteArray();
             DatagramPacket packet = new DatagramPacket(Buf, Buf.length,port);
             s.send(packet);

        } catch (IOException e) {
            
            e.printStackTrace();
        }


		return true;
}



public boolean udp_socket_close_1(Term Socket) throws PrologError {
	if (Socket.term() instanceof Var) {
		throw PrologError.instantiation_error(prolog, 1);
	}
	if (!(((AbstractSocket) Socket.term()).isDatagramSocket())) {
		throw PrologError.instantiation_error(prolog, 1);
	}
	DatagramSocket s=((Datagram_Socket) Socket.term()).getSocket();
	s.close();
	return true;
}



public boolean udp_receive(Term Socket, Term Data, Struct AddressFrom,
		Struct Options) throws PrologError {
	if (!(Socket.term() instanceof Var)) {
		throw PrologError.instantiation_error(prolog, 1);
	}

	
	Pattern p = Pattern.compile(addrRegex);
	String[] split = p.split(AddressFrom.name());
	if (split.length != 5)
		throw PrologError.instantiation_error(prolog, 1);
	byte[] address = new byte[4];
	for (int i = 0; i < split.length - 1; i++) {
		address[i] = Byte.parseByte(split[i]);
	}
	@SuppressWarnings("unused") int port = Integer.parseInt(split[split.length - 1]);
	DatagramSocket s= ((Datagram_Socket) Socket.term()).getSocket();
	byte[] buffer = new byte[100000];
	DatagramPacket packet = new DatagramPacket(buffer, buffer.length );
	try {
		
		s.receive(packet);
	} catch (IOException e) {
		
		e.printStackTrace();
	}
	LinkedList<Term> list = StructToList(Options);
	for (Term t : list) { 
		if ("timeout".equals(((Struct) t).name())) {
			int time = Integer.parseInt(((Struct) t).sub(0).toString());
			try {
				s.setSoTimeout(time);
			} catch (SocketException e) {
				e.printStackTrace();
				
			}
		}
		if("size".equals(((Struct) t).name())){
			int size=Integer.parseInt(((Struct) t).sub(0).toString());
			packet.setLength(size);
		}
	}
		
	
	return true;
}

/**
 * Create a ServerSocket bound to the specified Address.
 * 
 * @throws PrologError if Socket is not a variable
 */


public boolean tcp_socket_server_open_3(Struct Address, Term Socket, Struct Options) throws PrologError {

	if (!(Socket.term() instanceof Var)) {
		throw PrologError.instantiation_error(prolog, 1);
	}

	
	Pattern p = Pattern.compile(addrRegex);
	String[] split = p.split(Address.name());
	if (split.length != 5)
		throw PrologError.instantiation_error(prolog, 1);
	byte[] address = new byte[4];
	for (int i = 0; i < split.length - 1; i++) {
		address[i] = Byte.parseByte(split[i]);
	}
	int port = Integer.parseInt(split[split.length - 1]);


	LinkedList<Term> list = StructToList(Options); 			
	int backlog = 0;
	for (Term t : list) { 									
		if ("backlog".equals(((Struct) t).name())) {
			backlog = Integer.parseInt(((Struct) t).sub(0).toString());
		}
	}

	
	try {
		ServerSocket s=new ServerSocket(port, backlog, InetAddress.getByAddress(address));
		addServerSocket(s);
		Socket.unify(prolog, new Server_Socket(s));
	} catch (IOException e) {
		e.printStackTrace();
		throw PrologError.instantiation_error(prolog, 1);
	}

	return true;
}



private void addServerSocket(ServerSocket s){
	for(ServerSocket sock: serverSockets){
		if(sock.equals(s))return;
	}
	serverSockets.add(s);
}



private void addClientSocket(Socket s){
	for(Socket sock: clientSockets){
		if(sock.equals(s))return;
	}
	clientSockets.add(s);
}


/**
 * Accept a connection to the specified ServerSocket. This method blocks
 * until a connection is received.
 * 
 * @throws PrologError if ServerSock is a variable or it is not a Server_Socket
 */

public boolean tcp_socket_server_accept_3(Term ServerSock, Term Client_Addr, Term Client_Slave_Socket) throws PrologError {

	if (ServerSock.term() instanceof Var) {
		throw PrologError.instantiation_error(prolog, 1);
	}

	AbstractSocket as= (AbstractSocket)ServerSock.term();
	if(!as.isServerSocket()){									
		throw PrologError.instantiation_error(prolog, 1);
	}

	ServerSocket s = ((Server_Socket) ServerSock.term()).getSocket();
	try {
		Socket client = s.accept();
		Client_Addr.unify(prolog, new Struct(client.getInetAddress().getHostAddress() + ':' + client.getPort()));
		Client_Slave_Socket.unify(prolog, new Client_Socket(client));
		addClientSocket(client);
	} catch (IOException e) {
		
		return false;
	}
	return true;
}

/**
 * Create a Client_Socket and connect it to a specified address.
 * @throws PrologError if Socket is not a variable
 */

public boolean tcp_socket_client_open_2(Struct Address, Term SocketTerm) throws PrologError {
	if (!(SocketTerm.term() instanceof Var)) {
		throw PrologError.instantiation_error(prolog, 2);
	}

	
	Pattern p = Pattern.compile(addrRegex);
	String[] split = p.split(Address.name());
	if (split.length != 5)
		throw PrologError.instantiation_error(prolog, 1);
	byte[] address = new byte[4];
	for (int i = 0; i < split.length - 1; i++) {
		address[i] = Byte.parseByte(split[i]);
	}
	int port = Integer.parseInt(split[split.length - 1]);

	try {
		Socket s = new Socket(InetAddress.getByAddress(address), port);
		SocketTerm.unify(prolog, new Client_Socket(s));
		addClientSocket(s);
	} catch (IOException e) {
		e.printStackTrace();
		return false;
	}
	return true;
}

/**
 * Close a Server_Socket
 * @throws PrologError if serverSocket is a variable or it is not a Server_Socket
 */

public synchronized boolean tcp_socket_server_close_1(Term serverSocket) throws PrologError {
	if (serverSocket.term() instanceof Var) {
		throw PrologError.instantiation_error(prolog, 1);
	}
	if (!(((AbstractSocket) serverSocket.term()).isServerSocket())) {
		throw PrologError.instantiation_error(prolog, 1);
	}
	try {
		ServerSocket s=((Server_Socket) serverSocket.term()).getSocket();
		s.close();
		
		for(int i=0;i<serverSockets.size();i++){
			if(serverSockets.get(i).equals(s)){
				serverSockets.remove(i);
				return true;
			}
		}
	} catch (IOException e) {
		e.printStackTrace();
		return false;
	}
	return true;
}

/**
 * Send Msg through the socket Socket. Socket has to be connected!
 * @throws PrologError if Socket is a variable or it is not a Client_Socket or Msg is not bound
 */

public boolean write_to_socket_2(Term Socket, Term Msg) throws PrologError {
	if (Socket.term() instanceof Var) {
		throw PrologError.instantiation_error(prolog, 1);
	}
	if (((AbstractSocket) Socket.term()).isServerSocket()) { 
		throw PrologError.instantiation_error(prolog, 1);
	}
	if (Msg.term() instanceof Var) {
		throw PrologError.instantiation_error(prolog, 2);

	} else {
		Socket sock = ((Client_Socket) Socket.term()).getSocket();
		try {
			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			out.writeObject(Msg);		
		} catch (IOException e) {
			
			return false;
		}

	}
	return true;
}

/**
 * Synchronous reading from Socket. This is a blocking operation.
 * @param Options The user can specify a timeout using [timeout(millis)]. If timeout expires the
 * 					predicate fails
 * @throws PrologError if Socket is not bound or it is not a Client_Socket or Msg is bound
 */

public boolean read_from_socket_3(Term Socket, Term Msg, Struct Options) throws PrologError {
	if (Socket.term() instanceof Var) {
		throw PrologError.instantiation_error(prolog, 1);
	}
	if (!(Msg.term() instanceof Var)) {
		throw PrologError.instantiation_error(prolog, 2);
	}
	if (!((AbstractSocket) Socket.term()).isClientSocket()) { 
		throw PrologError.instantiation_error(prolog, 1);
	} else {
		Socket sock = ((Client_Socket) Socket.term()).getSocket();

		
		ThreadReader r = readerExist(sock);
		
		if (r != null) {
			if (r.started())
				return false;
		}

		LinkedList<Term> list = StructToList(Options); 
		for (Term t : list) { 
			if ("timeout".equals(((Struct) t).name())) {
				int time = Integer.parseInt(((Struct) t).sub(0).toString());
				try {
					sock.setSoTimeout(time); 
				} catch (SocketException e) {
					e.printStackTrace();
					return false;
				}
			}
		}



		try {
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
			Term m = (Term) in.readObject();
			Msg.unify(prolog, m);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}

	}
	return true;
}

/**
 * Asynchronous read from Socket. When a message is received an assertA
 * (by default) is executed to put it in the theory. The user can set the
 * option "assertZ" to use assertZ instead of assertA.
 * 
 * @param Socket
 *            Socket used to read
 * @param Options
 *            a timeout can be specified for the socket with the option
 *            [timeout(millis)]. If timeout expires while reading, nothing
 *            is read and nothing is asserted.
 *            The user can insert the option assertZ to change the way the 
 *            received message is asserted
 * @return true if no error happens
 * @throws PrologError if Socket is not bound or it is not a Client_Socket
 */

public boolean aread_from_socket_2(Term Socket, Struct Options) throws PrologError {
	if (Socket.term() instanceof Var) {
		throw PrologError.instantiation_error(prolog, 1);
	}
	if (!((AbstractSocket) Socket.term()).isClientSocket()) { 
		throw PrologError.instantiation_error(prolog, 1);
	} else {
		
		Socket sock = ((Client_Socket) Socket.term()).getSocket();

		
		
		ThreadReader r = readerExist(sock);
		if (r == null) {
			synchronized (this) {
				readers.add(new ThreadReader(sock, prolog));
				r = readers.getLast();
			}
		}

		
		if (r.started())
			return true;

		try {
			sock.setSoTimeout(0); 
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

		LinkedList<Term> list = StructToList(Options); 
		for (Term t : list) { 
			if ("timeout".equals(((Struct) t).name())) {
				int time = Integer.parseInt(((Struct) t).sub(0).toString());
				try {
					sock.setSoTimeout(time); 
				} catch (SocketException e) {
					e.printStackTrace();
					return false;
				}
			}
			
			
			if ("assertZ".equals(((Struct) t).name())) {
				r.assertZ();
			}
		}

		r.startRead();
	}
	return true;
}


/*
 * Transform the Struct s in a LinkedList
 */
private static LinkedList<Term> StructToList(Struct s) {
	Term temp = s;
	LinkedList<Term> list = new LinkedList<>();
	while (".".equals(((Struct) temp).name())) {
		list.add(((Struct) temp).sub(0));
		temp = ((Struct) temp).sub(1);

	}
	return list;
}


/*
 * Check whether a reader associated to socket s already exists
 */
private ThreadReader readerExist(Socket s) {
	return readers.stream().filter(r -> r.compareSocket(s)).findFirst().orElse(null);
}


/*
 * When a goal is solved close all ServerSockets and stop all readers
 */

public void onSolveEnd(){
	for(ServerSocket s:serverSockets){
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	serverSockets= new LinkedList<>();
	for(Socket s:clientSockets){
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	clientSockets = new LinkedList<>();
	for(ThreadReader r:readers)r.stopRead();

}

/*
 * If the user stops the computation call onSolveEnd() to close all sockets and stop all readers
 */

public void onSolveHalt(){
	onSolveEnd();
}

public boolean getAddress_2(Term sock, Term addr) throws PrologError {
	if (sock.term() instanceof Var) {
		throw PrologError.instantiation_error(prolog, 1);
	}
	AbstractSocket abs = (AbstractSocket) sock.term();
	if (abs.isClientSocket()) {
		Socket s = ((Client_Socket) sock.term()).getSocket();
		addr.unify(prolog, new Struct(s.getInetAddress().toString(), new Struct(new NumberTerm.Int(s.getLocalPort()).toString())));
		return true;
	}
	if (abs.isServerSocket()) {
		ServerSocket s = ((Server_Socket) sock.term()).getSocket();
		addr.unify(prolog, new Struct(s.getInetAddress().toString(), new Struct(new NumberTerm.Int(s.getLocalPort()).toString())));
		return true;
	}
	if (abs.isDatagramSocket()) {
		DatagramSocket s = ((Datagram_Socket) sock.term()).getSocket();
		addr.unify(prolog, new Struct(s.getInetAddress().toString(), new Struct(new NumberTerm.Int(s.getLocalPort()).toString())));
		return true;
	}

	return true;
}



/*
 * Definition of thread Reader. It waits until a message is received and assert it.
 */
private static class ThreadReader extends Thread {
	private final Socket socket;				
	private final Prolog mainEngine;
	private boolean assertA;			
	private volatile boolean started;	
	private final Semaphore sem;

	protected ThreadReader(Socket socket, Prolog mainEngine) {
		this.socket = socket;
		this.mainEngine = mainEngine;
		assertA = true;					
		started = false;
		sem = new Semaphore(0);
		this.start();
	}

	
	protected synchronized void startRead() {
		if(started)return;
		started = true;
		sem.release();
	}

	protected boolean started() {
		return started;
	}

	
	
	protected synchronized void stopRead(){
		this.interrupt();
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected synchronized void assertZ() {
		assertA = false;
	}

	protected boolean compareSocket(Socket s) {
		return s.equals(socket);
	}

	
	public void run() {
		while (true) {
			while (!started) {
				try {
					sem.acquire();
					if(this.isInterrupted())return;
				} catch (InterruptedException e1) {
					
					return;
				}
			}
			try {
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				if(this.isInterrupted())return;
				Term msg = (Term) in.readObject();
				if(this.isInterrupted())return;					
				Struct s = (Struct) Term.term(msg.term().toString());
				if (assertA)
					mainEngine.theories.assertA(s, "");
				else
					mainEngine.theories.assertZ(s, true, "", false);
				assertA = true; 
				started = false;
			} catch (IOException e) {
				
				started = false;
				return;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				started = false;
				return;
			}
		}
	}

}




}