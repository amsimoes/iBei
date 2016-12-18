package sd.model;
import iBei.rmi.*;
import iBei.aux.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Arrays;
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

	public boolean checkFacebook(String username) throws RemoteException {
		Boolean result = server.checkFacebook(username);
		return result;
	}

	public boolean associate(String username, String user_id) throws RemoteException {
		LinkedHashMap<String, String> data = new LinkedHashMap<>();

		data.put("type", "associate");
		data.put("username", username);
		data.put("user_id", user_id);
		Boolean result = server.associateFacebook(data);

		if(!result){
			System.out.println("ERROR Associating facebook");
			return result;
		}

		return true;
	}

	public boolean loginFacebook(String user_id) throws RemoteException {
		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
		data.put("user_id", user_id);
		String username = server.login_facebook(data);
		System.out.println("[ BEAN ] Returned from RMI login_facebook = "+username);
		if(username == null) {
			System.out.println("[loginFacebook] Error logging in with facebook.");
			return false;
		}
		//FALTA GUARDAR O USERNAME !!!
		this.username = username;
		return true;
	}
	
	public boolean login(String username, String password) throws RemoteException {
		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();

		Boolean result;

		data.put("type", "login");
		data.put("username", username);
		data.put("password", password);
		result = server.login_client(data);

		System.out.println("Result do login_client= "+result);
		System.out.println(username+" | "+password);
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


	public int createAuction(String title, String description, String deadline, String code, String amount){
		LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
		data.put("type", "create_auction");
		data.put("code", code);
		data.put("title", title);
		data.put("description", description);
		data.put("deadline", deadline);
		data.put("amount", amount);

		if(title.equals("") || deadline.equals("") || code.equals("") || description.equals("") || amount.equals("")){
			System.out.println("[Create Auction] Missing parameters");
			return 0;
		}

		int res = 0;
		try {
			res = server.create_auction(data, username);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		System.out.println(data);
		return res;
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
	
	public ArrayList<ArrayList<Object>> notificationsMsg() throws RemoteException{
		ArrayList<ArrayList<Object>> data = server.checkMsgNotf_clientsWebSockets();
		System.out.println("tamanho_array"+data.size());
		return data;
	}
	
	public ArrayList<ArrayList<Object>> notificationsBid() throws RemoteException{
		ArrayList<ArrayList<Object>> data = server.checkBidNotf_clientsWebSockets();
		System.out.println("tamanho_array"+data.size());
		return data;
	}
	
	public boolean banUser(String username) throws RemoteException {
		//exemplo de input
		Boolean result = server.banUser(username);
		
		if(!result){
			return result;
		}
		//System.out.println("Encontrou o leilao");
		return result;
	}

	public boolean removeFacebook(String username) throws RemoteException {
		Boolean result = server.removeFacebook(username);
		if(!result){
			System.out.println("Bean - Error deassociating fb account of user:"+ username);
			return result;
		}
		System.out.println();
		return true;
	}
	
	public boolean cancelAuction(long id) throws RemoteException {
		//exemplo de input
		Boolean result = server.cancelAuction(id);
		
		if(!result){
			return result;
		}
		System.out.println("Encontrou o leilao");
		return result;
	}
	
	public User[] getStatsLeiloes() throws RemoteException{
		User[] stats = Arrays.copyOf(server.statsLeiloes(), server.statsLeiloes().length);
		
		return stats;
		
	}
	
	public User[] getStatsVitorias() throws RemoteException{
		User[] stats = Arrays.copyOf(server.statsVitorias(), server.statsVitorias().length);
		
		return stats;
		
	}
	
	public int getStatsLastWeek() throws RemoteException{
		int stats = server.statsLastWeek();
		
		return stats;
		
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
