//java Server_TCP <porto>
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class AdminCLient extends UnicastRemoteObject /*implements TCP_Interface*/{
    //public static int numero=0;//numero de clientes online
    public static RMI_Interface RMI;
    public static int count=0;
    //private static ArrayList<Connection> clients = new ArrayList<Connection>();
    //static ArrayList<Connection> connections;

    protected AdminCLient() throws RemoteException {
        //connections = new ArrayList<Connection>();
    }

    public static void main(String args[]) {
        if (args.length == 0) {
            System.out.println("insert serverport");
            System.exit(0);
        }
        String port = args[0];

        try{
            AdminCLient.RMI = (RMI_Interface) LocateRegistry.getRegistry(7000).lookup("connection");
            TCP_Interface AdminCLient = new AdminCLient();

            int serverPort = Integer.parseInt(port);
            AdminCLient.RMI.data(AdminCLient);


            InetAddress group = InetAddress.getByName("225.0.0.0");
            MulticastSocket socket = new MulticastSocket(6789);
            socket.joinGroup(group);
            //UDPSender udp = new UDPSender(numero, serverPort);

            //HashMap <Integer, Integer> info = new HashMap<Integer, Integer>();
            /*new Thread(){
                public void run(){

                    try {
                        while(true){
                            //udp.setNumero(connections.size());
                            byte[] inBuf = new byte[8*1024];
                            DatagramPacket msgIn = new DatagramPacket(inBuf, inBuf.length);
                            socket.receive(msgIn);
                            String rcv = new String(inBuf, 0, msgIn.getLength());
                            checkMsg(rcv, info);
                            System.out.println("Received from " + rcv);
                            //enviar para clientes dados
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            new Thread(){
                public void run(){
                    try {
                        while (true) {
                            sendClients(info);
                            Thread.sleep(60000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();*/


            /*System.out.println("Listening on Port: "+port);
            ServerSocket listenSocket = new ServerSocket(serverPort);

            //loop that accepts clients
            while(true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())="+clientSocket);
                numero++;
                Connection c = new Connection(clientSocket, numero);
                //clients.add(c);
                System.out.println(numero);
            }*/
        } catch(IOException e) {
            System.out.println("Listen: " + e.getMessage());
        } catch(Exception ex){
            System.out.println("NULL ???");
            ex.printStackTrace();
        }
    }

    private boolean cancelAuction(int id){
        try {
            if(AdminCLient.RMI.cancelAuction(id)){
            	System.out.println("Leilão cancelado com sucesso.");
            }else{
            	System.out.println("Não existe nenhum leilão com ID: "+id+".");
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminCLient.RMI_reconnection();
            cancelAuction(id);
        }
    }

    private boolean banUser(String username){
    	try {
            if(AdminCLient.RMI.banUser(username)){
            	System.out.println("Utlizador "+username+" banido com sucesso.");
            }else{
            	System.out.println("Não existe nenhum utilizador "+username+".");
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminCLient.RMI_reconnection();
            banuser(username);
        }
    }

    private boolean getStats(){
    	try {
            if(AdminCLient.RMI.getStats()){
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminCLient.RMI_reconnection();
            getStats();
        }
    } 

    public long getItemID(String data){
        String [] aux = data.split(",");

        for(String field : aux){
            String [] aux1 = field.trim().split(":", 2);
            aux1[0] = aux1[0].trim();
            aux1[1] = aux1[1].trim();
            if (aux1[0].equals("items_0_id")){
                return Long.parseLong(aux1[1]);
            }
        }
        return -1;
    }

    private boolean testTCP(String... args){
    	//TODO add tcp client code
        Socket socket;
        PrintWriter outToServer;
        BufferedReader inFromServer = null;
        try {
            // connect to the specified address:port (default is localhost:12345)
            if(args.length == 2)
                socket = new Socket(args[0], Integer.parseInt(args[1]));
            else
                socket = new Socket("localhost", 12345);

            //test values
            long code=(long)(Random.nextDouble()*2^62);
            long id;
            String title = "Test auction please don't bid";
            String description = "The unseen auction is the deadliest";
            String deadline = "3000-09-19 16:20";
            int amount=666;
            int count=0;

            // create streams for writing to and reading from the socket
            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToServer = new PrintWriter(socket.getOutputStream(), true);
           
           	//login
            outToServer.println("type: login, username: admin, password: 123");
            if(!inFromServer.readLine().equals("type: login, ok: true")){
            	return false;
            }

            //testing for equal codes
            outToServer.println("type: search_auction, code: "+code);
            while(!inFromServer.readLine().equals("type: search_auction , items_count: 0")){
            	count++;
            	if (count>10){
            		return false;
            	}
            	code=(long)(Random.nextDouble()*2^62);
            }

            //creating auction
            outToServer.println("type: create_auction, code: "+code+", title: "+title+", description: "+description+", deadline: "+deadline+", amount: "+amount);
            if(!inFromServer.readLine().equals("type: create_auction, ok: true")){
            	return false;
            }

            //getting id
            outToServer.println("type: search_auction, code: "+code);
           	String temp=inFromServer.readLine();
            id=getItemID(temp);
            if ((id==-1)||(!temp.equals("type: search_auction, items_count: 1, items_0_id: "+id+", items_0_code: "+code+", items_0_title: "+title))){
            	return false;
            }

            //checkign details
            outToServer.println("type: detail_auction, id: "+id);
            if(!inFromServer.readLine().equals("type: detail_auction, title: "+title+", description: "+description+", deadline: "+deadline+" , messages_count: 0, bids_count : 0")){
            	return false;
            }

            //biding
            outToServer.println("type: bid, id: "+id+", amount: "+amount);
            if(!inFromServer.readLine().equals("type: bid, ok: true")){
            	return false;
            }

            //checking details
            outToServer.println("type: detail_auction, id: "+id);
            if(!inFromServer.readLine().equals("type: detail_auction, title: "+title+", description: "+description+", deadline: "+deadline+", messages_count: 0, bids_count: 1")){
            	return false;
            }
            return true;

        } catch (IOException e) {
            if(inFromServer == null)
                System.out.println("\nUsage: java TCPClient <host> <port>\n");
            System.out.println(e.getMessage());
        } finally {
            try { inFromServer.close(); } catch (Exception e) {}
        }
    } 

    static public void menu(){
    	int slt;
    	Scanner scan = new Scanner(System.in);
    	do{
    		System.out.println("Selecione uma das seguintes opções:\n1.Cancelar leilão\n2.Banir utilizador\n3.Consultar estatísticas\n4.Testar servidor TCP\n0.Sair\n");
    		slt=scan.nextInt();
    		switch(slt){
    			case 1:
    				System.out.print("ID do leião: ");
    				int id = scan.nextLong();
    				cancelAuction();
    				break;
    			case 2:
	    			System.out.print("Nome de utilizador: ");
			    	String username = scan.readLine();
    				banUser(username);
    				break;
    			case 3:
    				System.out.print("TCP Server: ");
    				//TODO scan tcp server info
    				getStats();
    				break;
    			case 4:
    				testTCP();
    				break;
    			case 0:
    				break;
    			default:
    				System.out.println("Seleção inválida.\n");
    		}
    	}while(slt!=0);
    }

    /*static public void checkMsg(String str, HashMap<Integer, Integer> info){
        String [] fields = str.split(":");
        int i=0;
        for(i=0; i< fields.length;i++){
            fields[i] = fields[i].trim();
        }
        info.put(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));


    }
    */
    /*static public void sendClients(HashMap<Integer, Integer> info){
        int i=0;
        String text = "type: notification_load, server_list: "+info.size();
        for (HashMap.Entry<Integer, Integer> entry : info.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            text += " server_"+i+"_hostname: localhost, server_"+i+"_port: " + key+", server_"+i+"_load: "+value;
            i++;

        }
        for(Connection cnt : connections){
            System.out.println(cnt.getUsername());
            cnt.getOut().println(text);
        }
    }
	*/




    public static void RMI_reconnection(){
        try {
            Thread.sleep(2000);
            System.out.println(count);
            AdminCLient.RMI = (RMI_Interface) LocateRegistry.getRegistry(7000).lookup("connection");
            count = 0;
        } catch (RemoteException | NotBoundException e) {
            count += 2;
            if(count >= 30) {
                System.out.println("RMI Servers with problems...");
            }
            AdminCLient.RMI_reconnection();
            //e.printStackTrace();
        } catch (InterruptedException e) {
            count += 2;
            if(count >= 30) {
                System.out.println("RMI Servers with problems...");
            }
            //e.printStackTrace();
        }
    }

    /*
    public void sendMsg(String type, String username, String text, Leilao leilao, String author) throws RemoteException{
        for(Connection ctn : connections){
            if(ctn.getUsername().equals(username))
                ctn.sendMessage("type",type,"id",String.valueOf(leilao.id_leilao),"user",author,"text",text);
        }
    }

    public boolean checkUser(String username) throws RemoteException{
        for(Connection cnt : connections){
            if(cnt.getUsername().equals(username))
                return true;
        }
        return false;
    }


    public static void addConnections(Connection cnt){
        connections.add(cnt);
    }
	*/


}

/*class UDPSender{
    private int numero;
    private int port;
    public UDPSender(int numero, int tcpPort){

        this.numero = numero;
        this.port = tcpPort;
        try {
            DatagramSocket socket = new DatagramSocket();


            new Thread(){
                public void run() {
                    try {
                        InputStreamReader input = new InputStreamReader(System.in);
                        BufferedReader reader = new BufferedReader(input);
                        while(true) {
                            InetAddress group = InetAddress.getByName("225.0.0.0");
                            String reply = String.valueOf(tcpPort)+": "+String.valueOf(getNumero());
                            byte[] buf =  reply.getBytes();
                            DatagramPacket msgOut = new DatagramPacket(buf, buf.length, group, 6789);
                            socket.send(msgOut);
                            Thread.sleep(30000);
                        }
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                    //while(true){
                    //}
                }
            }.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }
}
*/



/*
// Thread para tratar da comunicação com um cliente
class Connection  extends Thread implements Serializable {
    private PrintWriter out;
    private BufferedReader in= null;
    //private Socket clientSocket;
    private int thread_number;
    private boolean logged = false;
    private User u;

    public Connection (Socket socket, int numero) {
        thread_number = numero;
        try{
            out =  new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.start();
        } catch(IOException e){
            System.out.println("Connection:" + e.getMessage());
        }
    }

    public boolean isLogged() {
        return logged;
    }
    public String getUsername() {
        return u.username;
    }
    public User getUser() {
        return u;
    }

    public void sendMessage(String... args) {
        if (args.length % 2 != 0) { // Se o numero de campos não for par
            System.out.println("Erro ao registar campos de envio da mensagem.");
            return;
        }
        LinkedHashMap<String, String> reply = new LinkedHashMap<String, String>();
        for(int i=0;i<args.length;i+=2)
            reply.put(args[i], args[i+1]);
        String r = reply.toString().replaceAll("=",":");
        out.println(r.substring(1, r.length() - 1));
    }

    // Corre 1 vez por cada cliente, fica sempre no while()
    public void run() {
        try{
            while(true) {
                if(!logged) {
                    //System.out.println("NAO ESTA LOGGADO...");
                    try {
                        String data= in.readLine();
                        LinkedHashMap<String, String> hashMap = parseData(data);

                        String type = hashMap.get("type");

                        if (type.equals("register")) {
                            register(hashMap);
                        } else if (type.equals("login")) {
                            System.out.println("LOGGING IN...");
                            login(hashMap);
                        } else {
                            sendMessage("type", "status", "logged", "off", "msg", "You must login first!");
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                        out.println("You must follow the protocol.");
                    } catch (NullPointerException e) {
                        System.out.println("Desligado a bruta.");
                        this.removeConection(u);
                        break;
                    }
                } else {
                    String data= in.readLine();
                    LinkedHashMap<String, String> hashMap = parseData(data);

                    System.out.println("T["+thread_number + "] Received: ");
                    //list elements
                    for (String key : hashMap.keySet()) {

                        String value = hashMap.get(key);
                        System.out.println(key + " : " + value);
                    }

                    this.getType(hashMap);
                    System.out.println("loggado: "+logged);
                }
            }
        } catch(EOFException | NullPointerException e){
            System.out.println("The client["+thread_number+"] ended the connection: EOF:" + e);
            AdminCLient.numero--;
            try {
                AdminCLient.RMI.logoutClient(u.username);
                System.out.println("User "+u.username+" desligado a bruta.");
                this.removeConection(u);
            } catch (RemoteException e1) {
                //e1.printStackTrace();
                System.out.println("Erro ao desconnectar a forca o user: "+u.username);
            }
        } catch(IOException e) {
            System.out.println("IO:" + e);
        }
    }

    public boolean removeConection(User user){
        for(Connection cnt : AdminCLient.connections) {
            if (cnt.getUsername().equals(user.getUsername())) {
                AdminCLient.connections.remove(cnt);
                return true;
            }
        }
        return false;
    }

    public LinkedHashMap<String, String> parseData(String data){
        LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>();    // {type: login, type : login}
        String [] aux = data.split(",");

        for(String field : aux){
            String [] aux1 = field.trim().split(":", 2);    // 2, pq hora:minutos
            aux1[0] = aux1[0].trim();
            aux1[1] = aux1[1].trim();
            hashMap.put(aux1[0], aux1[1]);
        }
        return hashMap;
    }

    public void login(LinkedHashMap<String, String> hashMap){
        // Funcao para verificar se o user existe
        try {
            if(!AdminCLient.RMI.login_client(hashMap)) {
                sendMessage("type", "login", "ok", "false");
            } else {
                sendMessage("type", "login", "ok", "true");
                logged = true;
                u = new User(hashMap.get("username"), hashMap.get("password"));
                AdminCLient.addConnections(this);
                List <String> notifications = AdminCLient.RMI.getNotifications(hashMap.get("username"));
                for(String notification : notifications){
                    this.out.println(notification);
                }
                AdminCLient.RMI.cleanNotifications(hashMap.get("username"));
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminCLient.RMI_reconnection();
            login(hashMap);
            //sendMessage("type", "login", "ok", "false");
        }
    }

    public void register(LinkedHashMap<String, String> data){
        try {
            if(!AdminCLient.RMI.register_client(data)) {   // Registo falhado, utilizador já existe
                sendMessage("type", "register", "ok", "false");
            } else {    // Registo bem sucedido
                sendMessage("type", "register", "ok", "true");
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminCLient.RMI_reconnection();
            register(data);
        }
    }
    public void create_auction(LinkedHashMap <String, String> data, String username){
        try {
            if(AdminCLient.RMI.create_auction(data,username)){
                sendMessage("type", "create_auction", "ok", "true");
            }
            else{
                sendMessage("type", "create_auction","ok","false");
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminCLient.RMI_reconnection();
            create_auction(data, username);
        }
    }
    public void detail_auction(LinkedHashMap<String, String> data){
        try {
            LinkedHashMap<String, String> reply = AdminCLient.RMI.detail_request(data);
            String r = reply.toString().replaceAll("=",":");
            out.println(r.substring(1, r.length() - 1));
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminCLient.RMI_reconnection();
            detail_auction(data);
        }

    }
    public void search_auction(LinkedHashMap<String, String> data) {
        try {
            LinkedHashMap<String, String> reply = AdminCLient.RMI.search_auction(data);
            String r = reply.toString().replaceAll("=",":");
            out.println(r.substring(1, r.length() - 1));
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminCLient.RMI_reconnection();
            search_auction(data);
        }
    }
    public void my_auctions(LinkedHashMap<String, String> data, String username) {
        try {
            LinkedHashMap<String, String> reply = AdminCLient.RMI.my_auctions(data,username);
            String r = reply.toString().replaceAll("=",":");
            out.println(r.substring(1, r.length() - 1));
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminCLient.RMI_reconnection();
            my_auctions(data, username);
        }


    }
    public void make_bid(LinkedHashMap<String, String> data, String username){
        //falta mandar para os restantes licitadores a notificacao
        try {
            if(AdminCLient.RMI.make_bid(data, username)){
                sendMessage("type","bid","ok","true");
            }
            else{
                sendMessage("type","bid","ok","false");
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminCLient.RMI_reconnection();
            make_bid(data, username);
        }
    }
    public void write_message(LinkedHashMap<String, String> data, String username){
        //falta mandar para a notificao para os que escreveram no mural e para o criador do leilao
        try {
            if(AdminCLient.RMI.write_message(data, username)){
                sendMessage("type","message","ok","true");
                //AdminCLient.serverPush(username, data.get("text"), data.get("id"));
            }
            else{
                sendMessage("type","message","ok","false");
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminCLient.RMI_reconnection();
            write_message(data, username);
        }
    }

    public void edit_auction(LinkedHashMap<String, String> data, String username){
        try {
            if(!AdminCLient.RMI.edit_auction(data, username)) {
                sendMessage("type","edit_auction","ok","false");
            }
            else{sendMessage("type","edit_auction","ok","true");}
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminCLient.RMI_reconnection();
            edit_auction(data, username);
        }
    }
    public void OnlineUsers(){
        LinkedHashMap<String, String> reply = null;
        try {
            reply = AdminCLient.RMI.listOnlineUsers();
            String r = reply.toString().replaceAll("=",":");
            out.println(r.substring(1, r.length() - 1));
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminCLient.RMI_reconnection();
            OnlineUsers();
        }
    }

    public void Logout(String username){
        LinkedHashMap<String, String> reply = null;
        try {
            reply = AdminCLient.RMI.logoutClient(username);
            String r = reply.toString().replaceAll("=",":");
            out.println(r.substring(1, r.length() - 1));
            logged = false;
            this.removeConection(this.u);
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminCLient.RMI_reconnection();
            Logout(username);
        }
    }

    // * ADMIN *
    //TODO change replies

    //ve o tipo de operacao e responde ao cliente conforma o tipo de operacao
    public void getType(LinkedHashMap <String, String> data){
        try{
            String username = u.username;
            switch (data.get("type")) {
                case "create_auction":
                    System.out.println(data);
                    create_auction(data, username);
                    break;
                case "login":    // JA ESTA LOGGADO... false
                    sendMessage("type", "login", "ok", "false");
                    break;
                case "register":    // JA ESTA REGISTADO... false
                    sendMessage("type", "register", "ok", "false");
                    break;
                case "detail_auction":
                    detail_auction(data);
                    break;
                case "search_auction":
                    search_auction(data);
                    break;
                case "my_auctions":
                    my_auctions(data, username);
                    break;
                case "bid":
                    make_bid(data, username);
                    break;
                case "message":
                    write_message(data, username);
                    break;
                case "edit_auction":
                    edit_auction(data, username);
                    break;
                case "online_users":
                    OnlineUsers();
                    break;
                case "logout":
                    Logout(username);
                    break;
                default:
                    System.out.println("Operation not found!");
                    break;
                }

        } catch (Exception e) {
            System.out.println("getType:"+e);
            e.printStackTrace();
        }
    }

    public PrintWriter getOut() {
        return out;
    }
}
*/