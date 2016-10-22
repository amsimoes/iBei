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
    public boolean write_message(LinkedHashMap<String, String> data, String username) throws RemoteException;
    public LinkedHashMap<String, String> listOnlineUsers() throws RemoteException;
    public LinkedHashMap<String, String> logoutClient(String username) throws RemoteException;


    public void import_auctions() throws RemoteException;
    public void export_auctions() throws RemoteException;

    public void export_logged() throws RemoteException;
    public void import_registed() throws RemoteException;
    public void export_registed() throws RemoteException;


    public LinkedHashMap<String, String> detail_request(LinkedHashMap<String, String> data) throws RemoteException;
    public String teste() throws RemoteException;
    public void verifica_terminoLeiloes() throws RemoteException;

    }
