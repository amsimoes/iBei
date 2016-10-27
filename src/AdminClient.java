//java Server_TCP <porto>
import java.net.*;
import java.rmi.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdminClient extends UnicastRemoteObject /*implements TCP_Interface*/{
    public static RMI_Interface RMI;
    public static int count=0;

    protected AdminClient() throws RemoteException {
    }

    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.println("usage: RMI IP, RMI port, TCP IP, TCP port");
            System.exit(0);
        }

        try{
            AdminClient.RMI = (RMI_Interface) LocateRegistry.getRegistry(args[0],Integer.parseInt(args[1])).lookup("ibei");
            AdminClient adminclient = new AdminClient();

            adminclient.menu();

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
            e.printStackTrace();
            AdminClient.RMI_reconnection();
            cancelAuction(id);
        }
        return true;
    }

    //bans user and removes his auctions and his bids from on going auction
    private static boolean banUser(String username){
    	try {
            if(AdminClient.RMI.banUser(username)){
            	System.out.println("Utlizador "+username+" banido com sucesso.");
            }else{
            	System.out.println("Utilizador nao banido.");
            }
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminClient.RMI_reconnection();
            banUser(username);
        }
        return true;
    }

    //displays stats (# logged users, #total auctions, #on going auctions, #ended auctions, #banned users, #total users, etc)
    private static void getStatsLeiloes() throws RemoteException{
    	try {
            User [] stats = AdminClient.RMI.statsLeiloes();
            //TODO print stats

        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminClient.RMI_reconnection();
            getStatsLeiloes();
        }

    }

    private static void getStatsVitorias() throws RemoteException{
        try {
            User [] stats = AdminClient.RMI.statsVitorias();
        } catch (RemoteException e) {
            //e.printStackTrace();
            AdminClient.RMI_reconnection();
            getStatsVitorias();
        }

    }

    private static void getStatsLastWeek() throws RemoteException{
            int stats = AdminClient.RMI.statsLastWeek();
    } 

    private static void getStats() throws RemoteException{
        getStatsVitorias();
        getStatsLeiloes();
        getStatsLastWeek();
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

    private static boolean testTCP(String ipTCP, String portTCP){
    	//TODO add tcp client code
        Socket socket;
        PrintWriter outToServer;
        BufferedReader inFromServer = null;
        try {
            // connect to the specified address:port (default is localhost:12345)
            socket = new Socket(ipTCP, Integer.parseInt(portTCP));

            //test values
            String code="9133";
            long id;
            String title = "Test auction please don't bid";
            String description = "The unseen auction is the deadliest";
            String deadline = "3002-09-19 16-20";
            DateFormat d1 = new SimpleDateFormat("yyyy-MM-dd HH-mm");
            Date date = d1.parse(deadline);
            int amount=666;
            int count=0;

            // create streams for writing to and reading from the socket
            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToServer = new PrintWriter(socket.getOutputStream(), true);

            //register admin
            outToServer.println("type: register, username: admin, password: 123");
            System.out.print("Testing register admin: ");
            if(!inFromServer.readLine().equals("type:register, ok:true")) {
                System.out.println("Wrong answer: (OK: False)");
            } else {
                System.out.println("Passed. (OK: True)");
            }
           
           	//login
            outToServer.println("type: login, username: admin, password: 123");
            System.out.print("Testing login admin: ");
            if(!inFromServer.readLine().equals("type:login, ok:true")){
                System.out.println("Wrong answer: (OK: False)");
            } else {
                System.out.println("Passed. (OK: True)");
            }

            //creating auction
            outToServer.println("type: create_auction, code: "+code+", title: "+title+", description: "+description+", deadline: "+deadline+", amount: "+amount);
            System.out.print("Testing create auction: ");
            if(!inFromServer.readLine().equals("type:create_auction, ok:true")){
                System.out.println("Wrong answer: (OK: false)");
            } else {
                System.out.println("Passed. (OK: True)");
            }

            //getting id
            outToServer.println("type: search_auction, code: "+code);
           	String temp=inFromServer.readLine();
            id=getItemID(temp);
            System.out.println("Auction id: "+id);
            System.out.print("Testing search_auction: ");
            if (id==-1){
                System.out.println("Wrong answer. No auction with code: "+code);
                socket.close();
                return false;
            } else {
                System.out.println("Passed. Answer: "+temp);
            }

            //checking details
            outToServer.println("type: detail_auction, id: "+id);
            String a = inFromServer.readLine();
            System.out.print("Testing detail_auction: ");
            if(a.equals("type:detail_auction, ok:false")){
                System.out.println("Wrong answer. Answer: "+a);
                cancelAuction(id);
                return false;
            } else {
                System.out.println("Passed.");
            }

            //biding
            System.out.println("bid");
            outToServer.println("type: bid, id: "+id+", amount: 500");
            String a1 = inFromServer.readLine();
            System.out.print("Testing bid: ");
            if(!a1.equals("type:bid, ok:true")) {
                System.out.println("Wrong answer. Answer: "+a1);
            } else {
                System.out.println("Passed.");
            }

            //checking details
            System.out.println("detail_auction");
            outToServer.println("type: detail_auction, id: "+id);
            inFromServer.readLine();
            a1 = inFromServer.readLine();
            String s = "type:detail_auction, title:1º: "+title+", description:1º: "+description+", deadline:"+date.toString()+", code:"+code+", messages_count:0, bids_count:1, bids_0_user:admin, bids_0_amount:500";
            System.out.print("Testing detail_auction: ");
            if(!a1.equals(s)){
                System.out.println("Wrong answer. Answer: "+a1);
                System.out.println("Expected: "+s);
                cancelAuction(id);
            } else {
                System.out.println("Passed.");
            }

            // logout
            System.out.println("logout");
            outToServer.println("type: logout");
            String log =inFromServer.readLine();
            System.out.print("Testing logout: ");
            if(!log.equals("type:logout, ok:true")){
                System.out.println("Wrong answer. Answer: "+log);
                cancelAuction(id);
            } else {
                System.out.println("Passed. (OK: True)");
            }

            inFromServer.close();
            socket.close();
            cancelAuction(id);
            return true;
        } catch (IOException | NullPointerException e) {
            if(inFromServer == null) {
                System.out.println("\nUsage: java TCPClient <host> <port>\n");
            }
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            try {
                inFromServer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;

    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void wait4user() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Prima para voltar ao menu.");
        scanner.nextLine();
        clearScreen();
    }

    public static void menu(){
    	int slt;
    	Scanner scan = new Scanner(System.in);
    	while(true) {
    		System.out.println("Selecione uma das seguintes opções:\n" +
                                "1.Cancelar leilão\n" +
                                "2.Banir utilizador\n" +
                                "3.Consultar estatísticas\n" +
                                "4.Testar servidor TCP\n" +
                                "0.Sair");
            System.out.print("Opcao: ");
            slt=scan.nextInt();
            clearScreen();
    		switch(slt){
    			case 1:
    				System.out.print("ID do leilao: ");
    				long id = scan.nextLong();
    				cancelAuction(id);
                    wait4user();
    				break;
    			case 2:
	    			System.out.print("Username: ");
			    	String username = scan.next();
                    System.out.println("Input: "+username);
                    banUser(username);
                    wait4user();
    				break;
    			case 3:
                    try {
                        getStats();
                        wait4user();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
    			case 4:
    				System.out.print("TCP Server <host> <port>: ");
                    scan.nextLine();
                    String data [] = scan.nextLine().split(" ");
                    testTCP(data[0], data[1]);
                    wait4user();
    				break;
    			case 0:
    			    System.exit(0);
    				break;
    			default:
    				//System.out.println("Seleção inválida.\n");
            }
    	}
    }

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

}



