//java Server_TCP <porto>
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;
import java.util.*;
public class TCPServer  {
    public static int numero=0;//numero de clientes online

    public static RMI_Interface RMI;
    public static void main(String args[]) {
        if (args.length == 0) {
            System.out.println("insert serverport");
            System.exit(0);
        }
        String port = args[0];

        try{
            TCPServer.RMI = (RMI_Interface) LocateRegistry.getRegistry(7000).lookup("connection");

            int serverPort = Integer.parseInt(port);
            System.out.println("Listening on Port: "+port);
            ServerSocket listenSocket = new ServerSocket(serverPort);
            //loop that accepts clients
            while(true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())="+clientSocket);

                numero ++;
                new Connection(clientSocket, numero);
                System.out.println(numero);
            }

        }catch(IOException e)
        {System.out.println("Listen: " + e.getMessage());}
        catch(Exception ex){

            System.out.println(ex);
        }
    }

    public static void RMI_reconnection(){
        try {
            Thread.sleep(2000);
            TCPServer.RMI = (RMI_Interface) LocateRegistry.getRegistry(7000).lookup("connection");

        } catch (RemoteException e) {
            TCPServer.RMI_reconnection();
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
            TCPServer.RMI_reconnection();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// Thread para tratar da comunicação com um cliente
class Connection  extends Thread implements Serializable {
    PrintWriter out;
    BufferedReader in= null;
    Socket clientSocket;
    int thread_number;
    boolean logged = false;
    User u;

    public Connection (Socket socket, int numero){
        thread_number = numero;
        try{
            clientSocket = socket;
            out =  new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }

    public void sendMessage(String... args) {
        if (args.length % 2 != 0) { // Se o numero de campos não for par
            System.out.println("Erro ao registar campos de envio da mensagem.");
            return;
        }
        LinkedHashMap<String, String> reply = new LinkedHashMap<String, String>();
        for(int i=0;i<args.length;i+=2)
            reply.put(args[i], args[i+1]);
        out.println(reply.toString());
    }

    // Corre 1 vez por cada cliente, fica sempre no while()
    public void run() {
        try{
            while(!logged) {
                try {
                    String data= in.readLine();
                    LinkedHashMap<String, String> hashMap = getData(data);

                    String type = hashMap.get("type");

                    if (type.equals("register")) {
                        register(hashMap);
                    } else if (type.equals("login")) {
                        login(hashMap);
                    } else {
                        sendMessage("type", "status", "logged", "off", "msg", "You must login first!");
                    }
                } catch (Exception e) {
                    out.println("You must follow the protocol.");
                }
            }
            while(logged){

                String data= in.readLine();
                LinkedHashMap<String, String> hashMap = getData(data);

                System.out.println("T["+thread_number + "] Received: ");
                //list elements
                for (String key : hashMap.keySet()) {

                    String value = hashMap.get(key);
                    System.out.println(key + " : " + value);
                }

                this.getType(hashMap);
            }
        }catch(EOFException e){
            System.out.println("The client["+thread_number+"] ended the connection: EOF:" + e);
            TCPServer.numero--;
            try {
                TCPServer.RMI.logoutClient(u.username);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }catch(IOException e){System.out.println("IO:" + e);

        }
    }

    public LinkedHashMap<String, String> getData(String data){
        LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>();
        String [] aux = data.split(",");

        for(String field : aux){
            String [] aux1 = field.trim().split(": ");
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
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            login(hashMap);
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
            out.println(reply.toString());
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            detail_auction(data);
        }

    }

    public void search_auction(LinkedHashMap<String, String> data) {
        try {
            LinkedHashMap<String, String> reply = TCPServer.RMI.search_auction(data);
            out.println(reply.toString());
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            search_auction(data);
        }
    }

    public void my_auctions(LinkedHashMap<String, String> data, String username) {
        try {
            LinkedHashMap<String, String> reply = TCPServer.RMI.my_auctions(data,username);
            out.println(reply.toString());
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
            out.println(reply.toString());
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
            out.println(reply.toString());
            logged = false;
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            Logout(username);
        }
    }

    public void cancelAuction(String username, ){
        LinkedHashMap<String, String> reply = null;
        try {
            reply = TCPServer.RMI.cancelAuction(username);
            out.println(reply.toString());
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            cancelAuction(username);
    }

    //admin

    //TODO change replies

    public void cancelAuction(String username, Long id){
        LinkedHashMap<String, String> reply = null;
        try {
            reply = TCPServer.RMI.cancelAuction(id);
            out.println(reply.toString());
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            cancelAuction(username, id);
        }
    }

    public void banUser(String username, String ban){
        LinkedHashMap<String, String> reply = null;
        try {
            reply = TCPServer.RMI.banUser(ban);
            out.println(reply.toString());
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            banUser(username, ban);
        }
    }

    public void getStats(String username){
        LinkedHashMap<String, String> reply = null;
        try {
            reply = TCPServer.RMI.getStats(username);
            out.println(reply.toString());
        } catch (RemoteException e) {
            //e.printStackTrace();
            TCPServer.RMI_reconnection();
            getStats(username);
        }
    }

    //ve o tipo de operacao e responde ao cliente conforma o tipo de operacao
    public void getType(LinkedHashMap <String, String> data){
        try{
            String username = u.username;
            switch (data.get("type")) {
                case "create_auction":
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
            System.out.println("getype:"+e);
        }
    }
}