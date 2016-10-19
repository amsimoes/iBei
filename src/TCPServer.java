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
    //=============================

    public void run() {

        try{
            while(true){
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
            String username = "daniel";//temos que ir buscar isto ao cliente...
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
            }

            else if(data.get("type").equals("detail_auction")){
                Leilao leilao = r.detail_auction(data);

                int i;
                if( leilao != null ){
                    reply.put("type","detail_auction");
                    reply.put("title",leilao.titulo);
                    reply.put("description", leilao.descricao);
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
                        reply.put("messages_"+String.valueOf(i)+"_user", leilao.licitacoes.get(i).get("author"));
                        reply.put("messages_"+String.valueOf(i)+"_text", leilao.licitacoes.get(i).get("bid"));
                    }

                }
                else{
                    reply.put("type","detail_auction");
                    reply.put("ok","false");
                }

                out.println(reply.toString());
            }

            else if(data.get("type").equals("search_auction")){
                ArrayList <Leilao> leiloes = r.search_auction(data);
                int i;
                if(leiloes.size() != 0){
                    reply.put("type","search_auction");
                    reply.put("items_count", String.valueOf(leiloes.size()));
                    for(i=0; i< leiloes.size(); i++){
                        reply.put("items_"+String.valueOf(i)+"_id", String.valueOf(leiloes.get(i).id_leilao));
                        reply.put("items_"+String.valueOf(i)+"_code", String.valueOf(leiloes.get(i).artigoId));
                        reply.put("items_"+String.valueOf(i)+"_title", leiloes.get(i).titulo);
                    }
                }
                else{
                    reply.put("type","search_auction");
                    reply.put("items_count","0");
                }
                out.println(reply.toString());
            }

            else if(data.get("type").equals("my_auctions")){

                ArrayList <Leilao> leiloes = r.my_auctions(data,username);
                int i;
                if(leiloes.size() != 0){
                    reply.put("type","my_auctions");
                    reply.put("items_count", String.valueOf(leiloes.size()));
                    for(i=0; i< leiloes.size(); i++){
                        reply.put("items_"+String.valueOf(i)+"_id", String.valueOf(leiloes.get(i).id_leilao));
                        reply.put("items_"+String.valueOf(i)+"_code", String.valueOf(leiloes.get(i).artigoId));
                        reply.put("items_"+String.valueOf(i)+"_title", leiloes.get(i).titulo);
                    }
                }
                else{
                    reply.put("type","my_auctions");
                    reply.put("items_count","0");
                }
                out.println(reply.toString());
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
            }

            else if(data.get("type").equals("message")){
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

            }

            else{
                System.out.println("Operation not found!");
            }
        }
        catch(Exception e){
            System.out.println("getype:"+e);
        }

    }

}