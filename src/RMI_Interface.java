import java.util.*;
import java.rmi.*;

public interface RMI_Interface extends Remote {

    public boolean edit_auction(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public boolean register_client(LinkedHashMap<String, String> data) throws RemoteException;
    public boolean login_client(LinkedHashMap<String, String> data) throws RemoteException;
    public boolean create_auction(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public Leilao detail_auction(LinkedHashMap<String, String> data) throws java.rmi.RemoteException;
    public LinkedHashMap<String, String> search_auction(LinkedHashMap<String, String> data) throws java.rmi.RemoteException;
    public LinkedHashMap<String, String> my_auctions(LinkedHashMap<String, String> data, String username) throws java.rmi.RemoteException;
    public boolean make_bid(LinkedHashMap<String, String> data, String username) throws java.rmi.RemoteException;
    public boolean write_message(LinkedHashMap<String, String> data, String username) throws java.rmi.RemoteException;
    public LinkedHashMap<String, String> listOnlineUsers() throws RemoteException;
    public LinkedHashMap<String, String> logoutClient(String username) throws RemoteException;

    //admin
    public boolean isAdmin(String username);
    public static boolean cancelAuction ();
    public static boolean banUser (String username);

    //serao precisas?
    public void import_auctions() throws java.rmi.RemoteException;
    public void export_auctions() throws java.rmi.RemoteException;
    public LinkedHashMap<String, String> detail_request(LinkedHashMap<String, String> data) throws RemoteException;
    public String teste() throws RemoteException;
    public void verifica_terminoLeiloes() throws RemoteException;

    }
