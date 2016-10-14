//java Server_TCP <porto>
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;
import java.util.*;
public class TCPServer  {
    public static int numero=0;
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
                
                HashMap <String, String> data = (HashMap <String, String>) in.readObject();
                
                System.out.println("T["+thread_number + "] Received: ");
                //verifica o tipo de operacao
                this.getType(data);
                //list elements
                for (String key : data.keySet()) {
                    
                      String value = data.get(key);
                      System.out.println(key + " : " + value);
               }
                
                
            }
        }catch(EOFException e){
            System.out.println("The client["+thread_number+"] ended the connection: EOF:" + e);
            TCPServer.numero--;
        }catch(ClassNotFoundException e){System.out.println("erro");}
        catch(IOException e){System.out.println("IO:" + e);


        }
    }

    public void getType(HashMap <String, String> data){
        try{
            HashMap<String, String> reply = new HashMap<String, String>();

            //se for do tipo criar_leilao...
            if(data.get("type").equals("create_auction")){
                if(r.criar_leilao(data)){
                    reply.put("type","create_auction");
                    reply.put("ok","true");
                }
                else{
                    reply.put("type","create_auction");
                    reply.put("ok","false");
                }

                out.writeObject(reply);
            }
        }
        catch(Exception e){

        }

    }

}