package sd.model;
import iBei.rmi.*;
import iBei.aux.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class Bean{
private RMI_Interface server;
public Leilao leilao;
public String loginMsg;
public String username;
public String password;
public ArrayList <Leilao> leiloes;
	public Bean() {
		try {
			server = (RMI_Interface) LocateRegistry.getRegistry("localhost",7000).lookup("ibei");
			leiloes = new ArrayList <Leilao> ();
			loginMsg="";
		}
		catch(NotBoundException|RemoteException e) {
			e.printStackTrace(); // what happens *after* we reach this line?
		}
	}
	public String LoginMessage(){
		return this.loginMsg;
	}
	public boolean register(String username, String password) throws RemoteException {
		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
		//exemplo de input
		data.put("type", "register");
		data.put("username", username);
		data.put("password", password);
		Boolean result = server.register_client(data);
		
		if(!result){
			return result;
		}
		this.username = username;
		loginMsg="";
		return result;
	}
	
	
	public boolean login(String username, String password) throws RemoteException {
		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
		//exemplo de input
		data.put("type", "login");
		data.put("username", username);
		data.put("password", password);
		Boolean result = server.login_client(data);
		
		if(!result){
			System.out.println("Login: Wrong username or password");
			loginMsg = "Wrong username or password.";
			return result;
		}
		//System.out.println("Encontrou o leilao");
		this.username = username;
		loginMsg="";
		return result;
	}
	
	public boolean logout(){
		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
		data.put("type", "logout");
		boolean result = false;
		try {
			result = server.logoutClient(this.username);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}


	public boolean createAuction(String title, String description, String deadline, String code, String amount){
		boolean result = false;
		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
		data.put("type", "create_auction");
		data.put("code", code);
		data.put("title", title);
		data.put("description", description);
		data.put("deadline", deadline);
		data.put("amount", amount);

		if(title.equals("") || deadline.equals("") || code.equals("") || description.equals("") || amount.equals("")){
			System.out.println("missing parameters");
			return false;
		}
		
		try {
			result= server.create_auction(data,username);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(result);
		System.out.println(data);
		return result;
	}
	
	public Leilao bidAuction(String id, String amount){
		Leilao leilao = null;
		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
		data.put("type", "bid");
		
		if(!id.equals("") && !amount.equals("")){
			data.put("id", id);
			data.put("amount", amount);
		}
		else
			return leilao;
		
		
		try {
			leilao = server.make_bid(data, this.username);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return leilao;
	}

	public Leilao msgAuction(String id, String text){
		Leilao leilao = null;
		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
		data.put("type", "message");
		
		if(!id.equals("") && !text.equals("")){
			data.put("id", id);
			data.put("text", text);
		}
		else
			return leilao;
		
		
		try {
			leilao = server.write_message(data, this.username);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return leilao;
	}
	
	public boolean editAuction(String id, String title, String description, String deadline){
		boolean result = false;
		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
		data.put("type", "edit_auction");
		
		if(!id.equals(""))
			data.put("id", id);
		else
			return false;
		if(!title.equals("")){
			System.out.println("title added");
			data.put("title", title);
		}
		if(!description.equals("")){
			System.out.println("description added");
			data.put("description", description);
		}
		if(!deadline.equals("")){
			System.out.println("deadline added");
			data.put("deadline", deadline);
		}
		
		try {
			result= server.edit_auction(data,username);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(result);
		System.out.println(data);
		return result;
	}
	
	public Leilao detailAuction(String id) throws RemoteException {
		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
		//exemplo de input
		data.put("type", "detail_auction");
		data.put("id", id);
		leilao = server.detail_auction(data);
		
		if(leilao == null){
			System.out.println("leilao null!!!");
			return null;
		}
		//System.out.println("Encontrou o leilao");
		return leilao;
	}
	
	public ArrayList <Leilao> myAuctions() throws RemoteException{
		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
		//exemplo de input
		data.put("type", "my_auctions");
		//System.out.println(this.username);
		leiloes = server.my_auctions(data, this.username);
		
		//System.out.println(leiloes.size());
		//System.out.println(leiloes.get(0).mensagens);
		//System.out.println(leiloes.get(0).licitacoes);
		return leiloes;
		
	}
	
	public ArrayList <Leilao> searchAuction(String code) throws RemoteException{
		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
		//exemplo de input
		data.put("type", "search_auction");
		data.put("code", code);
		leiloes = server.search_auction(data);
		
		return leiloes;
		
	}
	public List <User> OnlineUsers() throws RemoteException{
		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
		List <User> users = new ArrayList<User>();
		data.put("type","online_users");
		users = server.listOnlineUsers();
		if(users != null)
			return users;
		
		return null;
	}
	public void setUsername(String username){
		this.username = username;
	}
	public void setPassword(String password){
		this.password = password;
	}
	
	public Leilao getLeilao(){
		return leilao;
	}
	
	public ArrayList getLeiloes(){
		return leiloes;
	}
	public String getUsername(){
		return username;
	}

}
