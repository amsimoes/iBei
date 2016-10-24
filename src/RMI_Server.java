import java.io.IOException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.text.*;
import java.util.*;


public class RMI_Server extends UnicastRemoteObject implements RMI_Interface {
    private ArrayList<Leilao> leiloes;
    private ArrayList<User> registados;
    private static ArrayList<User> loggados;
    private ArrayList<Connection> conns;
    ArrayList<TCP_Interface> tcpServers;

    public RMI_Server() throws RemoteException {
        super();
        leiloes = new ArrayList<Leilao>();
        registados = new ArrayList<User>();
        loggados = new ArrayList<User>();
        tcpServers = new ArrayList<TCP_Interface>();
    }

    public void data(TCP_Interface tcp) throws RemoteException {
        tcpServers.add(tcp);
    }

    public boolean register_client(LinkedHashMap<String, String> data) throws RemoteException {
        try {
            User u = new User(data.get("username"), data.get("password"));
            for (User user : registados) {
                if (user.username.equals(u.username)) {
                    System.out.println("Nome de utilizador " + u.username + " ja registado.");
                    return false;
                }
            }
            registados.add(u);
            System.out.println("Utilizador " + u.username + " registado com sucesso.");
        } catch (Exception e) {
            System.out.println(e);
        }
        this.exportObjRegisted();
        return true;
    }

    public boolean login_client(LinkedHashMap<String, String> data) throws RemoteException {
        try {
            User u = new User(data.get("username"), data.get("password"));
            // Verificar se se encontra registado
            for (User user : registados) {
                if (user.username.equals(u.username)) {  // Encontra-se registado
                    if (!user.password.equals(u.password)) {
                        System.out.println("Password errada.");
                        return false;
                    }
                    for (User user2 : loggados) {
                        if (user2.username.equals(u.username)) {
                            System.out.println("Utilizador " + u.username + " ja se encontra loggado.");
                            return false;
                        }
                    }
                    //System.out.println("Utilizador não se encontra loggado.");
                    System.out.println("Utilizador " + u.username + " loggado com sucesso!");
                    loggados.add(u);
                    //this.export_logged();
                    this.exportObjLogged();
                    return true;
                }
            }
            System.out.println("Utilizador " + u.username + " nao se encontra registado.");
            return false;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public LinkedHashMap<String, String> logoutClient(String username) throws RemoteException {
        LinkedHashMap<String, String> reply = new LinkedHashMap<>();
        reply.put("type", "logout");
        for (User user : loggados) {
            if (user.username.equals(username)) {
                loggados.remove(user);
                //this.export_logged();
                this.exportObjLogged();
                reply.put("ok", "true");
                return reply;
            }
        }
        reply.put("ok", "false");
        return reply;
    }

    public boolean create_auction(LinkedHashMap<String, String> data, String username) throws RemoteException {
        System.out.println(data);
        String code = data.get("code");
        System.out.println(data.get("amount "));
        double amount = Double.parseDouble(data.get("amount"));


        DateFormat d1 = new SimpleDateFormat("dd-MM-yyyy HH-mm");

        try {
            Date date = d1.parse(data.get("deadline"));

            //verificar se existe um leilao ao mesmo tempo, com o mesmo artigo e feito pelo mesmo cliente
            for (Leilao leilao : leiloes) {
                if (username.equals(leilao.username_criador) && leilao.data_termino.equals(data) && code.equals(String.valueOf(leilao.artigoId)))
                    return false;
            }

            Leilao l = new Leilao(username, code, data.get("title"), data.get("description"), amount, date);

            leiloes.add(l);

            System.out.println("Auction created!");

            //this.export_auctions();
            this.exportObjAuctions();
            return true;
        } catch (Exception e) {
            System.out.println(e);
        }

        return false;
    }

    public Leilao detail_auction(LinkedHashMap<String, String> data) {
        long id = Long.parseLong(data.get("id"));

        int i;
        for (i = 0; i < leiloes.size(); i++) {
            if (leiloes.get(i).id_leilao == id) {
                leiloes.get(i).printInfo();
                return leiloes.get(i);

            }
        }

        return null;
    }

    public LinkedHashMap<String, String> detail_request(LinkedHashMap<String, String> data) {

        Leilao leilao = detail_auction(data);

        int i;
        LinkedHashMap<String, String> reply = new LinkedHashMap<String, String>();
        if (leilao != null) {
            reply.put("type", "detail_auction");
            String titulos = "";
            i=0;
            for (String titulo : leilao.titulo) {
                i++;
                titulos += i + "º: "+titulo + ", ";
            }
            i=0;
            reply.put("title", titulos.substring(0, titulos.length() - 2));
            String descricoes = "";
            for (String descricao : leilao.descricao) {
                i++;
                descricoes += i+"º: " + descricao + ", ";
            }
            reply.put("description", descricoes.substring(0, descricoes.length() - 2));

            reply.put("deadline", leilao.data_termino.toString());
            reply.put("messages_count", String.valueOf(leilao.mensagens.size()));
            //mandar mensagens
            for (i = 0; i < leilao.mensagens.size(); i++) {
                reply.put("messages_" + String.valueOf(i) + "_user", leilao.mensagens.get(i).get("author"));
                reply.put("messages_" + String.valueOf(i) + "_text", leilao.mensagens.get(i).get("message"));
            }

            reply.put("bids_count", String.valueOf(leilao.licitacoes.size()));
            //mandar licitacoes
            for (i = 0; i < leilao.licitacoes.size(); i++) {
                reply.put("messages_" + String.valueOf(i) + "_user", leilao.licitacoes.get(i).get("author"));
                reply.put("messages_" + String.valueOf(i) + "_text", leilao.licitacoes.get(i).get("bid"));
            }

        } else {
            reply.put("type", "detail_auction");
            reply.put("ok", "false");
        }
        return reply;
    }

    public LinkedHashMap<String, String> search_auction(LinkedHashMap<String, String> data) {
        ArrayList<Leilao> leiloes_encontrados = new ArrayList<Leilao>();
        for (Leilao leilao : leiloes) {
            if (String.valueOf(leilao.artigoId).equals(data.get("code"))) {
                leiloes_encontrados.add(leilao);
                leilao.printInfo();
            }

        }
        LinkedHashMap<String, String> reply = new LinkedHashMap<String, String>();
        int i;
        if (leiloes_encontrados.size() != 0) {
            reply.put("type", "search_auction");
            reply.put("items_count", String.valueOf(leiloes_encontrados.size()));
            for (i = 0; i < leiloes_encontrados.size(); i++) {
                reply.put("items_" + String.valueOf(i) + "_id", String.valueOf(leiloes_encontrados.get(i).id_leilao));
                reply.put("items_" + String.valueOf(i) + "_code", String.valueOf(leiloes_encontrados.get(i).artigoId));
                reply.put("items_" + String.valueOf(i) + "_title", leiloes_encontrados.get(i).titulo.get(leiloes_encontrados.get(i).titulo.size() - 1));//so mostra o titulo mais recente
            }
        } else {
            reply.put("type", "search_auction");
            reply.put("items_count", "0");
        }

        return reply;

    }



    public boolean edit_auction(LinkedHashMap<String, String> data, String username) throws RemoteException {
        try {
            long id2edit = Long.parseLong(data.get("id"));
            for(Leilao it : leiloes) {
                if(it.id_leilao == id2edit) {
                    if(!it.username_criador.equals(username)) { // Utilizador tenta editar leilao q nao é autor
                        System.out.println("Utilizador "+username+" nao é autor do leilao.");
                        return false;
                    }
                    // Campos alteraveis do leilao: code|title|description|deadline|amount
                    if (data.containsKey("title")) {
                        it.titulo.add(data.get("title"));
                    } if (data.containsKey("description")) {
                        it.descricao.add(data.get("description"));
                    } if (data.containsKey("deadline")) {
                        DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH-mm");
                        try {
                            it.data_termino = df.parse(data.get("deadline"));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } if (data.containsKey("amount")) {
                        double d = Double.parseDouble(data.get("amount"));
                        it.precoMax = d;
                    }
                    exportObjAuctions();
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





    //temos que excluir os leiloes que ja acabaram?
    public LinkedHashMap<String, String> my_auctions(LinkedHashMap<String, String> data, String username) {

        ArrayList<Leilao> leiloes_encontrados = new ArrayList<Leilao>();

        for (Leilao leilao : leiloes) {


            int n = leiloes_encontrados.size();
            for (LinkedHashMap<String, String> msg1 : leilao.mensagens) {
                if (msg1.get("author").equals(username)) {
                    leiloes_encontrados.add(leilao);
                    break;
                }
            }

            if (n == leiloes_encontrados.size()) {
                for (LinkedHashMap<String, String> bid1 : leilao.licitacoes) {
                    if (bid1.get("author").equals(username)) {
                        leiloes_encontrados.add(leilao);
                        break;
                    }
                }
            }
            //melhorar codigo
            if (n == leiloes_encontrados.size() && leilao.username_criador.equals(username)) {
                leiloes_encontrados.add(leilao);
            }

        }

        LinkedHashMap<String, String> reply = new LinkedHashMap<String, String>();
        int i;
        if (leiloes_encontrados.size() != 0) {
            reply.put("type", "my_auctions");
            reply.put("items_count", String.valueOf(leiloes_encontrados.size()));
            for (i = 0; i < leiloes_encontrados.size(); i++) {
                reply.put("items_" + String.valueOf(i) + "_id", String.valueOf(leiloes_encontrados.get(i).id_leilao));
                reply.put("items_" + String.valueOf(i) + "_code", String.valueOf(leiloes_encontrados.get(i).artigoId));
                reply.put("items_" + String.valueOf(i) + "_title", leiloes_encontrados.get(i).titulo.get(leiloes_encontrados.get(i).titulo.size() - 1));//so mostra o titulo mais recente
            }
        } else {
            reply.put("type", "my_auctions");
            reply.put("items_count", "0");
        }


        return reply;
    }

    //falta mandar para os restantes licitadores a notificacao
    public boolean make_bid(LinkedHashMap<String, String> data, String username) throws RemoteException {
        long id = Long.parseLong(data.get("id"));
        double amount = Double.parseDouble(data.get("amount"));
        int i;
        for (i = 0; i < leiloes.size(); i++) {
            if (leiloes.get(i).id_leilao == id) {
                break;
            }
        }

        if (i >= leiloes.size()) {
            System.out.println("Auction does not exist");
            return false;
        }

        Leilao auc = leiloes.get(i);
        if (auc.precoMax <= amount) {
            System.out.println("Bid higher than preco Max");
            return false;
        }

        for (LinkedHashMap<String, String> lic : auc.licitacoes) {
            if (amount > Double.parseDouble(lic.get("bid"))) {
                return false;
            }
        }

        if (auc.state == 1 || auc.state == 2) {
            System.out.println("The auction already ended or was canceled");
            return false;
        }

        LinkedHashMap<String, String> my_bid = new LinkedHashMap<String, String>();
        my_bid.put("author", username);
        my_bid.put("bid", data.get("amount"));


        auc.licitacoes.add(my_bid);
        auc.printInfo();
        //this.export_auctions();
        this.exportObjLogged();
        this.bidNotification(auc,amount,username);

        return true;
    }

    //falta mandar para a notificao para os que escreveram no mural e para o criador do leilao
    public boolean write_message(LinkedHashMap<String, String> data, String username) throws RemoteException {
        long id = Long.parseLong(data.get("id"));
        String text = data.get("text");

        int i;
        for (i = 0; i < leiloes.size(); i++) {
            if (leiloes.get(i).id_leilao == id) {
                break;
            }
        }
        //se o leilao nao existir
        if (i >= leiloes.size()) {
            System.out.println("Auction does not exist");
            return false;
        }

        Leilao auc = leiloes.get(i);

        if (auc.state == 1 || auc.state == 2) {
            System.out.println("The auction already ended or was canceled");
            return false;
        }

        LinkedHashMap<String, String> my_msg = new LinkedHashMap<String, String>();
        my_msg.put("author", username);
        my_msg.put("message", text);

        auc.mensagens.add(my_msg);
        System.out.println("print no write");
        auc.printInfo();

        this.exportObjAuctions();
        this.msgNotification(auc, text, username);

        return true;
    }

    public void msgNotification(Leilao auc, String text, String username) {
        try {
            int i = 0;
            boolean flag = false;
            for (LinkedHashMap<String, String> msg : auc.mensagens) {
                if (checkPrevious(auc.mensagens, msg.get("author"), i)) {
                    for (TCP_Interface s : tcpServers) {
                        //users online
                        if (s.checkUser(msg.get("author")) && !msg.get("author").equals(username) && !msg.get("author").equals(auc.getUsername_criador())) {
                            flag = true;
                            s.sendMsg("notification_message",msg.get("author"), text, auc, username);
                        }
                    }
                    //users offline
                    if (flag == false && !msg.get("author").equals(username) && !msg.get("author").equals(auc.getUsername_criador())) {
                        //mandar notificaçao offline
                        String notification = "type: notification_message, id: " + String.valueOf(auc.id_leilao) + ", user: " + username + ", text: " + text;
                        addNotification(msg.get("author"), notification);
                    }
                    flag = false;

                }
                i++;
            }
            checkOwner(auc, username, text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bidNotification(Leilao auc, Double amount, String username) {
        try {
            int i = 0;
            for (LinkedHashMap<String, String> bid : auc.licitacoes) {
                if (checkPrevious(auc.licitacoes, bid.get("author"), i)) {
                    for (TCP_Interface s : tcpServers) {
                        if (s.checkUser(bid.get("author")) && !bid.get("author").equals(username)) {
                            s.sendMsg("notification_bid",bid.get("author"), String.valueOf(amount), auc, username);
                        }
                    }

                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //verifica se o criador do leilao esta online e manda a notificaçao
    public void checkOwner(Leilao leilao, String username, String text) {
        boolean criador = false;
        TCP_Interface owner = null;
        try {
            for (TCP_Interface s : tcpServers) {
                if (s.checkUser(leilao.getUsername_criador())) {
                    criador = true;
                    owner = s;
                    break;
                }
            }
            if (criador == false) {
                String notification = "type: notification_message, id: " + String.valueOf(leilao.id_leilao) + ", user: " + username + ", text: " + text;

                addNotification(leilao.getUsername_criador(), notification);

            } else {
                owner.sendMsg("notification_message",leilao.getUsername_criador(), text, leilao, username);
            }


        } catch(RemoteException e) {
            e.printStackTrace();
        }

}

    public static boolean checkPrevious( ArrayList<LinkedHashMap<String, String>> messages, String username, int i){
        int j=0;
        for(LinkedHashMap <String, String > msg : messages){
            if(j>=i)
                break;
            if(msg.get("author").equals(username)){
                return false;
            }
            j++;
        }
        return true;

    }



    // FicheirosObjetos
    public static void importObjLogged() {
        FicheiroDeObjeto file = new FicheiroDeObjeto();
        try {
            file.abreLeitura("loggados.ser");
            loggados = (ArrayList<User>) file.leObjeto();
            file.fechaLeitura();
        } catch (IOException e) {
            System.out.println("Ficheiro dos loggados vazio.");
        } catch (ClassNotFoundException e1) {
            System.out.println("Classe ArrayList/User not found.");
        }
    }
    public void exportObjLogged() {
        FicheiroDeObjeto file = new FicheiroDeObjeto();
        try {
            file.abreEscrita("loggados.ser");
            file.escreveObjeto(loggados);
            file.fechaEscrita();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importObjRegisted() {
        FicheiroDeObjeto file = new FicheiroDeObjeto();
        try {
            file.abreLeitura("registados.ser");
            this.registados = (ArrayList<User>) file.leObjeto();
            file.fechaLeitura();
        } catch (IOException e) {
            //System.out.println("Erro ao importar registados de fobj.");
            System.out.println("Ficheiro dos registados vazio.");
            //System.out.println(e);
        } catch (ClassNotFoundException e1) {
            System.out.println("Classe ArrayList/User not found.");
        }
    }
    public void exportObjRegisted() {
        FicheiroDeObjeto file = new FicheiroDeObjeto();
        try {
            file.abreEscrita("registados.ser");
            file.escreveObjeto(this.registados);
            file.fechaEscrita();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importObjAuctions() {
        FicheiroDeObjeto file = new FicheiroDeObjeto();
        try {
            file.abreLeitura("leiloes.ser");
            this.leiloes = (ArrayList<Leilao>) file.leObjeto();
            file.fechaLeitura();
        } catch (IOException e) {
            System.out.println("Ficheiro dos leiloes vazio.");
            //e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Classe ArrayList/Leilao not found.");
        }
    }
    public void exportObjAuctions() {
        FicheiroDeObjeto file = new FicheiroDeObjeto();
        try {
            file.abreEscrita("leiloes.ser");
            file.escreveObjeto(this.leiloes);
            file.fechaEscrita();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //adiciona uma notificaçao a lista de notificaçoes do user
    synchronized public void addNotification(String username, String text) throws RemoteException{
        for(User user: registados){
            if(user.getUsername().equals(username)){
                user.addNotification(text);
                System.out.println("Notification: "+text+"added");
                exportObjRegisted();
            }
        }

    }
    //retorna lista de notificaçoes do user
    synchronized public List<String> getNotifications(String username) throws RemoteException{
        for(User user: registados){
            if(user.getUsername().equals(username)){
                return user.getNotifications();
            }
        }

        return null;
    }
    //limpa lista de notificaçoes do user
    public void cleanNotifications(String username) throws RemoteException{
        for(User user: registados){
            if(user.getUsername().equals(username)){
                user.notifications = new ArrayList<String>();
                exportObjRegisted();
            }
        }
    }

    public void verifica_terminoLeiloes(){
        for(Leilao leilao: leiloes){
            if(new Date().after(leilao.data_termino) && leilao.state == 0){
                leilao.state = 2;
                System.out.println("Leilao com id: "+leilao.id_leilao+"e  titulo: "+leilao.titulo.get(leilao.titulo.size()-1)+" terminou");
                if(leilao.licitacoes.size() == 0)
                    System.out.println("There wasn't any bidding");
                else
                    System.out.println("Winner: "+leilao.licitacoes.get(leilao.licitacoes.size()-1).get("author"));
                exportObjAuctions();
            }
        }
    }

    public static void start(){
        try {
            RMI_Server h = new RMI_Server();
            Registry r = LocateRegistry.createRegistry(7000);
            r.rebind("connection", h);
            //h.import_registed();
            h.importObjRegisted();
            System.out.println(h.registados);
            //h.import_auctions();
            h.importObjAuctions();
            //System.out.println(h.loggados);   vazio

            System.out.println("RMI Server ready.");

            //thread para verificar o termino dos leiloes

            new Thread() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(60000);
                            h.verifica_terminoLeiloes();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        } catch (RemoteException e) {
            //e.printStackTrace();
        }
    }

    public String teste() throws RemoteException {
        return "A verificar Servidor RMI Primario";
    }

    public static void verifica(RMI_Interface h) {
        try {
            String teste = h.teste();
            System.out.println(teste);
            try {
                Thread.sleep(3000);
                verifica(h);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (RemoteException e){
            start();
            //import_logged();
            importObjLogged();
        }
    }
    
    public static void main(String args[]) {
        try {
            RMI_Interface h= (RMI_Interface) LocateRegistry.getRegistry(7000).lookup("connection");
            verifica(h);
        } catch (RemoteException | NotBoundException re) {
            start();
        }
    }
}
