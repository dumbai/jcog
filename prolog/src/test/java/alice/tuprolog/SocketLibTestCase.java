package alice.tuprolog;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Eleonora Cau
 *
 */

@Disabled
class SocketLibTestCase {
	
	private final Prolog engine = new Prolog("alice.tuprolog.lib.SocketLibrary", "alice.tuprolog.lib.ThreadLibrary");
	String theory;

	@Test
	void test_server_write() throws InvalidTheoryException, MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		
		String theory =
				"""
						server(Y) :- thread_create(ID1, Y).
						doServer(S) :- tcp_socket_server_open('127.0.0.1:4445', S, []),tcp_socket_server_accept(S, '127.0.0.1:4445', ClientSock),write_to_socket(ClientSock, 'msg inviato dal server'),thread_sleep(1),mutex_lock('mutex'),tcp_socket_server_close(S),mutex_unlock('mutex').
						client(X):- thread_create(ID2,X),thread_read(ID2,X).
						doClient(Sock, Msg) :- tcp_socket_client_open('127.0.0.1:4445',Sock),mutex_lock('mutex'),read_from_socket(Sock, Msg, []),mutex_unlock('mutex').""";
		
		engine.setTheory(new Theory(theory));
		
		Solution result = engine.solve("server(doServer(SS)), client(doClient(CS,Msg)).");
		Assertions.assertTrue(result.isSuccess());

		/*Var clientSock = (Var) result.getTerm("CS");	
		System.out.println("[SocketLibTest] Client Socket: "+ clientSock);
		
		Var serverSock = (Var) result.getTerm("SS");	
		System.out.println("[SocketLibTest] Server Socket: "+ serverSock);*/
		
		Struct msg = (Struct) result.getTerm("Msg");	
		Assertions.assertEquals(Term.term("'msg inviato dal server'"), msg);
	
	}
	
	@Test
	void test_client_write() throws InvalidTheoryException, MalformedGoalException, NoSolutionException, Var.UnknownVarException {
		String theory =
				"""
						server(ID1):- thread_create(ID1, doServer(SS, Msg)).\s
						doServer(S, Msg) :- tcp_socket_server_open('127.0.0.1:4446', S, []), tcp_socket_server_accept(S, '127.0.0.1:4446', ClientSock), mutex_lock('mutex'), read_from_socket(ClientSock, Msg, []), mutex_unlock('mutex'), tcp_socket_server_close(S).
						client(X):- thread_create(ID2,X), thread_read(ID2,X).
						doClient(Sock) :- tcp_socket_client_open('127.0.0.1:4446',Sock),  write_to_socket(Sock, 'msg inviato dal client'), thread_sleep(1).
						read(ID1,Y):- thread_read(ID1,Y).
						""";
		engine.setTheory(new Theory(theory));
		
		Solution result = engine.solve("server(ID1), client(doClient(CS)), read(ID1,doServer(SS,Msg)).");
		Assertions.assertTrue(result.isSuccess());
		
		/*Var clientSock = (Var) result.getTerm("CS");	
		System.out.println("[SocketLibTest] Client Socket: "+ clientSock);
		
		Var serverSock = (Var) result.getTerm("SS");	
		System.out.println("[SocketLibTest] Server Socket: "+ serverSock);*/
		
		Struct msg = (Struct) result.getTerm("Msg");	
		Assertions.assertEquals(Term.term("'msg inviato dal client'"), msg);
	}
}

