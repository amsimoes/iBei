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
    public static String primaryRmi [] = new String[2];
    public static String backupRmi [] = new String[2];
    static int count = 0;

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
        System.out.println("[ REGISTER CLIENT ]");
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
        System.out.println("[ LOGIN CLIENT ] ");
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

                    if(user.isBanned()) {
                        System.out.println("Utilizador "+u.username+"banido.");
                        return false;
                    }
                    System.out.println("Utilizador " + u.username + " loggado com sucesso!");
                    loggados.add(u);
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
        System.out.println("[ CREATE AUCTION ]");
        String code = data.get("code");

        double amount = Double.parseDouble(data.get("amount"));

        DateFormat d1 = new SimpleDateFormat("yyyy-MM-dd HH-mm");
        DateFormat d2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        System.out.println("Deadline: "+data.get("deadline"));

        try {
            Date date;
            try {
                date = d1.parse(data.get("deadline"));
            } catch (ParseException e) {
                date = d2.parse(data.get("deadline"));
            }

            //verificar se existe um leilao ao mesmo tempo, com o mesmo artigo e feito pelo mesmo cliente
            for (Leilao leilao : leiloes) {
                if (username.equals(leilao.username_criador) && leilao.getState() != 1 && leilao.data_termino.equals(date) && code.equals(String.valueOf(leilao.artigoId))) {
                    System.out.println("[FALHA] Create auction - false.");
                    return false;
                }
            }

            System.out.println("[DATE]"+date);
            Leilao l = new Leilao(username, code, data.get("title"), data.get("description"), amount, date);

            leiloes.add(l);
            for(User user: registados){
                if(user.getUsername().equals(username))
                    user.setLeiloes(user.getLeiloes()+1);
            }
            System.out.println("Auction created!");

            //this.export_auctions();
            this.exportObjAuctions();
            this.exportObjRegisted();
            return true;
        } catch (Exception e) {
            System.out.println("[EXCEPTION] Failed to create auction");
            e.printStackTrace();
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

    public ArrayList <Leilao> search_auction(LinkedHashMap<String, String> data) throws RemoteException {
        System.out.println("[ SEARCH AUCTION ]");
        ArrayList<Leilao> leiloes_encontrados = new ArrayList<Leilao>();
        for (Leilao leilao : leiloes) {
            if (String.valueOf(leilao.artigoId).equals(data.get("code"))) {
                leiloes_encontrados.add(leilao);
                leilao.printInfo();
            }

        }

        return leiloes_encontrados;

    }


    public boolean edit_auction(LinkedHashMap<String, String> data, String username) throws RemoteException {
        System.out.println("[ EDIT AUCTION ]");
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
                        DateFormat d1 = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                        DateFormat d2 = new SimpleDateFormat("dd-MM-yyyy HH-mm");
                        try {
                            it.data_termino = d1.parse(data.get("deadline"));
                        } catch (ParseException e) {
                            it.data_termino = d2.parse(data.get("deadline"));
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

    public ArrayList<User> listOnlineUsers() throws RemoteException {
        return loggados;
    }

    //temos que excluir os leiloes que ja acabaram?
    public ArrayList <Leilao> my_auctions(LinkedHashMap<String, String> data, String username) throws RemoteException{
        System.out.println("[ MY AUCTIONS ]");
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
        return leiloes_encontrados;
    }

    //falta mandar para os restantes licitadores a notificacao
    public Leilao make_bid(LinkedHashMap<String, String> data, String username) throws RemoteException {
        System.out.println("[ BID ]");
        long id = Long.parseLong(data.get("id"));
        double amount = Double.parseDouble(data.get("amount"));

        int i;
        for (i = 0; i < leiloes.size(); i++) {
            if (leiloes.get(i).id_leilao == id) {
                System.out.println("Leilao existente.");
                break;
            }
        }

        if (i >= leiloes.size()) {
            System.out.println("Leilao nao existe.");
            System.out.println("[ END ]");
            return null;
        }

        Leilao auc = leiloes.get(i);
        System.out.println("[INFO LEILAO]");
        System.out.println(auc.toString());
        if (auc.precoMax <= amount) {
            System.out.println("Bid higher than preco Max");
            System.out.println("[ END ]");
            return null;
        }

        for (LinkedHashMap<String, String> lic : auc.licitacoes) {
            if (amount > Double.parseDouble(lic.get("bid"))) {
                System.out.println("Montante maior que a ultima bid.");
                System.out.println("[ END ]");
                return null;
            }
        }

        if (auc.state == 1 || auc.state == 2) {
            System.out.println("[BID] The auction already ended or was canceled");
            System.out.println("Auction deadline: "+auc.getData_termino().toString());
            System.out.println("[ END ]");
            return null;
        }

        LinkedHashMap<String, String> my_bid = new LinkedHashMap<String, String>();
        my_bid.put("author", username);
        my_bid.put("bid", data.get("amount"));


        auc.licitacoes.add(my_bid);
        auc.printInfo();
        //this.export_auctions();
        this.exportObjLogged();

        System.out.println("[ END ]");
        return auc;
    }


    public void bidNotification(Leilao auc, String amount, String username, String type) throws RemoteException {
        boolean flag = false;
        try {
            int i = 0;
            for (LinkedHashMap<String, String> bid : auc.licitacoes) {
                if (checkPrevious(auc.licitacoes, bid.get("author"), i)) {
                    for (TCP_Interface s : tcpServers) {
                        if (s.checkUser(bid.get("author"))/* && !bid.get("author").equals(username)*/) {
                            flag = true;
                            s.sendMsg(type,bid.get("author"), amount, auc, username);

                        }
                    }
                    //users offline
                    if (!flag && !bid.get("author").equals(username) && !bid.get("author").equals(auc.getUsername_criador())) {
                        //mandar notificaçao offline
                        System.out.println("bid users offline");

                        if(type.equals("notification_bid")) {
                            String notification = "type: notification_bid, id: " + String.valueOf(auc.id_leilao) + ", user: " + username + ", amount: " + amount;
                            addNotification(bid.get("author"), notification);
                        }
                        else{
                            String notification = "type: notification_message, id: " + String.valueOf(auc.id_leilao) + ", user: " + username + ", text: " + amount;
                            addNotification(bid.get("author"), notification);
                        }
                    }
                    flag = false;

                }
                i++;
            }
            if(type.equals("notification_bid"))
                checkOwner(auc, username, String.valueOf(amount),"notification_bid");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Leilao write_message(LinkedHashMap<String, String> data, String username) throws RemoteException {
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
            return null;
        }

        Leilao auc = leiloes.get(i);

        if (auc.state == 1 || auc.state == 2) {
            System.out.println("The auction already ended or was canceled");
            return null;
        }

        LinkedHashMap<String, String> my_msg = new LinkedHashMap<String, String>();
        my_msg.put("author", username);
        my_msg.put("message", text);

        auc.mensagens.add(my_msg);
        auc.printInfo();

        this.exportObjAuctions();

        return auc;
    }


    public void msgNotification(Leilao auc, String text, String username) throws RemoteException {
        try {
            int i = 0;
            boolean flag = false;
            for (LinkedHashMap<String, String> msg : auc.mensagens) {
                if (checkPrevious(auc.mensagens, msg.get("author"), i)) {
                    for (TCP_Interface s : tcpServers) {
                        //users online
                        if (s.checkUser(msg.get("author")) /*&& !msg.get("author").equals(username)*/ && !msg.get("author").equals(auc.getUsername_criador())) {
                            flag = true;
                            System.out.println("msg users online");
                            s.sendMsg("notification_message",msg.get("author"), text, auc, username);
                        }
                    }
                    //users offline
                    if (flag == false && !msg.get("author").equals(username) && !msg.get("author").equals(auc.getUsername_criador())) {
                        //mandar notificaçao offline
                        System.out.println("msg users offline");
                        String notification = "type: notification_message, id: " + String.valueOf(auc.id_leilao) + ", user: " + username + ", text: " + text;
                        addNotification(msg.get("author"), notification);
                    }
                    flag = false;

                }
                i++;
            }
            this.bidNotification(auc,text,username,"notification_message");
            checkOwner(auc, username, text,"notification_message");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //verifica se o criador do leilao esta online e manda a notificaçao
    public void checkOwner(Leilao leilao, String username, String text, String type) {
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
            String notification;
            if (!criador) {
                if(type.equals("notification_message"))
                    notification = "type: "+type+", id: " + String.valueOf(leilao.id_leilao) + ", user: " + username + ", text: " + text;
                else{
                    notification = "type: "+type+", id: " + String.valueOf(leilao.id_leilao) + ", user: " + username + ", amount: " + text;
                }

                addNotification(leilao.getUsername_criador(), notification);

            } else {
                owner.sendMsg(type,leilao.getUsername_criador(), text, leilao, username);
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
    private static void importObjLogged() {
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
    private void exportObjLogged() {
        FicheiroDeObjeto file = new FicheiroDeObjeto();
        try {
            file.abreEscrita("loggados.ser");
            file.escreveObjeto(loggados);
            file.fechaEscrita();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importObjRegisted() {
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
    private void exportObjRegisted() {
        FicheiroDeObjeto file = new FicheiroDeObjeto();
        try {
            file.abreEscrita("registados.ser");
            file.escreveObjeto(this.registados);
            file.fechaEscrita();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importObjAuctions() {
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
    private void exportObjAuctions() {
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
    private synchronized void addNotification(String username, String text) throws RemoteException{
        for(User user: registados){
            if(user.getUsername().equals(username)){
                user.addNotification(text);
                System.out.println("notification: "+text+"added");
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

    private void verifica_terminoLeiloes(){
        for(Leilao leilao: leiloes){
            if(new Date().after(leilao.data_termino) && leilao.state == 0){
                Date now = new Date();
                System.out.println("DATA ATUAL: "+now);
                System.out.println("DATA_TERMINO: "+leilao.data_termino);
                leilao.state = 2;
                System.out.println("Leilao com id: "+leilao.id_leilao+"e  titulo: "+leilao.titulo.get(leilao.titulo.size()-1)+" terminou");
                if(leilao.licitacoes.size() == 0)
                    System.out.println("There wasn't any bidding");
                else {
                    System.out.println("Winner: " + leilao.licitacoes.get(leilao.licitacoes.size() - 1).get("author"));
                    for(User user: registados){
                        if(user.getUsername().equals(leilao.licitacoes.get(leilao.licitacoes.size() - 1).get("author")))
                            user.setvitorias(user.getVitorias()+1);
                    }
                }
                exportObjRegisted();
                exportObjAuctions();
            }
        }
    }

    //ADMIN
    public User [] statsVitorias() throws RemoteException{
        //top 10
        User current;
        User [] reply;
        if (registados.size()<10) {
            reply = new User[registados.size()];
            for (int i=0; i<registados.size(); i++){
                reply[i] = registados.get(i);
            }

        } else {
            reply = new User[10];
            reply[0] = registados.get(0);
            int min = 0;
            for (int i=1; i<reply.length; i++){
                reply[i] = registados.get(i);
                if (reply[i].getVitorias() < reply[min].getVitorias()){
                    min=i;
                }
            }
            for (int i=10; i<registados.size(); i++){
                current = registados.get(i);
                if (current.getVitorias()>reply[min].getVitorias()){
                    reply[min]=current;
                    min=0;
                    for(int k=1; k<reply.length;k++){
                        if (reply[k].getVitorias()<reply[min].getVitorias()){
                            min=k;
                        }
                    }
                }
            }
        }
        Arrays.sort(reply,User::compareVitorias);
        return reply;
    }

    public User [] statsLeiloes() throws RemoteException{
        //top 10
        User current;
        User [] reply;
        if(registados.size()<10){
             reply = new User[registados.size()];
            for (int i=0; i<registados.size(); i++){
                reply[i] = registados.get(i);
            }
        }else{
            reply = new User[10];
            reply[0] = registados.get(0);
            int min = 0;
            for (int i=1; i<reply.length; i++){
                reply[i] = registados.get(i);
                if (reply[i].getLeiloes()<reply[min].getLeiloes()){
                    min=i;
                }
            }
            for (int i=10; i<registados.size(); i++){
                current = registados.get(i);
                if (current.getLeiloes()>reply[min].getLeiloes()){
                    reply[min]=current;
                    min=0;
                    for(int k=1; k<reply.length; k++){
                        if (reply[k].getLeiloes()<reply[min].getLeiloes()){
                            min=k;
                        }
                    }
                }
            }
        }
        Arrays.sort(reply,User::compareLeiloes);
        return reply;
    }

    public int statsLastWeek(){
        int count=0;
        for (int i=0; i<leiloes.size(); i++){
            if(leiloes.get(i).lastWeek()){
                count++;
            }
        }
        return count;
    }

    // Admin
    public boolean cancelAuction (long id) throws RemoteException{
        for (Leilao current : leiloes) {
            if(current.id_leilao == id) {
                current.setState(1);
                return true;
            }
        }
        return false;
    }

    public boolean banUser (String username) throws RemoteException{//remover a conta de user?
        String lamento = "Lamentamos, o user "+username+" foi banido.";
        boolean done = false;

        for(User u : registados) {
            if(u.getUsername().equals(username)) {
                if(u.isBanned()) {
                    System.out.println("User ja se encontra banido.");
                    return false;
                } else {
                    u.setBanned(true);
                    System.out.println("Username "+u.getUsername()+" Banido: "+u.isBanned());
                    for(Leilao l : leiloes) {
                        if(l.getUsername_criador().equals(username) && l.getState() == 0) {
                            l.setState(1);
                        } else {
                            String bid = null;
                            int pos = 0;

                            // Admin escreve no mural
                            if(l.mensagens.size()>0) {
                                for (int i = 0; i < l.mensagens.size(); i++) {
                                    //System.out.println("Leilao | Autor: "+l.getUsername_criador()+" Estado: "+l.getState());
                                    LinkedHashMap<String, String> mensagem = l.mensagens.get(i);
                                    System.out.println("Mensagem: " + mensagem);
                                    if (mensagem.containsValue(username)) {
                                        if (this.warnBanned(String.valueOf(l.getId_leilao()), username)) {
                                            this.msgNotification(l, lamento, "Admin");
                                            done = true;
                                        }
                                    }
                                }
                            }
                            if(l.licitacoes.size()>0) {
                                for(int i=0;i<l.licitacoes.size();i++) {
                                    System.out.println(l.licitacoes.get(i).get("author"));
                                    LinkedHashMap<String, String> licitacao = l.licitacoes.get(i);
                                    if(licitacao.containsValue(username) && !done) {
                                        if(this.warnBanned(String.valueOf(l.getId_leilao()), username))
                                            this.msgNotification(l,lamento,"Admin");
                                    }
                                }
                            }

                            // Deleting bids do user banido
                            for(int i=0;i<l.licitacoes.size();i++){
                                LinkedHashMap<String, String> licitacao = l.licitacoes.get(i);
                                if(licitacao.containsValue(username)) {
                                    bid = licitacao.get("bid");
                                    l.licitacoes.remove(i);
                                    pos = i;
                                    i--;
                                }
                            }

                            // Ajustar bids
                            if(l.licitacoes.size()>0) {
                                LinkedHashMap <String,String> temp=l.licitacoes.get(l.licitacoes.size()-1);
                                if ((bid!=null)&&(Double.parseDouble(temp.get("bid"))<Double.parseDouble(bid))){
                                    temp.put("bid",bid);
                                    l.licitacoes.set(pos,temp);
                                    for (int i=l.licitacoes.size()-1; i>pos;i--){
                                        l.licitacoes.remove(i);
                                    }
                                }
                            }

                            // Apagar mensagens do user banido
                            for(int i=0;i<l.mensagens.size();i++) {
                                LinkedHashMap<String, String> mensagem = l.mensagens.get(i);
                                if(mensagem.containsValue(username)){
                                    l.mensagens.remove(i);
                                    i--;
                                }
                            }
                            done = false;
                        }
                    }
                    this.exportObjAuctions();
                    this.exportObjRegisted();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean warnBanned(String id, String username) throws RemoteException {
        LinkedHashMap<String, String> admin_message = new LinkedHashMap<String, String>();
        admin_message.put("id", id);
        admin_message.put("text", "Lamentamos. O user "+username+" foi banido.");
        if(this.write_message(admin_message, "Admin") == null) {
            System.out.println("Erro no aviso do admin por user banido.");
            return false;
        }
        return true;
    }

    private static void start(){
        try {
            RMI_Server h = new RMI_Server();

            Registry r = LocateRegistry.createRegistry(Integer.parseInt(primaryRmi[1]));
            r.rebind("ibei", h);
            h.importObjRegisted();
            System.out.println("[Base dados] Registados importados: "+h.registados);
            h.importObjAuctions();
            System.out.println("[Base dados] Leiloes importados: "+h.leiloes);


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

    private static void verifica(RMI_Interface h) {
        try {
         //   h= (RMI_Interface) LocateRegistry.getRegistry(primaryRmi[0], 7000).lookup("ibei");
            String teste = h.teste();
            System.out.println(teste);
            try {
                Thread.sleep(3000);
                verifica(h);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (RemoteException e){
            //if(count > 30) {
                start();
                importObjLogged();
                System.out.println("[Base dados] Users loggados importados: " + loggados);
             /*   count = 0;
            }
            else{
                try {
                    System.out.println("Waiting for Primary RMI be up again");
                    Thread.sleep(3000);
                    count += 3;
                    verifica(h);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

            }
        } catch (NotBoundException e) {
            e.printStackTrace();
        */}
    }

    public static void main(String args[]) {
        if (args.length != 2 && args.length != 4) {
            System.out.println("Usage: <primary RMI server ip> <primary RMI port>");
            System.out.println("Optional (Secondary RMI on other machine): ... <secondary RMI server ip> <secondary RMI port>");
            System.exit(0);
        } else if (args.length == 2) {
            primaryRmi[0] = args[0];
            primaryRmi[1] = args[1];
        } else {
            primaryRmi[0] = args[0];
            primaryRmi[1] = args[1];
            backupRmi[0] = args[2];
            backupRmi[1] = args[3];
        }

        System.out.println("Establish connection with RMI ip address:"+primaryRmi[0]);

        try {
            System.setProperty("java.rmi.server.hostname", primaryRmi[0]);
            RMI_Interface h = (RMI_Interface) LocateRegistry.getRegistry(primaryRmi[0], Integer.parseInt(primaryRmi[1])).lookup("ibei");
            verifica(h);
        } catch (RemoteException | NotBoundException re) {
            start();
        }
    }
}
