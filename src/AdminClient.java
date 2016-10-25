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
    public static RMI_Interface RMI;
    public static int count=0;

    protected AdminCLient() throws RemoteException {
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

        } catch(IOException e) {
            System.out.println("Listen: " + e.getMessage());
        } catch(Exception ex){
            System.out.println("NULL ???");
            ex.printStackTrace();
        }
    }

    //cancels an on going auction
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

    //bans user and removes his auctions and his bids from on going auction
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

    //displays stats (# logged users, #total auctions, #on going auctions, #ended auctions, #banned users, #total users, etc)
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
            	inFromServer.close();
                socket.close();
                return false;
            }

            //testing for equal codes
            outToServer.println("type: search_auction, code: "+code);
            while(!inFromServer.readLine().equals("type: search_auction , items_count: 0")){
            	count++;
            	if (count>10){
            		inFromServer.close();
                    socket.close();
                    return false;
            	}
            	code=(long)(Random.nextDouble()*2^62);
            }

            //creating auction
            outToServer.println("type: create_auction, code: "+code+", title: "+title+", description: "+description+", deadline: "+deadline+", amount: "+amount);
            if(!inFromServer.readLine().equals("type: create_auction, ok: true")){
            	inFromServer.close();
                socket.close();
                return false;
            }

            //getting id
            outToServer.println("type: search_auction, code: "+code);
           	String temp=inFromServer.readLine();
            id=getItemID(temp);
            if ((id==-1)||(!temp.equals("type: search_auction, items_count: 1, items_0_id: "+id+", items_0_code: "+code+", items_0_title: "+title))){
            	inFromServer.close();
                socket.close();
                return false;
            }

            //checkign details
            outToServer.println("type: detail_auction, id: "+id);
            if(!inFromServer.readLine().equals("type: detail_auction, title: "+title+", description: "+description+", deadline: "+deadline+" , messages_count: 0, bids_count : 0")){
            	inFromServer.close();
                socket.close();
                return false;
            }

            //biding
            outToServer.println("type: bid, id: "+id+", amount: "+amount);
            if(!inFromServer.readLine().equals("type: bid, ok: true")){
            	inFromServer.close();
                socket.close();
                return false;
            }

            //checking details
            outToServer.println("type: detail_auction, id: "+id);
            if(!inFromServer.readLine().equals("type: detail_auction, title: "+title+", description: "+description+", deadline: "+deadline+", messages_count: 0, bids_count: 1")){
            	inFromServer.close();
                socket.close();
                return false;
            }
            inFromServer.close();
            socket.close();
            return true;

        } catch (IOException e) {
            if(inFromServer == null)
            System.out.println("\nUsage: java TCPClient <host> <port>\n");
            System.out.println(e.getMessage());
            
        } finally {
            try { inFromServer.close(); } catch (Exception e) {}
        }
        return false;

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


