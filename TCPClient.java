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
class TCPClient {
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
      LinkedHashMap<String, String> HashMap = new LinkedHashMap<String, String>();

	  //para criar leilao...    
      /*
      String dString = "22-06-2016 10:37:10";
      DateFormat date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
      
      HashMap.put("type","create_auction");
      HashMap.put("title","1_leilao");
      HashMap.put("description","tentativa de criar leilao");
      HashMap.put("code","123456789");
      HashMap.put("deadline",dString);
      HashMap.put("amount","110.20");
*/
      
	  //para consultar detalhes de leilao...
      /*
      HashMap.put("type","detail_auction");
      HashMap.put("id","356563");
      */

      //para pesquisar por codigo...
      
      HashMap.put("type","search_auction");
      HashMap.put("code","123456789");
      



      // create streams for writing to and reading from the socket
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());

      // create a thread for reading from the keyboard and writing to the server
      new Thread() {
        public void run() {
          Scanner keyboardScanner = new Scanner(System.in);
          while(!socket.isClosed()) {
            String readKeyboard = keyboardScanner.nextLine();
            try{
            	//em cada input do teclado envia a hashmap criada em cima para o TCPServer
                out.writeObject(HashMap);
              }
            catch(IOException e){}
            
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
      try { inFromServer.close(); } catch (Exception e) {}
    }
  }
}
