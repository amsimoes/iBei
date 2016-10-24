import java.io.Serializable;
import java.util.*;
import java.rmi.*;

public interface RMI_Interface extends Remote {
    public boolean edit_auction(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public boolean register_client(LinkedHashMap<String, String> data) throws RemoteException;
    public boolean login_client(LinkedHashMap<String, String> data) throws RemoteException;
    public boolean create_auction(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public Leilao detail_auction(LinkedHashMap<String, String> data) throws RemoteException;
    public LinkedHashMap<String, String> search_auction(LinkedHashMap<String, String> data) throws RemoteException;
    public LinkedHashMap<String, String> my_auctions(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public boolean make_bid(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public boolean write_message(LinkedHashMap<String, String> data, String username) throws java.rmi.RemoteException;
    public LinkedHashMap<String, String> listOnlineUsers() throws RemoteException;
    public LinkedHashMap<String, String> logoutClient(String username) throws RemoteException;
    public void data(TCP_Interface tcp) throws RemoteException;
    public List<String> getNotifications(String username) throws RemoteException;
    public void cleanNotifications(String username) throws RemoteException;
    public LinkedHashMap<String, String> detail_request(LinkedHashMap<String, String> data) throws RemoteException;
    public String teste() throws RemoteException;


    }
