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

	public boolean create_auction(LinkedHashMap<String, String> data) throws RemoteException{

		int code = 	Integer.parseInt(data.get("code"));
		double amount = Double.parseDouble(data.get("amount"));

		DateFormat d1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		try{
			
			Date date = d1.parse(data.get("deadline"));
			Leilao l = new Leilao(code,data.get("title"),data.get("description"),amount,date);
			
			leiloes.add(l);
			
			System.out.println("Auction created!");

			return true;
		}

		catch(Exception e){
			System.out.println(e);
		}

		return false;

	}

	public Leilao detail_auction(LinkedHashMap<String, String> data){
		long id = 	Long.parseLong(data.get("id"));
		
		int i;
		for(i=0; i<leiloes.size();i++){
			if(leiloes.get(i).id_leilao == id){
				leiloes.get(i).printInfo();
				return leiloes.get(i);
			}
		}
		
		return null;
	}

	public ArrayList <Leilao> search_auction(LinkedHashMap<String, String> data){

		ArrayList <Leilao> leiloes_encontrados = new ArrayList <Leilao>();
		int i;
		for(Leilao leilao : leiloes){
			if(leilao.artigoId == Long.parseLong(data.get("code"))){
				leiloes_encontrados.add(leilao);
			}

		}
		return leiloes_encontrados;

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