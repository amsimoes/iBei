import java.io.FileNotFoundException;
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

    public boolean registerClient(LinkedHashMap<String, String> data) throws RemoteException {
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
        this.export_registed();
        return true;
    }

    public boolean loginClient(LinkedHashMap<String, String> data) throws RemoteException {
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
                    //System.out.println("Utilizador não se encontra loggado.");
                    System.out.println("Utilizador "+u.username+" loggado com sucesso!");
                    loggados.add(u);
                    this.export_logged();
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

    public LinkedHashMap<String, String> logoutClient(String username) throws RemoteException {
        LinkedHashMap <String, String> reply = new LinkedHashMap<>();
        reply.put("type", "logout");
        for(User user : loggados) {
            if(user.username.equals(username)) {
                loggados.remove(user);
                reply.put("ok", "true");
                return reply;
            }
        }
        reply.put("ok", "false");
        return reply;
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
        long id = Long.parseLong(data.get("id"));
        double amount = Double.parseDouble(data.get("amount"));
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

    public boolean edit_auction(LinkedHashMap<String, String> data, String username) throws RemoteException {
        try {
            long id2edit = Long.parseLong(data.get("id"));
            for(Leilao it : leiloes) {
                if(it.id_leilao == id2edit) {
                    if(!it.username_criador.equals(username)) { // Utilizador tenta editar leilao q nao é autor
                        System.out.println("Utilizador "+username+" nao e' autor do leilao.");
                        return false;
                    }
                    // Campos alteraveis do leilao: code|title|description|deadline|amount
                    if (data.containsKey("title")) {
                        it.titulo.set(0, data.get("title"));
                    } if (data.containsKey("description")) {
                        it.descricao.set(0, data.get("description"));
                    } if (data.containsKey("deadline")) {
                        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                        try {
                            it.data_termino = df.parse(data.get("deadline"));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } if (data.containsKey("amount")) {
                        double d = Double.parseDouble(data.get("amount"));
                        it.precoMax = d;
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Erro ao editar campos de leilao.");
        }
        return false;
    }

    public LinkedHashMap<String, String> listOnlineUsers() throws RemoteException {
        LinkedHashMap <String, String> reply = new LinkedHashMap<>();
        reply.put("type", "online_users");
        int users_count = loggados.size();
        reply.put("users_count", String.valueOf(users_count));
        for(int i=0;i<loggados.size();i++) {
            reply.put("users_"+i+"_username", loggados.get(i).username);
        }
        return reply;
    }


    public void import_auctions(){
        FicheiroDeTexto file = new FicheiroDeTexto();
        try {
            file.abreLeitura("auctions.txt");

            DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            int i;
            String username = file.leLinha();
            // TODO: Verificar se existem artigos cujos autores não se encontram registados
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

    public void export_registed(){
        FicheiroDeTexto file = new FicheiroDeTexto();
        try {
            file.abreEscrita("regist.txt");
            for(User user : registados) {
                file.escreveNovaLinha(user.username+";"+user.password);
                System.out.println("regist.txt | A Escrever: "+user.username+" "+user.password);
            }
            file.fechaEscrita();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void import_registed(){
        FicheiroDeTexto file = new FicheiroDeTexto();
        try {
            file.abreLeitura("regist.txt");
            String read = file.leLinha();
            while(read != null) {
                String[] creds = read.split(";");
                registados.add(new User(creds[0], creds[1]));
                read = file.leLinha();
            }
            file.fechaLeitura();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void export_logged(){
        FicheiroDeTexto file = new FicheiroDeTexto();
        try {
            file.abreEscrita("logged.txt");
            for(User user : loggados) {
                file.escreveNovaLinha(user.username);
                System.out.println("login.txt | A Escrever: "+user.username);
            }
            file.fechaEscrita();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
    public void import_logged(){
        FicheiroDeTexto file = new FicheiroDeTexto();
        try {
            file.abreLeitura("logged.txt");
            String read = file.leLinha();
            while(read != null) {
                registados.add(new User(read,""));
                read = file.leLinha();
            }
            file.fechaLeitura();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws MalformedURLException {

        try {
            RMI_Server h = new RMI_Server();

            Registry r = LocateRegistry.createRegistry(7000);
            r.rebind("connection", h);

            h.import_auctions();
            h.import_registed();

            System.out.println("RMI Server ready.");

        } catch (RemoteException re) {
            //talvez nao seja a melhor soluçao
            new Thread() {
                public void run() {

                    try {
                        Thread.sleep(1000);
                        main(args);
                    } catch (InterruptedException | MalformedURLException e) {
                        e.printStackTrace();
                    }
                }

            }.start();
        }
    }
}
