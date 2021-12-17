package alice.tuprologx.runtime.tcp;

import alice.tuprolog.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Proxy implements Prolog {

    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream  in;

    public Proxy(String host) throws IOException {
        socket=new Socket(host, Daemon.DEFAULT_PORT);
        out=new ObjectOutputStream(socket.getOutputStream());
        in=new ObjectInputStream(socket.getInputStream());
    }

    public Proxy(String host, int port) throws IOException {
        socket=new Socket(host,port);
        out=new ObjectOutputStream(socket.getOutputStream());
        in=new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void clearTheory() throws IOException {
        out.writeObject(new NetMsg("clearTheory"));
        out.flush();
    }

    @Override
    public Theory getTheory() throws IOException, ClassNotFoundException {
        out.writeObject(new NetMsg("getTheory"));
        out.flush();
        Boolean b=(Boolean)in.readObject();
        if (b){
            return (Theory)in.readObject();
        }
        return null;
    }

    @Override
    public void setTheory(Theory th) throws IOException, ClassNotFoundException, InvalidTheoryException {
        out.writeObject(new NetMsg("setTheory"));
        out.writeObject(th);
        out.flush();
        Boolean b=(Boolean)in.readObject();
        if (!b){
            throw new InvalidTheoryException();
        }
    }

    @Override
    public void addTheory(Theory th) throws IOException, ClassNotFoundException, InvalidTheoryException {
        out.writeObject(new NetMsg("addTheory"));
        out.writeObject(th);
        out.flush();
        Boolean b=(Boolean)in.readObject();
        if (!b){
            throw new InvalidTheoryException();
        }
    }


    @Override
    public Solution solve(String st) throws IOException, ClassNotFoundException, MalformedGoalException {
        out.writeObject(new NetMsg("solveString"));
        out.writeObject(st);
        out.flush();
        Boolean b=(Boolean)in.readObject();
        if (b){
            return (Solution)in.readObject();
        } else {
            throw new MalformedGoalException();
        }
    }

    @Override
    public Solution solve(Term term) throws IOException, ClassNotFoundException, MalformedGoalException {
        out.writeObject(new NetMsg("solveTerm"));
        out.writeObject(term);
        out.flush();
        Boolean b=(Boolean)in.readObject();
        if (b){
            return (Solution)in.readObject();
        } else {
            throw new MalformedGoalException();
        }
    }

    @Override
    public Solution solveNext() throws IOException, ClassNotFoundException, NoSolutionException {
        out.writeObject(new NetMsg("solveNext"));
        out.flush();
        Boolean b=(Boolean)in.readObject();
        if (b){
            return (Solution)in.readObject();
        } else {
            throw new NoSolutionException();
        }
    }

    @Override
    public boolean hasOpenAlternatives() throws IOException, ClassNotFoundException {
        out.writeObject(new NetMsg("hasOpenAlternatives"));
        out.flush();
        return (Boolean)in.readObject();
    }

    @Override
    public void solveHalt() throws IOException {
        out.writeObject(new NetMsg("solveHalt"));
        out.flush();
    }

    @Override
    public void solveEnd() throws IOException {
        out.writeObject(new NetMsg("solveEnd"));
        out.flush();
    }



    @Override
    public void loadLibrary(String st) throws IOException, ClassNotFoundException, InvalidLibraryException {
        out.writeObject(new NetMsg("loadLibrary"));
        out.writeObject(st);
        out.flush();
        Boolean b=(Boolean)in.readObject();
        if (!b){
            throw new InvalidLibraryException();
        }
    }

    @Override
    public void unloadLibrary(String st) throws IOException, ClassNotFoundException, InvalidLibraryException {
        out.writeObject(new NetMsg("unloadLibrary"));
        out.writeObject(st);
        out.flush();
        Boolean b=(Boolean)in.readObject();
        if (!b){
            throw new InvalidLibraryException();
        }
    }
}