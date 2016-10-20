import java.io.IOException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.text.*;
import java.net.*;
import java.util.*;


public class RMI_Server extends UnicastRemoteObject implements RMI_Interface{
    ArrayList <Leilao> leiloes;
    ArrayList <User> registados;
    ArrayList <User> loggados;

    public RMI_Server() throws RemoteException {
        super();
        leiloes = new ArrayList <Leilao>();
        registados = new ArrayList<User>();
        loggados = new ArrayList<User>();
    }

    public boolean register_client(LinkedHashMap<String, String> data) throws RemoteException {
        try {
            User u = new User(data.get("username"), data.get("password"));
            for (User user : registados) {
                if(user.username.equals(u.username)) {
                    System.out.println("Nome de utilizador "+u.username+" ja registado.");
                    return false;
                }
            }
            registados.add(u);
            System.out.println("Utilizador "+u.username+" registado com sucesso.");
        } catch(Exception e) {
            System.out.println(e);
        }
        return true;
    }

    public boolean login_client(LinkedHashMap<String, String> data) throws RemoteException {
        try {
            User u = new User(data.get("username"), data.get("password"));
            // Verificar se se encontra registado
            for(User user : registados) {
                if(user.username.equals(u.username)) {  // Encontra-se registado
                    if(!user.password.equals(u.password)) {
                        System.out.println("Password errada.");
                        return false;
                    }
                    for (User user2 : loggados) {
                        if(user2.username.equals(u.username)) {
                            System.out.println("Utilizador "+u.username+" ja se encontra loggado.");
                            return false;
                        }
                    }
                    System.out.println("Utilizador não se encontra loggado.");
                    System.out.println("Utilizador loggado com sucesso!");
                    return true;
                }
            }
            System.out.println("Utilizador "+u.username+" nao se encontra registado.");
            return false;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public boolean create_auction(LinkedHashMap<String, String> data, String username) throws RemoteException{

        int code = 	Integer.parseInt(data.get("code"));
        double amount = Double.parseDouble(data.get("amount"));

        DateFormat d1 = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        try{
            Date date = d1.parse(data.get("deadline"));
            Leilao l = new Leilao(username,code,data.get("title"),data.get("description"),amount,date);

            leiloes.add(l);

            System.out.println("Auction created!");

            this.export_auctions();
            return true;
        }

        catch(Exception e){
            System.out.println(e);
        }

        return false;
    }

    public Leilao detail_auction(LinkedHashMap<String, String> data){
        long id = 	Long.parseLong(data.get("id"));

        int i;
        for(i=0; i<leiloes.size();i++){
            if(leiloes.get(i).id_leilao == id){
                leiloes.get(i).printInfo();
                return leiloes.get(i);
            }
        }

        return null;
    }

    public ArrayList <Leilao> search_auction(LinkedHashMap<String, String> data){



        ArrayList <Leilao> leiloes_encontrados = new ArrayList <Leilao>();
        for(Leilao leilao : leiloes){
            if(leilao.artigoId == Long.parseLong(data.get("code"))){
                leiloes_encontrados.add(leilao);
                leilao.printInfo();
            }

        }
        return leiloes_encontrados;

    }
    //temos que excluir os leiloes que ja acabaram?
    public ArrayList <Leilao> my_auctions(LinkedHashMap<String, String> data, String username){

        ArrayList <Leilao> leiloes_encontrados = new ArrayList <Leilao>();

        for(Leilao leilao : leiloes){


            int n = leiloes_encontrados.size();
            for(LinkedHashMap <String, String> msg1 : leilao.mensagens){
                if(msg1.get("author").equals(username)){
                    leiloes_encontrados.add(leilao);
                    break;
                }
            }

            if(n == leiloes_encontrados.size()){
                for(LinkedHashMap <String, String> bid1 : leilao.licitacoes){
                    if(bid1.get("author").equals(username)){
                        leiloes_encontrados.add(leilao);
                        break;
                    }
                }
            }
            //melhorar codigo
            if(n == leiloes_encontrados.size() && leilao.username_criador.equals(username)){
                leiloes_encontrados.add(leilao);
            }

        }
        return leiloes_encontrados;
    }
    //falta mandar para os restantes licitadores a notificacao	
    public boolean make_bid(LinkedHashMap<String, String> data, String username){


        long id = 	Long.parseLong(data.get("id"));
        double amount = 	Double.parseDouble(data.get("amount"));
        int i;
        for(i=0;i<leiloes.size();i++){
            if(leiloes.get(i).id_leilao == id){
                break;
            }
        }

        if(i >= leiloes.size()){
            System.out.println("Auction does not exist");
            return false;
        }

        Leilao auc = leiloes.get(i);

        if(auc.precoMax <= amount){
            System.out.println("Bid higher than preco Max");
            return false;
        }

        for(LinkedHashMap <String, String> lic : auc.licitacoes){
            if(amount > Double.parseDouble(lic.get("bid"))){
                return false;
            }
        }

        if(auc.state == 1 || auc.state == 2){
            System.out.println("The auction already ended or was canceled");
            return false;
        }

        LinkedHashMap <String, String > my_bid = new LinkedHashMap <String, String>();
        my_bid.put("author", username);
        my_bid.put("bid", data.get("amount"));

        auc.licitacoes.add(my_bid);
        this.export_auctions();


        return true;

    }
    //falta mandar para a notificao para os que escreveram no mural e para o criador do leilao
    public boolean write_message(LinkedHashMap<String, String> data, String username){

        long id = Long.parseLong(data.get("id"));
        String text = data.get("text");

        int i;
        for(i=0;i<leiloes.size();i++){
            if(leiloes.get(i).id_leilao == id){
                break;
            }
        }
        //se o leilao nao existir
        if(i >= leiloes.size()){
            System.out.println("Auction does not exist");
            return false;
        }

        Leilao auc = leiloes.get(i);

        if(auc.state == 1 || auc.state == 2){
            System.out.println("The auction already ended or was canceled");
            return false;
        }

        LinkedHashMap <String, String > my_msg = new LinkedHashMap <String, String>();
        my_msg.put("author", username);
        my_msg.put("message", text);

        auc.mensagens.add(my_msg);
        auc.printInfo();
        this.export_auctions();
        return true;

    }


    public void import_autions(){
        FicheiroDeTexto file = new FicheiroDeTexto();
        try {
            file.abreLeitura("auctions.txt");

            DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            int i;
            String username = file.leLinha();
            while(username != null){
                long artigoID = Long.parseLong(file.leLinha());
                long id_leilao = Long.parseLong(file.leLinha());
                String [] titulo = file.leLinha().replaceAll(", ",",").split(",");
                ArrayList <String> titulos = new  ArrayList<String>(Arrays.asList(titulo));
                String [] descricao = file.leLinha().replaceAll(", ",",").split(",");
                ArrayList <String> descricoes =new  ArrayList<String>(Arrays.asList(descricao));
                double precoMax = Double.parseDouble(file.leLinha());
                int state = Integer.parseInt(file.leLinha());
                Date data = df.parse(file.leLinha());

                Leilao leilao = new Leilao(username, artigoID, titulos.get(0), descricoes.get(0), precoMax, data);
                //talvez nao seja a melhor solucao...
                for(i=1; i< titulos.size();i++){
                    leilao.titulo.add(titulos.get(i));
                }
                for(i=1; i< descricoes.size();i++){
                    leilao.descricao.add(descricoes.get(i));
                }
                leilao.state = state;
                leilao.id_leilao = id_leilao;
                String next = file.leLinha();

                while(next.indexOf("author=") != -1){
                    LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>();
                    next = next.substring(1,next.length()-1);
                    String [] aux = next.split(", ");

                    for(String field : aux){
                        String [] aux1 = field.split("=");
                        hashMap.put(aux1[0], aux1[1]);
                    }

                    if(next.indexOf("message=") != -1){
                        leilao.mensagens.add(hashMap);
                        }

                    else if(next.indexOf("bid=") != -1){
                        leilao.licitacoes.add(hashMap);
                    }
                    next = file.leLinha();
                }
                leiloes.add(leilao);
                username = file.leLinha();
            }
            file.fechaLeitura();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void export_auctions(){
        FicheiroDeTexto file = new FicheiroDeTexto();
        int i;
        try {
            file.abreEscrita("auctions.txt");
            for(Leilao leilao: leiloes){

                file.escreveNovaLinha(leilao.username_criador);
                file.escreveNovaLinha(String.valueOf(leilao.artigoId));
                file.escreveNovaLinha(String.valueOf(leilao.id_leilao));
                for(i=0; i< leilao.titulo.size()-1; i++){
                    file.escreveLinha(leilao.titulo.get(i)+", ");
                }
                file.escreveNovaLinha(leilao.titulo.get(leilao.titulo.size()-1));

                for(i=0; i< leilao.descricao.size()-1; i++){
                    file.escreveLinha(leilao.descricao.get(i)+", ");
                }
                file.escreveNovaLinha(leilao.descricao.get(leilao.descricao.size()-1));
                file.escreveNovaLinha(String.valueOf(leilao.precoMax));
                file.escreveNovaLinha(String.valueOf(leilao.state));

                DateFormat d1 = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                file.escreveNovaLinha(d1.format(leilao.data_termino));

                for(LinkedHashMap <String, String> msg: leilao.mensagens){
                    file.escreveNovaLinha(msg.toString());
                }

                for(LinkedHashMap <String, String> bid: leilao.licitacoes){
                    file.escreveNovaLinha(bid.toString());
                }
                file.escreveNovaLinha("");
            }
            file.fechaEscrita();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    public static void main(String args[]) throws MalformedURLException {

        try {
            RMI_Server h = new RMI_Server();

            Registry r = LocateRegistry.createRegistry(7000);
            r.rebind("connection", h);

            h.import_autions();

            System.out.println("RMI Server ready.");

        } catch (RemoteException re) {
            //talvez nao seja a melhor soluçao
            new Thread() {
                public void run() {

                    try {
                        Thread.sleep(1000);
                        main(args);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }


                }

            }.start();

        }
    }


}
