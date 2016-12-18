package iBei.rmi;

import iBei.server.*;
import iBei.aux.*;

import java.io.Serializable;
import java.util.*;
import java.rmi.*;

public interface RMI_Interface extends Remote {
    public boolean edit_auction(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public boolean register_client(LinkedHashMap<String, String> data) throws RemoteException;
    public boolean login_client(LinkedHashMap<String, String> data) throws RemoteException;
    public boolean create_auction(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public Leilao detail_auction(LinkedHashMap<String, String> data) throws RemoteException;
    public ArrayList <Leilao> search_auction(LinkedHashMap<String, String> data) throws RemoteException;
    public ArrayList <Leilao> my_auctions(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public Leilao make_bid(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public Leilao write_message(LinkedHashMap<String, String> data, String username) throws java.rmi.RemoteException;
    public List<User> listOnlineUsers() throws RemoteException;
    public LinkedHashMap<String, String> logoutClient(String username) throws RemoteException;
    public void addTCP(TCP_Interface tcp) throws RemoteException;
    public List<String> getNotifications(String username) throws RemoteException;
    public void cleanNotifications(String username) throws RemoteException;
    public String teste() throws RemoteException;
    public void msgNotification(Leilao auc, String text, String username) throws RemoteException;
    public void bidNotification(Leilao auc, String amount, String username, String type) throws RemoteException;
    public boolean cancelAuction (long id) throws RemoteException;
    public User [] statsLeiloes() throws RemoteException;
    public User [] statsVitorias() throws RemoteException;
    public int statsLastWeek() throws RemoteException;
    public boolean banUser (String username) throws RemoteException;

    }
