import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
/**
 * This class establishes a TCP connection to a specified server, and loops
 * sending/receiving strings to/from the server.
 * <p>
 * The main() method receives two arguments specifying the server address and
 * the listening port.
 * <p>
 * The usage is similar to the 'telnet <address> <port>' command found in most
 * operating systems, to the 'netcat <host> <port>' command found in Linux,
 * and to the 'nc <hostname> <port>' found in macOS.
 *
 * @author Raul Barbosa
 * @author Alcides Fonseca
 * @version 1.1
 */
class TCPClient implements Serializable{
  public static void main(String[] args) throws ClassNotFoundException{
    Socket socket;
    ObjectOutputStream  out;
    ObjectInputStream in;
    
    BufferedReader inFromServer = null;
    try {
      // connect to the specified address:port (default is localhost:1099)
      if(args.length == 2)
        socket = new Socket(args[0], Integer.parseInt(args[1]));
      else
        socket = new Socket("localhost", 1099);
        
      
      
      //exemplo de input de cliente


      // create streams for writing to and reading from the socket
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());

      // create a thread for reading from the keyboard and writing to the server
      new Thread() {
        public void run() {
          Scanner keyboardScanner = new Scanner(System.in);
          while(!socket.isClosed()) {
            
            LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>();
            String readKeyboard = keyboardScanner.nextLine();
            try{
            	//em cada input do teclado envia a hashmap para o TCPServer
                String [] aux = readKeyboard.split(",");

                for(String field : aux){
                    String [] aux1 = field.trim().split(": ");
                    hashMap.put(aux1[0], aux1[1]);
                }
            
                out.writeObject(hashMap);
                
                
              }
            catch(IOException e){System.out.println(e);}
            
          }
        }
      }.start();

      // the main thread loops reading from the server and writing to System.out
      while(true){
        
          LinkedHashMap <String, String> reply = (LinkedHashMap <String, String>) in.readObject();
          System.out.println("Received: " + reply);
        
        }
    } catch (IOException e) {
      if(inFromServer == null){
        System.out.println("TCP Server is down...");
        System.exit(0);

      }
    //} catch(ParseException e){
        
    } finally {
      try { inFromServer.close(); } catch (Exception e) {System.out.println(e);}
    }
  }
}
