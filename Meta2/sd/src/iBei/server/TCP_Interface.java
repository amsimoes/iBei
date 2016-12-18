package iBei.server;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TCP_Interface extends Remote, Serializable{

    public boolean checkUser(String username) throws RemoteException;
    public void sendMsg(String type, String username, String text, int id_leilao, String author) throws RemoteException;

}