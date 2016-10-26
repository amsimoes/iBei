//java Server_TCP <porto>
import java.net.*;
import java.rmi.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class AdminClient extends UnicastRemoteObject /*implements TCP_Interface*/{
    public static RMI_Interface RMI;
    public static int count=0;

    protected AdminClient() throws RemoteException {
    }

    public static void main(String args[]) {
        if (args.length == 0) {
            System.out.println("insert serverport");
            System.exit(0);
        }
        String port = args[0];

        try{
            AdminClient.RMI = (RMI_Interface) LocateRegistry.getRegistry(7000).lookup("connection");
            AdminClient adminclient = new AdminClient();

            adminclient.menu();

            int serverPort = Integer.parseInt(port);


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
    private static boolean cancelAuction(long id){
        try {
            if(AdminClient.RMI.cancelAuction(id)){
            	System.out.println("Leilão cancelado com sucesso.");
            }else{
            	System.out.println("Não existe nenhum leilão com ID: "+id+".");
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminClient.RMI_reconnection();
            cancelAuction(id);
        }
    }

    //bans user and removes his auctions and his bids from on going auction
    private static boolean banUser(String username){
    	try {
            if(AdminClient.RMI.banUser(username)){
            	System.out.println("Utlizador "+username+" banido com sucesso.");
            }else{
            	System.out.println("Não existe nenhum utilizador "+username+".");
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminClient.RMI_reconnection();
            banUser(username);
        }
    }

    //displays stats (# logged users, #total auctions, #on going auctions, #ended auctions, #banned users, #total users, etc)
    private static boolean getStatsLeiloes(){
    	try {
            if(AdminClient.RMI.getStats()){
                //TODO print stats
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminClient.RMI_reconnection();
            getStats();
        }
    }

    private static boolean getStatsLVitorias(){
        try {
            if(AdminClient.RMI.getStats()){
                //TODO print stats
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminClient.RMI_reconnection();
            getStats();
        }
    } 

    private static long getItemID(String data){
        String [] aux = data.split(",");
        long r = -1;
        int i = 0;
        for(String field : aux){
            String [] aux1 = field.trim().split(":", 2);
            aux1[0] = aux1[0].trim();
            aux1[1] = aux1[1].trim();
            if (aux1[0].equals("items_"+i+"_id")){
                i++;
                r = Long.parseLong(aux1[1]);
            }

        }
        return r;
    }

    private static boolean testTCP(String [] args){
    	//TODO add tcp client code
        Socket socket;
        PrintWriter outToServer;
        BufferedReader inFromServer = null;
        try {
            // connect to the specified address:port (default is localhost:12345)
            if(args.length == 2)
                socket = new Socket(args[0], Integer.parseInt(args[1]));
            else
                return false;

            //test values
            long code=9001;
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
                socket.close();
                return false;
            }

            //creating auction
            outToServer.println("type: create_auction, code: "+code+", title: "+title+", description: "+description+", deadline: "+deadline+", amount: "+amount);
            if(!inFromServer.readLine().equals("type: create_auction, ok: true")){
                socket.close();
                return false;
            }

            //getting id
            outToServer.println("type: search_auction, code: "+code);
           	String temp=inFromServer.readLine();
            id=getItemID(temp);
            if (id!=-1){
                socket.close();
                return false;
            }

            //checkign details
            outToServer.println("type: detail_auction, id: "+id);
            if(!inFromServer.readLine().equals("type: detail_auction, title: "+title+", description: "+description+", deadline: "+deadline+" , messages_count: 0, bids_count : 0")){
                socket.close();
                cancelAuction(id);
                return false;
            }

            //biding
            outToServer.println("type: bid, id: "+id+", amount: "+amount);
            if(!inFromServer.readLine().equals("type: bid, ok: true")){
                socket.close();
                cancelAuction(id);
                return false;
            }

            //checking details
            outToServer.println("type: detail_auction, id: "+id);
            if(!inFromServer.readLine().equals("type: detail_auction, title: "+title+", description: "+description+", deadline: "+deadline+", messages_count: 0, bids_count: 1")){
                socket.close();
                cancelAuction(id);
                return false;
            }
            inFromServer.close();
            socket.close();
            cancelAuction(id);
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

    public static void menu(){
    	int slt;
    	Scanner scan = new Scanner(System.in);
    	do{
    		System.out.println("Selecione uma das seguintes opções:\n1.Cancelar leilão\n2.Banir utilizador\n3.Consultar estatísticas\n4.Testar servidor TCP\n0.Sair\n");
    		slt=scan.nextInt();
    		switch(slt){
    			case 1:
    				System.out.print("ID do leião: ");
    				long id = scan.nextLong();
    				cancelAuction(id);
    				break;
    			case 2:
	    			System.out.print("Nome de utilizador: ");
			    	String username = scan.nextLine();
    				banUser(username);
    				break;
    			case 3:
    				getStats();
    				break;
    			case 4:
    				System.out.print("TCP Server <host> <port>: ");
                    testTCP(scan.nextLine().split(" "));
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
            AdminClient.RMI = (RMI_Interface) LocateRegistry.getRegistry(7000).lookup("connection");
            count = 0;
        } catch (RemoteException | NotBoundException e) {
            count += 2;
            if(count >= 30) {
                System.out.println("RMI Servers with problems...");
            }
            AdminClient.RMI_reconnection();
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


