import java.util.*;
import java.rmi.*;

public interface RMI_Interface extends Remote {
	public boolean criar_leilao(HashMap<String, String> data) throws java.rmi.RemoteException;
}
