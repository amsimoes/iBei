import java.util.*;
import java.rmi.*;

public interface RMI_Interface extends Remote {
    public boolean registerClient(LinkedHashMap<String, String> data) throws RemoteException;
    public boolean loginClient(LinkedHashMap<String, String> data) throws RemoteException;
    public boolean create_auction(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public Leilao detail_auction(LinkedHashMap<String, String> data) throws RemoteException;
    public ArrayList <Leilao> search_auction(LinkedHashMap<String, String> data) throws RemoteException;
    public ArrayList <Leilao> my_auctions(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public boolean edit_auction(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public boolean make_bid(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public boolean write_message(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public LinkedHashMap<String, String> listOnlineUsers() throws RemoteException;
    public LinkedHashMap<String, String> logoutClient(String username) throws RemoteException;

    // Serao precisas?
    public void import_auctions() throws RemoteException;
    public void export_auctions() throws RemoteException;
    }
