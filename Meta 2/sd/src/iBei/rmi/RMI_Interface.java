package iBei.rmi;

import iBei.aux.Leilao;
import iBei.aux.User;
import iBei.server.TCP_Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public interface RMI_Interface extends Remote {
    public boolean edit_auction(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public boolean register_client(LinkedHashMap<String, String> data) throws RemoteException;
    public boolean login_client(LinkedHashMap<String, String> data) throws RemoteException;
    public boolean create_auction(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public Leilao detail_auction(LinkedHashMap<String, String> data) throws RemoteException;
    public ArrayList <Leilao> search_auction(LinkedHashMap<String, String> data) throws RemoteException;
    public ArrayList <Leilao> my_auctions(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public Leilao make_bid(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public Leilao write_message(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public List<User> listOnlineUsers() throws RemoteException;
    public boolean logoutClient(String username) throws RemoteException;
    public void addTCP(TCP_Interface tcp) throws RemoteException;
    public String teste() throws RemoteException;
    public void msgNotification(int leilaoId, String usernameSender, int msgId, Connection connection) throws RemoteException;
    public void bidNotification(int leilaoId, String usernameSender, int bidId, Connection connection) throws RemoteException;
    public boolean cancelAuction (long id) throws RemoteException;
    public User [] statsLeiloes() throws RemoteException;
    public User [] statsVitorias() throws RemoteException;
    public int statsLastWeek() throws RemoteException;
    public boolean banUser (String username) throws RemoteException;
    public void checkBidNotf_clients() throws RemoteException;
    public void checkMsgNotf_clients() throws RemoteException;

    }
