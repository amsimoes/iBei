package iBei.server;

import iBei.aux.*;

import java.io.Serializable;
import java.rmi.*;

public interface TCP_Interface extends Remote, Serializable{

    public boolean checkUser(String username) throws RemoteException;
    public void sendMsg(String type, String username, String text, Leilao leilao, String author) throws RemoteException;

}