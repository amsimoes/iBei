//java Server_TCP <porto>
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class TCPServer extends UnicastRemoteObject implements TCP_Interface{
    public static int numero=0;//numero de clientes online
    public static RMI_Interface RMI;
    public static int count=0;
    //private static ArrayList<Connection> clients = new ArrayList<Connection>();
    static ArrayList<Connection> connections;

    protected TCPServer() throws RemoteException {
        connections = new ArrayList<Connection>();
    }

    public static void main(String args[]) {
        if (args.length == 0) {
            System.out.println("insert serverport");
            System.exit(0);
        }
        String port = args[0];

        try{
            TCPServer.RMI = (RMI_Interface) LocateRegistry.getRegistry(7000).lookup("connection");
            TCP_Interface tcpserver = new TCPServer();

            int serverPort = Integer.parseInt(port);
            TCPServer.RMI.data(tcpserver);


            InetAddress group = InetAddress.getByName("225.0.0.0");
            MulticastSocket socket = new MulticastSocket(6789);
            socket.joinGroup(group);
            UDPSender udp = new UDPSender(numero, serverPort);

            new Thread(){
                public void run(){
                    try {
                        while(true){
                            udp.setNumero(connections.size());
                            byte[] inBuf = new byte[8*1024];
                            DatagramPacket msgIn = new DatagramPacket(inBuf, inBuf.length);
                            socket.receive(msgIn);
                            String rcv = new String(inBuf, 0, msgIn.getLength());
                            System.out.println("Received from " + rcv);
                            Thread.sleep(29000);
                            //enviar para clientes dados
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();


            System.out.println("Listening on Port: "+port);
            ServerSocket listenSocket = new ServerSocket(serverPort);

            //loop that accepts clients
            while(true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())="+clientSocket);
                numero++;
                Connection c = new Connection(clientSocket, numero);
                //clients.add(c);
                System.out.println(numero);
            }
        } catch(IOException e) {
            System.out.println("Listen: " + e.getMessage());
        } catch(Exception ex){
            System.out.println("NULL ???");
            ex.printStackTrace();
        }
    }

    public static void RMI_reconnection(){
        try {
            Thread.sleep(2000);
            System.out.println(count);
            TCPServer.RMI = (RMI_Interface) LocateRegistry.getRegistry(7000).lookup("connection");
            count = 0;
        } catch (RemoteException | NotBoundException e) {
            count += 2;
            if(count >= 30) {
                System.out.println("RMI Servers with problems...");
            }
            TCPServer.RMI_reconnection();
            //e.printStackTrace();
        } catch (InterruptedException e) {
            count += 2;
            if(count >= 30) {
                System.out.println("RMI Servers with problems...");
            }
            //e.printStackTrace();
        }
    }

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


/*
    public static void serverPush(String username, String message, String id) {
        for(Connection c : clients) {
            if(!c.getUsername().equals(username)) {
                c.sendMessage("type","notification_message","id",id,"user",username,"text",message);
            }
        }
    }*/
}

class UDPSender{
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
            TCPServer.numero--;
            try {
                TCPServer.RMI.logoutClient(u.username);
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
        for(Connection cnt : TCPServer.connections) {
            if (cnt.getUsername().equals(user.getUsername())) {
                TCPServer.connections.remove(cnt);
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
            if(!TCPServer.RMI.login_client(hashMap)) {
                sendMessage("type", "login", "ok", "false");
            } else {
                sendMessage("type", "login", "ok", "true");
                logged = true;
                u = new User(hashMap.get("username"), hashMap.get("password"));
                TCPServer.addConnections(this);
                List <String> notifications = TCPServer.RMI.getNotifications(hashMap.get("username"));
                for(String notification : notifications){
                    this.out.println(notification);
                }
                TCPServer.RMI.cleanNotifications(hashMap.get("username"));
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            login(hashMap);
            //sendMessage("type", "login", "ok", "false");
        }
    }

    public void register(LinkedHashMap<String, String> data){
        try {
            if(!TCPServer.RMI.register_client(data)) {   // Registo falhado, utilizador já existe
                sendMessage("type", "register", "ok", "false");
            } else {    // Registo bem sucedido
                sendMessage("type", "register", "ok", "true");
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            register(data);
        }
    }
    public void create_auction(LinkedHashMap <String, String> data, String username){
        try {
            if(TCPServer.RMI.create_auction(data,username)){
                sendMessage("type", "create_auction", "ok", "true");
            }
            else{
                sendMessage("type", "create_auction","ok","false");

            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            create_auction(data, username);
        }
    }
    public void detail_auction(LinkedHashMap<String, String> data){
        try {
            LinkedHashMap<String, String> reply = TCPServer.RMI.detail_request(data);
            String r = reply.toString().replaceAll("=",":");
            out.println(r.substring(1, r.length() - 1));
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            detail_auction(data);
        }

    }
    public void search_auction(LinkedHashMap<String, String> data) {
        try {
            LinkedHashMap<String, String> reply = TCPServer.RMI.search_auction(data);
            String r = reply.toString().replaceAll("=",":");
            out.println(r.substring(1, r.length() - 1));
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            search_auction(data);
        }
    }
    public void my_auctions(LinkedHashMap<String, String> data, String username) {
        try {
            LinkedHashMap<String, String> reply = TCPServer.RMI.my_auctions(data,username);
            String r = reply.toString().replaceAll("=",":");
            out.println(r.substring(1, r.length() - 1));
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            my_auctions(data, username);
        }


    }
    public void make_bid(LinkedHashMap<String, String> data, String username){
        //falta mandar para os restantes licitadores a notificacao
        try {
            if(TCPServer.RMI.make_bid(data, username)){
                sendMessage("type","bid","ok","true");
            }
            else{
                sendMessage("type","bid","ok","false");
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            make_bid(data, username);
        }
    }
    public void write_message(LinkedHashMap<String, String> data, String username){
        //falta mandar para a notificao para os que escreveram no mural e para o criador do leilao
        try {
            if(TCPServer.RMI.write_message(data, username)){
                sendMessage("type","message","ok","true");
                //TCPServer.serverPush(username, data.get("text"), data.get("id"));
            }
            else{
                sendMessage("type","message","ok","false");
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            write_message(data, username);
        }
    }

    public void edit_auction(LinkedHashMap<String, String> data, String username){
        try {
            if(!TCPServer.RMI.edit_auction(data, username)) {
                sendMessage("type","edit_auction","ok","false");
            }
            else{sendMessage("type","edit_auction","ok","true");}
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            edit_auction(data, username);
        }
    }
    public void OnlineUsers(){
        LinkedHashMap<String, String> reply = null;
        try {
            reply = TCPServer.RMI.listOnlineUsers();
            String r = reply.toString().replaceAll("=",":");
            out.println(r.substring(1, r.length() - 1));
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            OnlineUsers();
        }
    }

    public void Logout(String username){
        LinkedHashMap<String, String> reply = null;
        try {
            reply = TCPServer.RMI.logoutClient(username);
            String r = reply.toString().replaceAll("=",":");
            out.println(r.substring(1, r.length() - 1));
            logged = false;
            this.removeConection(this.u);
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
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
}
