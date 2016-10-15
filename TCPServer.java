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
        catch(Exception ex)
        {System.out.println(ex);}
        
        
    }

}

// Thread para tratar da comunicação com um cliente
class Connection  extends Thread implements Serializable {
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket clientSocket;
    int thread_number;
    RMI_Interface r;
    
    public Connection (Socket socket, int numero, RMI_Interface h){
        thread_number = numero;
        try{
            clientSocket = socket;
            r=h;
            in = new ObjectInputStream(clientSocket.getInputStream());
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.start();
        }catch(IOException e){System.out.println("Connection:" + e.getMessage());}
    }
    //=============================

    public void run() {
        
    
        try{
            while(true){
                
                LinkedHashMap <String, String> data = (LinkedHashMap <String, String>) in.readObject();
                
                
                System.out.println("T["+thread_number + "] Received: ");
                //list elements
                for (String key : data.keySet()) {
                    
                      String value = data.get(key);
                      System.out.println(key + " : " + value);
               }
                
                
                this.getType(data);
                
            }
        }catch(EOFException e){
            System.out.println("The client["+thread_number+"] ended the connection: EOF:" + e);
            TCPServer.numero--;
        }catch(ClassNotFoundException e){System.out.println("erro");}
        catch(IOException e){System.out.println("IO:" + e);


        }
    }
    //ve o tipo de operacao e responde ao cliente conforma o tipo de operacao
    public void getType(LinkedHashMap <String, String> data){
        try{
            LinkedHashMap<String, String> reply = new LinkedHashMap<String, String>();
           	

            //se for do tipo criar_leilao...
            if(data.get("type").equals("create_auction")){
                if(r.create_auction(data)){
                    reply.put("type","create_auction");
                    reply.put("ok","true");
                }
                else{
                    reply.put("type","create_auction");
                    reply.put("ok","false");
                }

                out.writeObject(reply);
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
           		
           		out.writeObject(reply);
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
	           	out.writeObject(reply);
       		}

       		else if(data.get("type").equals("my_auctions")){
       			String username = "daniel";//temos que ir buscar isto ao cliente...
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
	           	out.writeObject(reply);
       		}

           else{
           	System.out.println("operation not found!");
           }
        }
        catch(Exception e){
        	System.out.println(e);
        }

    }

}