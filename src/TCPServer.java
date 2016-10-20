//java Server_TCP <porto>
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;
import java.util.*;

public class TCPServer  {
    public static int numero=0;//numero de clientes online
    public static void main(String args[]) {
        if (args.length == 0) {
            System.out.println("insert serverport");
            System.exit(0);
        }
        String port = args[0];

        try{
            RMI_Interface h = (RMI_Interface) LocateRegistry.getRegistry(7000).lookup("connection");

            int serverPort = Integer.parseInt(port);
            System.out.println("Listening on Port: "+port);
            ServerSocket listenSocket = new ServerSocket(serverPort);
            //loop that accepts clients
            while(true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())="+clientSocket);

                numero ++;
                new Connection(clientSocket, numero, h);
                System.out.println(numero);
            }

        }catch(IOException e)
        {System.out.println("Listen: " + e.getMessage());}
        catch(Exception ex){

            System.out.println(ex);
        }
    }
}

// Thread para tratar da comunicação com um cliente
class Connection  extends Thread implements Serializable {
    PrintWriter out;
    BufferedReader in= null;
    Socket clientSocket;
    int thread_number;
    RMI_Interface r;
    boolean logged = false;
    User u;

    public Connection (Socket socket, int numero, RMI_Interface h){
        thread_number = numero;
        try{
            clientSocket = socket;
            r=h;
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
                    LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>();

                    String data= in.readLine();

                    String [] aux = data.split(",");
                    for(String field : aux){
                        String [] aux1 = field.trim().split(": ");
                        hashMap.put(aux1[0], aux1[1]);
                    }
                    String type = hashMap.get("type");

                    if (type.equals("register")) {
                        if(!r.registerClient(hashMap)) {   // Registo falhado, utilizador já existe
                            sendMessage("type", "register", "ok", "false");
                        } else {    // Registo bem sucedido
                            sendMessage("type", "register", "ok", "true");
                        }
                    } else if (type.equals("login")) {
                        // Funcao para verificar se o user existe
                        if(!r.loginClient(hashMap)) {
                            sendMessage("type", "login", "ok", "false");
                        } else {
                            sendMessage("type", "login", "ok", "true");
                            logged = true;
                            u = new User(hashMap.get("username"), hashMap.get("password"));
                        }
                    } else {
                        sendMessage("type", "status", "logged", "off", "msg", "You must login first!");
                    }
                } catch (Exception e) {
                    LinkedHashMap<String, String> reply = new LinkedHashMap<String, String>();
                    out.println("You must follow the protocol.");
                }
            }
            while(logged){
                //an echo server
                LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>();

                String data= in.readLine();

                String [] aux = data.split(",");

                for(String field : aux){
                    String [] aux1 = field.trim().split(": ");
                    hashMap.put(aux1[0], aux1[1]);
                }

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
        }catch(IOException e){System.out.println("IO:" + e);
        }
    }
    //ve o tipo de operacao e responde ao cliente conforma o tipo de operacao
    public void getType(LinkedHashMap <String, String> data){
        try{
            String username = u.username;
            LinkedHashMap<String, String> reply = new LinkedHashMap<String, String>();

            //se for do tipo criar_leilao...
            if(data.get("type").equals("create_auction")){
                if(r.create_auction(data,username)){
                    reply.put("type","create_auction");
                    reply.put("ok","true");
                }
                else{
                    reply.put("type","create_auction");
                    reply.put("ok","false");
                }

                out.println(reply.toString());
            } else if (data.get("type").equals("login")) {   // JA ESTA LOGGADO... false
                sendMessage("type", "login", "ok", "false");
            } else if (data.get("type").equals("register")) {    // JA ESTA REGISTADO... false
                sendMessage("type", "register", "ok", "false");
            } else if (data.get("type").equals("detail_auction")) {
                Leilao leilao = r.detail_auction(data);

                int i;
                if( leilao != null ){
                    reply.put("type","detail_auction");
                    String titulos = "";
                    for(String titulo: leilao.titulo){
                        titulos += titulo+", ";
                    }
                    reply.put("title",titulos.substring(0, titulos.length()-2));
                    String descricoes = "";
                    for(String descricao:leilao.descricao){
                        descricoes += descricao+", ";
                    }
                    reply.put("title",descricoes.substring(0, descricoes.length()-2));

                    reply.put("deadline",leilao.data_termino.toString());
                    reply.put("messages_count",String.valueOf(leilao.mensagens.size()));
                    //mandar mensagens
                    for(i=0; i< leilao.mensagens.size();i++){
                        reply.put("messages_"+String.valueOf(i)+"_user", leilao.mensagens.get(i).get("author"));
                        reply.put("messages_"+String.valueOf(i)+"_text", leilao.mensagens.get(i).get("message"));
                    }

                    reply.put("bids_count",String.valueOf(leilao.licitacoes.size()));
                    //mandar licitacoes
                    for(i=0; i< leilao.licitacoes.size();i++){
                        reply.put("bid_"+String.valueOf(i)+"_author", leilao.licitacoes.get(i).get("author"));
                        reply.put("bid_"+String.valueOf(i)+"_value", leilao.licitacoes.get(i).get("bid"));
                    }

                }
                else{
                    reply.put("type","detail_auction");
                    reply.put("ok","false");
                }

                out.println(reply.toString());
            } else if(data.get("type").equals("search_auction")){
                ArrayList <Leilao> leiloes = r.search_auction(data);
                int i;
                if(leiloes.size() != 0){
                    reply.put("type","search_auction");
                    reply.put("items_count", String.valueOf(leiloes.size()));
                    for(i=0; i< leiloes.size(); i++){
                        reply.put("items_"+String.valueOf(i)+"_id", String.valueOf(leiloes.get(i).id_leilao));
                        reply.put("items_"+String.valueOf(i)+"_code", String.valueOf(leiloes.get(i).artigoId));
                        reply.put("items_"+String.valueOf(i)+"_title", leiloes.get(i).titulo.get(leiloes.get(i).titulo.size()-1));//so mostra o titulo mais recente
                    }
                }
                else{
                    reply.put("type","search_auction");
                    reply.put("items_count","0");
                }
                out.println(reply.toString());
            } else if(data.get("type").equals("my_auctions")){

                ArrayList <Leilao> leiloes = r.my_auctions(data,username);
                int i;
                if(leiloes.size() != 0){
                    reply.put("type","my_auctions");
                    reply.put("items_count", String.valueOf(leiloes.size()));
                    for(i=0; i< leiloes.size(); i++){
                        reply.put("items_"+String.valueOf(i)+"_id", String.valueOf(leiloes.get(i).id_leilao));
                        reply.put("items_"+String.valueOf(i)+"_code", String.valueOf(leiloes.get(i).artigoId));
                        reply.put("items_"+String.valueOf(i)+"_title", leiloes.get(i).titulo.get(leiloes.get(i).titulo.size()-1));//so mostra o titulo mais recente
                    }
                }
                else{
                    reply.put("type","my_auctions");
                    reply.put("items_count","0");
                }
                out.println(reply.toString());
            }

            else if(data.get("type").equals("edit_auction")) {
                if(!r.edit_auction(data, username)) {
                    sendMessage("type","edit_auction","ok","false");
                } else {
                    sendMessage("type","edit_auction","ok","true");
                }
            }

            else if(data.get("type").equals("bid")){
                //falta mandar para os restantes licitadores a notificacao
                if(r.make_bid(data, username)){
                    reply.put("type","bid");
                    reply.put("ok","true");
                }
                else{
                    reply.put("type","bid");
                    reply.put("ok","false");
                }
                out.println(reply.toString());
            } else if(data.get("type").equals("message")){
                //falta mandar para a notificao para os que escreveram no mural e para o criador do leilao
                if(r.write_message(data, username)){
                    reply.put("type","message");
                    reply.put("ok","true");
                }
                else{
                    reply.put("type","message");
                    reply.put("ok","false");
                }
                out.println(reply.toString());
            } else if(data.get("type").equals("online_users")) {
                reply = r.listOnlineUsers();
                out.println(reply.toString());
            } else if(data.get("type").equals("logout")) {
                reply = r.logoutClient(username);
                out.println(reply.toString());
                logged = false;
            } else {
                System.out.println("Operation not found!");
            }
        } catch (Exception e) {
            System.out.println("getype:"+e);
        }
    }
}