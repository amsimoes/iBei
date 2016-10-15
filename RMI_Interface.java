import java.util.*;
import java.rmi.*;

public interface RMI_Interface extends Remote {
	public boolean create_auction(LinkedHashMap<String, String> data) throws java.rmi.RemoteException;
	public Leilao detail_auction(LinkedHashMap<String, String> data) throws java.rmi.RemoteException;
	public ArrayList <Leilao> search_auction(LinkedHashMap<String, String> data) throws java.rmi.RemoteException;
}
