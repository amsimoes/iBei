import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.text.*;
import java.net.*;
import java.util.*;


public class RMI_Server extends UnicastRemoteObject implements RMI_Interface{

	ArrayList <Leilao> leiloes;
	public RMI_Server() throws RemoteException {
		super();
		leiloes = new ArrayList <Leilao>();
	}

	public boolean criar_leilao(HashMap<String, String> data) throws RemoteException{

		int code = 	Integer.parseInt(data.get("code"));
		double amount = Double.parseDouble(data.get("amount"));

		DateFormat d1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		try{
			
			Date date = d1.parse(data.get("deadline"));
			Leilao l = new Leilao(code,data.get("title"),data.get("description"),amount,date);
			HashMap<String, Double> lc= new HashMap<String, Double>();
			
			lc.put("licitacao1",100.3);
	      	lc.put("licitacao2",90.9);
			
			l.licitacoes.add(lc);
			
			leiloes.add(l);
			//l.printInfo();
			System.out.println("Auction created!");

			return true;
		}

		catch(Exception e){
			System.out.println(e);
		}

		return false;

	}

	public boolean consulta_leilao(Leilao leilao){
		leilao.printInfo();
		return true;
	}

	public static void main(String args[]) throws MalformedURLException{

		try {
			RMI_Server h = new RMI_Server();
			
			Registry r = LocateRegistry.createRegistry(7000);
			r.rebind("connection", h);

			System.out.println("RMI Server ready.");

		} catch (RemoteException re) {
			System.out.println("Exception in RMI_Server.main: " + re);
		}

	}


}