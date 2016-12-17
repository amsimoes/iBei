package iBei.rmi;

import iBei.aux.FacebookRest;
import iBei.aux.FicheiroDeObjeto;
import iBei.aux.Leilao;
import iBei.aux.User;
import iBei.server.TCP_Interface;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

class dbConnection{
    public Connection connection;
    public int estado;
    public int posicaoArray;

    public dbConnection(Connection connection, int estado, int posicaoArray) {
        this.connection = connection;
        this.estado = estado;
        this.posicaoArray = posicaoArray;
        try {
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

public class RMI_Server extends UnicastRemoteObject implements RMI_Interface {
    //private List<Leilao> leiloes;
    //private List<User> registados;
    //private static List<User> loggados;
    private static List<TCP_Interface> tcpServers;
    private static String primaryRmi [] = new String[2];
    private static String backupRmi [] = new String[2];
    public static String dbHost;
    static int count = 0;
    public static ArrayList <dbConnection> connections = new ArrayList<>();


    public RMI_Server() throws RemoteException {
        super();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connectionString = "jdbc:mysql://"+dbHost+":3306/bd";
            //System.out.println("Connecting to BD on host: "+dbHost);
            int i;
            for(i=0; i< 5; i++){
                connections.add(new dbConnection(DriverManager.getConnection(connectionString, "root", "root"),0,i));
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.out.println("DATABASE OFFLINE.");
            System.exit(1);
        }

        //leiloes = Collections.synchronizedList(new ArrayList<Leilao>());
        //registados = Collections.synchronizedList(new ArrayList<User>());
        //loggados = Collections.synchronizedList(new ArrayList<User>());
        tcpServers = Collections.synchronizedList(new ArrayList<TCP_Interface>());
    }

    public dbConnection getConnection(){
        int i;
        for(i = 0; i< connections.size(); i++){
            if(connections.get(i).estado == 0){
                connections.get(i).estado = 1;
                System.out.println("Connection "+i+" active");
                return connections.get(i);
            }

        }
        System.out.println("Connection "+i+" active");
        //adiciona caso nao haja nenhuma disponivel
        addConnection();
        return connections.get(connections.size()-1);

    }

    public void addConnection(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connectionString = "jdbc:mysql://"+dbHost+":3306/bd";
            connections.add(new dbConnection(DriverManager.getConnection(connectionString, "root", "root"),0,connections.size()-1));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void releaseConnection(dbConnection c){
        int i;
        for(i = 0; i< connections.size(); i++){
            //System.out.println(c.estado+" "+ c.posicaoArray);
            if(i == c.posicaoArray){
                connections.get(i).estado = 0;
                break;
            }
        }
        System.out.println("Connection "+i+" released");
    }

    public void addTCP(TCP_Interface tcp) throws RemoteException {
        tcpServers.add(tcp);
        //this.exportObjTCPServers();
    }

    public boolean register_client(LinkedHashMap<String, String> data) throws RemoteException {
        System.out.println("[ REGISTER CLIENT ]");
        ArrayList <User> users = new ArrayList<>();

        dbConnection c = getConnection();
        Connection connection = c.connection;

        PreparedStatement statement = null;
        String username = data.get("username");
        String password = data.get("password");
        String query = "SELECT * FROM user WHERE user.username = ?;";

        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet response = statement.executeQuery();
            users = getArraylistUsers(response, connection);
            if(users.size()!=0){
                System.out.println(users.size());
                releaseConnection(c);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            releaseConnection(c);
            return false;
        }

        query= "INSERT INTO user (username, password, banido, leiloes, vitorias, logado) VALUES (?, ? ,false ,0,0,false);";

        try {
            statement = connection.prepareStatement(query);
            statement.setString(1,username);
            statement.setString(2,password);
            statement.execute();
            connection.commit();

        } catch (SQLException e) {

            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();

        }
        releaseConnection(c);
        return true;
    }

    
    //TODO: TEMOS DE VER EM QUE CASOS TEMOS DE FAZER SELECT ... FOR UPDATE(slides)
    public boolean login_client(LinkedHashMap<String, String> data) throws RemoteException {
        //DEVEMOS RETORNAR O PROBLEMA??
        System.out.println("[ LOGIN CLIENT ] ");
        ArrayList <User> users = new ArrayList<>();

        dbConnection c = getConnection();
        Connection connection = c.connection;

        PreparedStatement statement = null;
        String username = data.get("username");
        String password = data.get("password");
        String query = "SELECT * FROM user WHERE user.username = ? AND user.password = ? AND user.banido = false AND user.logado = false;";
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet response = statement.executeQuery();
            users = getArraylistUsers(response, connection);
            if(users.size()!=1){
                releaseConnection(c);
                return false;
            }



        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
            releaseConnection(c);
            return false;
        }

        query= "UPDATE user SET logado = TRUE WHERE username = ?;";

        try {
            statement = connection.prepareStatement(query);
            statement.setString(1,username);
            Boolean result = statement.execute();
            //System.out.println("LOGIN: "+result);
            connection.commit();

        } catch (SQLException ex) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            ex.printStackTrace();

        }
        releaseConnection(c);
        return true;

    }

    public boolean logoutClient(String username) throws RemoteException {
        PreparedStatement statement = null;


        dbConnection c = getConnection();
        Connection connection = c.connection;

        ArrayList <User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE user.username = ? AND user.logado = true;";
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet response = statement.executeQuery();
            users = getArraylistUsers(response,connection);
            if(users.size()!=1) {
                releaseConnection(c);
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        query= "UPDATE user SET logado = FALSE WHERE username = ?;";

        try {
            statement = connection.prepareStatement(query);
            statement.setString(1,username);
            Boolean result = statement.execute();
            System.out.println("LOGOUT: "+result);
            connection.commit();

        } catch (SQLException ex) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            ex.printStackTrace();

        }
        releaseConnection(c);
        return true;
    }

    public boolean create_auction(LinkedHashMap<String, String> data, String username) throws RemoteException{
        System.out.println("[ CREATE_AUCTION ]");
        //parse data
        String query;

        dbConnection c = getConnection();
        Connection connection = c.connection;

        PreparedStatement statement = null;
        try {
            String code = data.get("code");
            double amount = Double.parseDouble(data.get("amount"));
            String titulo = data.get("title");
            String descricao = data.get("description");
            DateFormat d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date date = d1.parse(data.get("deadline"));

            query = "INSERT into leilao (username, artigoid, leilaoid, titulo, descricao, data, data_inicio, precomax, estado) VALUES (?, ?, null, ?, ?, ?, NOW(), ?, 0)";


            statement = connection.prepareStatement(query);
            statement.setString(1,username);
            statement.setString(2,code);
            statement.setString(3,titulo);
            statement.setString(4,descricao);
            statement.setTimestamp(5, new Timestamp(date.getTime()));
            statement.setDouble(6,amount);
            statement.execute();//return true or false
            connection.commit();

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback");
            }
            e.printStackTrace();
            releaseConnection(c);
            return false;
        } catch (ParseException e) {
            //e.printStackTrace();
            System.out.println("Date format invalid, it must be like: yyyy-MM-dd HH:mm:ss");
            releaseConnection(c);
            return false;
        }


        query= "UPDATE user SET leiloes = leiloes +1 WHERE username = ?;";

        try {
            statement = connection.prepareStatement(query);
            statement.setString(1,username);
            statement.execute();//return true or false
            connection.commit();

            } catch (SQLException e) {
                System.out.println("Rollback");
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    System.out.println("Error doing rollback!");
                }
                e.printStackTrace();
        }

        // Facebook aqui
        try {
            String fb_query = "SELECT facebook FROM user u WHERE u.username = ?";
            statement = connection.prepareStatement(fb_query);
            statement.setString(1, username);
            ResultSet response = statement.executeQuery();
            String token = response.getString("facebook");
            FacebookRest fb = new FacebookRest(username, token);
            fb.postAuction("TEST FROM RMIIIII");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        releaseConnection(c);
        return true;
    }

    public ArrayList <Leilao> search_auction(LinkedHashMap<String, String> data) throws RemoteException {
        System.out.println("[ SEARCH_AUCTION ]");
        String query = "SELECT * FROM leilao WHERE artigoid=?;";
        String code = data.get("code");
        PreparedStatement statement = null;

        dbConnection c = getConnection();
        Connection connection = c.connection;

        try {
            statement = connection.prepareStatement(query);
            statement.setString(1,code);
            ResultSet response = statement.executeQuery();
  /*          System.out.println("Response: ");
            while(response.next())
                System.out.println(response.getLong("leilaoid"));
*/
            ArrayList <Leilao> leiloes = getArraylistLeiloes(response, connection);
            releaseConnection(c);
            return (leiloes);

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }

        releaseConnection(c);
        return null;

    }

    
    public Leilao detail_auction(LinkedHashMap<String, String> data) throws RemoteException{
        System.out.println("[ DETAIL_AUCTION ]");
        String script = "SELECT leilao.leilaoid leilaoid, leilao.username username, leilao.artigoid artigoid, leilao.titulo titulo, leilao.descricao descricao, leilao.precomax precomax, leilao.data data FROM leilao WHERE leilaoid = ?;";
        PreparedStatement statement = null;

        dbConnection c = getConnection();
        Connection connection = c.connection;

        ArrayList <Leilao> resp = new ArrayList<>();
        try {
            statement = connection.prepareStatement(script);
            statement.setInt(1, Integer.parseInt(data.get("id")));
            ResultSet response = statement.executeQuery();

            resp = getArraylistLeiloes(response, connection);
            if(resp.size() == 1) {
                releaseConnection(c);
                return (resp.get(0));

            }
        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }

        releaseConnection(c);
        return null;
    }

    public ArrayList <Leilao> my_auctions(LinkedHashMap<String, String> data, String username) throws RemoteException{
        System.out.println("[ MY_AUCTIONS ]");

        dbConnection c = getConnection();
        Connection connection = c.connection;

        String query = "SELECT leilao.leilaoid leilaoid, leilao.username username, leilao.artigoid artigoid, leilao.titulo titulo, leilao.descricao descricao, leilao.precomax precomax, leilao.data data FROM leilao, ("+
                      "                    SELECT licitacao.leilaoid id FROM licitacao WHERE licitacao.username = ?"+
                      "                    UNION"+
                      "                    SELECT mensagem.leilaoid id FROM mensagem WHERE mensagem.username = ?"+
                      "                    UNION"+
                      "                    SELECT leilao.leilaoid id FROM leilao WHERE username = ?) idLeiloes "+
                      "WHERE idLeiloes.id = leilao.leilaoid";

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(query);
            statement.setString(1,username);
            statement.setString(2,username);
            statement.setString(3,username);
            ResultSet response = statement.executeQuery();
            System.out.println("Response: ");
            /*while(response.next())
                System.out.println(response.getInt("leilaoid"));
*/

            ArrayList <Leilao> leiloes = getArraylistLeiloes(response, connection);
            releaseConnection(c);
            return (leiloes);

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }

        releaseConnection(c);
        return null;

    }

    public ArrayList <Leilao> getArraylistLeiloes(ResultSet response, Connection connection){
        ArrayList <Leilao> leiloes = new ArrayList<>();
        PreparedStatement statement;

        try {
            while(response.next()) {
                Timestamp date = response.getTimestamp("data");
                DateFormat d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String d = d1.format(date);
                int leilaoId = response.getInt("leilaoid");
                java.util.Date data = d1.parse(d);
                System.out.println(response.getInt("leilaoid"));
                Leilao leilao = new Leilao(response.getString("username"),response.getString("artigoid"),response.getString("titulo"),response.getString("descricao"),response.getDouble("precomax"),data, leilaoId);

                String queryBids = "SELECT licitacao.username username, licitacao.quantia amount FROM licitacao WHERE licitacao.leilaoid = ?;";

                statement = connection.prepareStatement(queryBids);
                statement.setInt(1, leilaoId);
                ResultSet result = statement.executeQuery();
                while(result.next()) {
                    LinkedHashMap<String, String> my_bid = new LinkedHashMap<String, String>();
                    my_bid.put("author", result.getString("username"));
                    my_bid.put("bid", String.valueOf(result.getDouble("amount")));
                    leilao.licitacoes.add(my_bid);

                }

                String queryMessages = "SELECT mensagem.username username, mensagem.texto text FROM mensagem WHERE mensagem.leilaoid = ?;";

                statement = connection.prepareStatement(queryMessages);
                statement.setInt(1, leilaoId);
                result = statement.executeQuery();
                while(result.next()) {
                    LinkedHashMap<String, String> my_message = new LinkedHashMap<String, String>();
                    my_message.put("author", result.getString("username"));
                    my_message.put("message", result.getString("text"));
                    leilao.mensagens.add(my_message);

                }
                leilao.printInfo();
                leiloes.add(leilao);
            }

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return leiloes;
    }

    public ArrayList <User> getArraylistUsers(ResultSet response, Connection connection){
        ArrayList <User> users = new ArrayList<>();

        try {
            while(response.next()) {
                users.add(new User(response.getString("username"), "default"));
            }

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }


        return users;
    }

    public boolean edit_auction(LinkedHashMap<String, String> data, String username) throws RemoteException {
        //TRATAR DE AUTO INCREMENT NA VERSAO
        System.out.println("[ EDIT AUCTION ]");
        PreparedStatement statement = null;
        Boolean result = false;

        dbConnection c = getConnection();
        Connection connection = c.connection;

        int count=1;
        boolean title = false, descricao= false, date= false, precoMax= false;

        String query = "UPDATE leilao "
                +"SET ";

        if (data.containsKey("title")) {
            query += "titulo = ?, ";
            title = true;
        }
        if (data.containsKey("description")) {
            query += "descricao = ?, ";
            descricao = true;
        }
        if (data.containsKey("deadline")) {
            query += "data = ?, ";
            date = true;
        }
        if (data.containsKey("amount")) {
            query += "precomax = ?, ";
            precoMax = true;
        }
        if(!title && !descricao && !date && !precoMax) {
            releaseConnection(c);
            return false;
        }


        String insert = "INSERT INTO historico (leilaoid, versaoid, titulo, descricao, data, precomax)" +
                    "    SELECT l.leilaoid, 0, l.titulo, l.descricao, l.data, l.precomax FROM leilao l" +
                    "        WHERE l.leilaoid = ? AND l.username = ?;";


        try {
            statement = connection.prepareStatement(insert);
            statement.setLong(1, Long.parseLong(data.get("id")));
            statement.setString(2,username);
            if(statement.executeUpdate() <= 0){
                System.out.println("Something is wrong");
                releaseConnection(c);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }




        query = query.substring(0, query.lastIndexOf(","));
        query += " WHERE leilaoid = ?;";

        try {
            statement = connection.prepareStatement(query);
            if(title){
                statement.setString(count,data.get("title"));
                count++;
            }
            if(descricao){
                statement.setString(count,data.get("description"));
                count++;
            }
            if(date){
                DateFormat d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date d = d1.parse(data.get("deadline"));
                statement.setTimestamp(count, new Timestamp(d.getTime()));
                count++;
            }
            if(precoMax){
                statement.setDouble(count, Double.parseDouble(data.get("amount")));
                count++;
            }

            System.out.println(query);
            statement.setString(count,data.get("id"));
            result = statement.execute();
            connection.commit();

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("date format invalid!");
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }

        releaseConnection(c);
        return true;
    }

    public List<User> listOnlineUsers() throws RemoteException {
        String query = "SELECT * FROM user WHERE logado=true;";

        dbConnection c = getConnection();
        Connection connection = c.connection;

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(query);
            ResultSet response = statement.executeQuery();

            ArrayList <User> users = getArraylistUsers(response, connection);
            releaseConnection(c);
            return users;

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }

        releaseConnection(c);
        return null;
    }


    public Leilao make_bid(LinkedHashMap<String, String> data, String username) throws RemoteException {
        //FAZER BLOQUEIO A FUNCAO??
        //INFORMAR O QUE E QUE FALHOU??
        //NECESSIDADE DATAS ??
        //TODO:VERIFICACAO DE VALIDADE DE BID CORRETA(if...,max()...)??

        dbConnection c = getConnection();
        Connection connection = c.connection;

        ArrayList <Leilao> aux = new ArrayList<>();
        Long id = Long.parseLong(data.get("id"));
        Double amount = Double.parseDouble(data.get("amount"));
        System.out.println("amount: "+amount);
        PreparedStatement statement = null;
        String query =  "SELECT * FROM leilao " +
                        "WHERE leilao.leilaoid = ? AND leilao.estado = 0 " + /*" AND leilao.data > NOW() "*/
                        "AND leilao.precomax > ? AND ? < (SELECT IFNULL(MIN(licitacao.quantia),leilao.precomax) FROM licitacao WHERE licitacao.leilaoid = ?) " +
                        "FOR UPDATE;";

        try {
            statement = connection.prepareStatement(query);
            statement.setLong(1, id);
            statement.setDouble(2, amount);
            statement.setDouble(3, amount);
            statement.setLong(4, id);
            ResultSet response = statement.executeQuery();
            aux = getArraylistLeiloes(response, connection);
            if (aux.size() != 1){
                System.out.println("size: "+aux.size());
                connection.commit();//por causa do FOR UPDATE
                releaseConnection(c);
                return null;
            }

            //connection.prepareStatement("LOCK TABLES licitacao AS l1 WRITE, licitacao AS l2 WRITE, licitacao WRITE;").execute();

            query = "INSERT INTO licitacao (leilaoid, username, quantia) VALUES (?, ?, ?);";
            statement = connection.prepareStatement(query);
            statement.setLong(1, id);
            statement.setString(2, username);
            statement.setDouble(3, amount);
            statement.execute();

            //connection.commit();

            String licitacao =  "SELECT l1.licitacaoid id, l1.username usernameSender, l1.quantia amount, l1.leilaoid leilaoId FROM licitacao l1 WHERE " +
                    "l1.licitacaoid = (SELECT MAX(licitacaoid) FROM licitacao l2);";
            int bidId = 0, leilaoId = 0;
            String usernameSender="";
            try {
                statement = connection.prepareStatement(licitacao);
                response = statement.executeQuery();
                while(response.next()){
                    bidId = response.getInt("id");
                    usernameSender = response.getString("usernameSender");
                    leilaoId = response.getInt("leilaoId");
                }

            } catch (SQLException e) {
                System.out.println("Rollback");
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    System.out.println("Error doing rollback!");
                }
                e.printStackTrace();
            }



            //connection.prepareStatement("UNLOCK TABLES;").execute();
            bidNotification(leilaoId, usernameSender, bidId, connection);
            connection.commit();
            releaseConnection(c);
            return aux.get(0);

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }

        releaseConnection(c);
        return null;
    }


    public void bidNotification(int leilaoId, String usernameSender, int bidId, Connection connection) throws RemoteException {
        //DEVO USAR VIEWS E ESTAR SEMPRE A CRIAR E A ELIMINAR??
        //EXISTE GARANTIA QUE ISTO E CORRIDO A SEGUIR A FAZER A BID?? NO MAKE BID DEVIAMOS MANDAR NOTIFICACAO ANTES DE FAZER UNLOCK A TABLE
        PreparedStatement statement = null;
        String query =  "CREATE VIEW lic_anteriores " +
                        "AS " +
                        "SELECT DISTINCT licitacao.username username FROM licitacao" +
                        "      WHERE licitacao.leilaoid = ? AND licitacao.username <> ?" +
                        "        GROUP BY licitacao.leilaoid, licitacao.username " +
                        "UNION " +
                        "SELECT user.username FROM user, leilao " +
                        "WHERE user.username = leilao.username AND leilao.leilaoid = ?; ";


        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1,leilaoId);
            statement.setString(2,usernameSender);
            statement.setInt(3,leilaoId);
            statement.execute();


        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }

        query = "INSERT INTO notificacao_licitacao (notificacaolicid, licitacaoid, username_receiver)" +
                "    SELECT 0, ?, lic_anteriores.username FROM lic_anteriores;";

        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1,bidId);
            statement.execute();
            connection.commit();
            connection.prepareStatement("DROP VIEW lic_anteriores;").execute();

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }

    }


    public Leilao write_message(LinkedHashMap<String, String> data, String username) throws RemoteException {
        //if...e max()...??

        int id = Integer.parseInt(data.get("id"));
        PreparedStatement statement = null;
        String text = data.get("text");
        ArrayList <Leilao> aux = new ArrayList<>();

        dbConnection c = getConnection();
        Connection connection = c.connection;

        String query = "SELECT * FROM leilao " +
                       "WHERE leilao.leilaoid = ? AND leilao.estado = 0 FOR UPDATE";

        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            ResultSet response = statement.executeQuery();
            /*System.out.println("Response: ");
            while(response.next())
                System.out.println(response.getString("leilaoid"));*/

            aux = getArraylistLeiloes(response, connection);
            if(aux.size() != 1) {
                connection.commit();
                releaseConnection(c);
                return null;
            }

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }

        try {
            query = "INSERT INTO mensagem (leilaoid, username, texto) VALUES (?, ?, ?);";
            statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            statement.setString(2, username);
            statement.setString(3, text);
            statement.execute();

            //connection.commit();

            String msg =    "SELECT mensagem.mensagemid id, mensagem.username usernameSender, mensagem.texto text, mensagem.leilaoid leilaoId FROM mensagem WHERE " +
                    "mensagem.mensagemid= (SELECT MAX(mensagemid) FROM mensagem);";
            int msgId = 0, leilaoId = 0;
            String usernameSender="";

            try {
                statement = connection.prepareStatement(msg);
                ResultSet response = statement.executeQuery();
                while(response.next()){
                    msgId = response.getInt("id");
                    usernameSender = response.getString("usernameSender");
                    leilaoId = response.getInt("leilaoId");
                }



            } catch (SQLException e) {
                e.printStackTrace();
            }
            msgNotification(leilaoId, usernameSender, msgId, connection);
            releaseConnection(c);
            return aux.get(0);

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }

        releaseConnection(c);
        return null;

       }


    public void msgNotification(int leilaoId, String usernameSender, int msgId, Connection connection) throws RemoteException {

        PreparedStatement statement = null;
        String query =  "CREATE VIEW user_anteriores " +
                        "AS " +
                        "SELECT  licitacao.username username FROM licitacao " +
                        "WHERE licitacao.leilaoid = ? AND licitacao.username <> ? " +
                        "GROUP BY licitacao.leilaoid, licitacao.username " +
                        "UNION " +
                        "SELECT  mensagem.username username FROM mensagem " +
                        "WHERE mensagem.leilaoid = ? AND mensagem.username <> ? " +
                        "GROUP BY mensagem.leilaoid, mensagem.username " +
                        "UNION " +
                        "SELECT user.username FROM user, leilao " +
                        "WHERE user.username = leilao.username AND leilao.leilaoid = ?; ";


        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1,leilaoId);
            statement.setString(2,usernameSender);
            statement.setInt(3,leilaoId);
            statement.setString(4,usernameSender);
            statement.setInt(5,leilaoId);
            statement.execute();


        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }

        query = "INSERT INTO notificacao_mensagem (notificacaomsgid, mensagemid, username_receiver)" +
                "    SELECT 0, ?, user_anteriores.username FROM user_anteriores;";

        try {
            statement = connection.prepareStatement(query);
            statement.setInt(1,msgId);
            statement.execute();
            connection.commit();
            connection.prepareStatement("DROP VIEW user_anteriores;").execute();

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }


    }

    public void checkBidNotf_clients() throws RemoteException{
        //QUE CONNECTION DEVEMOS USAR??

        dbConnection c = getConnection();
        Connection connection = c.connection;

            try {
                String query =  "SELECT leilaoid idLeilao, lic.licitacaoid licitacaoId, lic.username username, lic.quantia amount, username_receiver userReceiver  FROM notificacao_licitacao notf, licitacao lic " +
                        "WHERE notf.licitacaoid = lic.licitacaoid AND username_receiver IN (SELECT user.username FROM user WHERE user.logado = true);";

                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet response = statement.executeQuery();

                while(response.next()){
                    String username = response.getString("username");
                    String userReceiver = response.getString("userReceiver");
                    Double quantia = response.getDouble("amount");
                    int leilaoId = response.getInt("idLeilao");
                    for (TCP_Interface s : tcpServers) {
                        if(s.checkUser(userReceiver)) {
                            s.sendMsg("notification_bid", userReceiver, String.valueOf(quantia), leilaoId, username);
                        }
                    }
                }

                query = "DELETE FROM notificacao_licitacao WHERE username_receiver IN (SELECT user.username FROM user WHERE user.logado = true);";

                statement = connection.prepareStatement(query);
                statement.execute();
                connection.commit();

            } catch (SQLException e) {
                System.out.println("Rollback");
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    System.out.println("Error doing rollback!");
                }
                e.printStackTrace();
            }
        releaseConnection(c);

    }

    public void checkMsgNotf_clients() throws RemoteException{
        //QUE CONNECTION DEVEMOS USAR??

        dbConnection c = getConnection();
        Connection connection = c.connection;

        try {
            String query =  "SELECT msg.leilaoid idLeilao, msg.mensagemid mensagemId, msg.username username, msg.texto text, username_receiver userReceiver FROM notificacao_mensagem notf, mensagem msg " +
                    "WHERE notf.mensagemid = msg.mensagemid AND username_receiver IN (SELECT user.username FROM user WHERE user.logado = true);";

            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet response = statement.executeQuery();

            while(response.next()){
                String username = response.getString("username");
                String text = response.getString("text");
                String userReceiver = response.getString("userReceiver");
                int leilaoId = response.getInt("idLeilao");
                for (TCP_Interface s : tcpServers) {
                    if(s.checkUser(userReceiver)) {
                        s.sendMsg("notification_message", userReceiver, text, leilaoId, username);
                    }
                }
            }

            query = "DELETE FROM notificacao_mensagem WHERE username_receiver IN (SELECT user.username FROM user WHERE user.logado = true);";
            statement = connection.prepareStatement(query);
            statement.execute();
            connection.commit();

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }

        releaseConnection(c);
    }

    // FicheirosObjetos

    private synchronized static void importObjTCPServers(){
        FicheiroDeObjeto file = new FicheiroDeObjeto();
        try {
            file.abreLeitura("iBei"+File.separator+"aux"+File.separator+"tcpServers.ser");
            tcpServers = (List<TCP_Interface>) file.leObjeto();
            file.fechaLeitura();
        } catch (IOException e) {
            System.out.println("File with TCPServers in users empty.");
        } catch (ClassNotFoundException e1) {
            System.out.println("Classe ArrayList/User not found.");
        }
    }

    private synchronized void exportObjTCPServers() {
        FicheiroDeObjeto file = new FicheiroDeObjeto();
        try {
            file.abreEscrita("iBei"+File.separator+"aux"+File.separator+"tcpServers.ser");
            file.escreveObjeto(tcpServers);
            file.fechaEscrita();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public ArrayList<ArrayList<Object>> checkBidNotf_clientsWebSockets() throws RemoteException{
        //QUE CONNECTION DEVEMOS USAR??

        dbConnection c = getConnection();
        Connection connection = c.connection;
        ArrayList<ArrayList<Object>> outer = new ArrayList<ArrayList<Object>>();
        try {

            String query =   "SELECT leilaoid idLeilao, lic.licitacaoid licitacaoId, lic.username username, lic.quantia amount, username_receiver userReceiver  FROM notificacao_licitacao notf, licitacao lic " +
                    "WHERE notf.licitacaoid = lic.licitacaoid AND username_receiver IN (SELECT user.username FROM user WHERE user.logado = true);";

            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet response = statement.executeQuery();


            while(response.next()){

                String userReceiver = response.getString("userReceiver");

                ArrayList <Object> aux = new ArrayList<>();
                aux.add(response.getInt("idLeilao"));
                aux.add(String.valueOf(response.getDouble("amount")));
                aux.add(response.getString("username"));
                aux.add(userReceiver);
                outer.add(aux);
            }

            query = "DELETE FROM notificacao_licitacao WHERE username_receiver IN (SELECT user.username FROM user WHERE user.logado = true);";

            statement = connection.prepareStatement(query);
            statement.execute();
            connection.commit();

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }
        releaseConnection(c);
        return outer;
    }

    public ArrayList<ArrayList<Object>> checkMsgNotf_clientsWebSockets() throws RemoteException{
        //QUE CONNECTION DEVEMOS USAR??
        ArrayList <Object> result = new ArrayList<>();
        ArrayList<ArrayList<Object>> outer = new ArrayList<ArrayList<Object>>();
        dbConnection c = getConnection();
        Connection connection = c.connection;

        try {
            String query =  "SELECT msg.leilaoid idLeilao, msg.mensagemid mensagemId, msg.username username, msg.texto text, username_receiver userReceiver FROM notificacao_mensagem notf, mensagem msg " +
                    "WHERE notf.mensagemid = msg.mensagemid AND username_receiver IN (SELECT user.username FROM user WHERE user.logado = true);";

            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet response = statement.executeQuery();

            //boolean f = false;
            while(response.next()){
                //String username = response.getString("username");
                //String text = response.getString("text");
                String userReceiver = response.getString("userReceiver");
                //int leilaoId = response.getInt("idLeilao");

                ArrayList <Object> aux = new ArrayList<>();
                aux.add(response.getInt("idLeilao"));
                aux.add(response.getString("text"));
                aux.add(response.getString("username"));
                aux.add(userReceiver);
                outer.add(aux);
                //result.add(aux);
            }

            query = "DELETE FROM notificacao_mensagem WHERE username_receiver IN (SELECT user.username FROM user WHERE user.logado = true);";
            statement = connection.prepareStatement(query);
            statement.execute();
            connection.commit();

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }

        releaseConnection(c);
        return outer;
    }


    private void verifica_terminoLeiloes(){

        dbConnection c = getConnection();
        Connection connection = c.connection;

        //view auxiliar com o id e nome do atual vencedor de todos os leiloes
        String aux =    "CREATE VIEW aux " +
                        "AS " +
                        "SELECT licitacao.username nome, leilaoid leilaoid FROM licitacao " +
                        "WHERE (quantia, leilaoid) IN (SELECT MIN(quantia) minLic, leilaoid leilaoid FROM licitacao GROUP BY leilaoid);";

        //view com nome e i id dos leiloes que acabaram
        String user_vencedoresView ="CREATE VIEW user_vencedores " +
                                    "AS " +
                                    "SELECT leilao.leilaoid id, vencedores.nome nome FROM leilao, aux vencedores " +
                                    "WHERE leilao.estado = 0 AND leilao.data < NOW() AND leilao.leilaoid = vencedores.leilaoid " +
                                    "UNION " +
                                    "SELECT leilao.leilaoid, null FROM leilao WHERE leilao.estado = 0 AND leilao.data < NOW() AND leilaoid NOT IN (SELECT licitacao.leilaoid FROM licitacao GROUP BY licitacao.leilaoid);";

        try {

            PreparedStatement statement = connection.prepareStatement(aux);
            statement.execute();

            statement = connection.prepareStatement(user_vencedoresView);
            statement.execute();


            String query = "SELECT * FROM user_vencedores;";

            statement = connection.prepareStatement(query);
            ResultSet response = statement.executeQuery();
            while(response.next()){
                int id = response.getInt("id");
                String nome = response.getString("nome");
                if(nome != null)
                    System.out.println("Winner of auction with id "+id+": "+nome);
                else
                    System.out.println("Auction with id: "+id+" had no bids.");

            }

            query = "UPDATE user SET vitorias = vitorias + (SELECT COUNT(*) FROM user_vencedores WHERE nome = user.username);";
            statement = connection.prepareStatement(query);
            statement.execute();


            query = "UPDATE leilao SET estado = 2 WHERE data < NOW();";
            statement = connection.prepareStatement(query);
            statement.execute();

            connection.prepareStatement("DROP VIEW aux, user_vencedores;").execute();

            connection.commit();

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }

    releaseConnection(c);
    }

    public User []  getArrayUsers(ResultSet response, Connection connection){

        ArrayList <User> users = new ArrayList<>();

        int i;
        try {
            while(response.next()) {
                users.add(new User(response.getString("username"), "default"));
                users.get(users.size()-1).setLeiloes(response.getInt("dados"));
                users.get(users.size()-1).setvitorias(response.getInt("dados"));
            }
            User [] array = new User[users.size()];
            for(i=0; i< array.length; i++){
                array[i] = users.get(i);
            }
            return array;

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }


        return new User[0];
    }

    //ADMIN
    public User [] statsVitorias() throws RemoteException{
        //top

        dbConnection c = getConnection();
        Connection connection = c.connection;

        User [] users = new User[10];
        String query = "SELECT username, vitorias dados FROM user ORDER BY 2 ASC LIMIT 10;";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet response = statement.executeQuery();
            users = getArrayUsers(response,connection);

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback");
            }
            e.printStackTrace();
        }

        releaseConnection(c);
        return users;

    }

    public User [] statsLeiloes() throws RemoteException{
        //top 10

        dbConnection c = getConnection();
        Connection connection = c.connection;

        User [] users = new User[10];
        String queryLeiloes = "SELECT username, leiloes dados FROM user ORDER BY 2 ASC LIMIT 10;";
        try {
            PreparedStatement statement = connection.prepareStatement(queryLeiloes);
            ResultSet resp = statement.executeQuery();
            users = getArrayUsers(resp,connection);

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();
        }

        releaseConnection(c);
        return users;
    }

    public int statsLastWeek(){

        dbConnection c = getConnection();
        Connection connection = c.connection;

        PreparedStatement statement = null;
        String query = "SELECT COUNT(*) contador FROM leilao where data >= DATE_SUB(NOW(), INTERVAL 10 DAY);";

        try {
            statement = connection.prepareStatement(query);
            ResultSet response = statement.executeQuery();
            while(response.next()){
                releaseConnection(c);
                return response.getInt("contador");
            }

            connection.commit();

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();


        }
        releaseConnection(c);
        return 0;
    }

    public boolean cancelAuction (long id) throws RemoteException{
        PreparedStatement statement = null;

        dbConnection c = getConnection();
        Connection connection = c.connection;

        String query = "UPDATE leilao SET estado = 1 WHERE leilaoid = ?;";

        try {
            statement = connection.prepareStatement(query);
            statement.setLong(1,id);
            System.out.println(statement.execute());//return true or false

            connection.commit();

        } catch (SQLException e) {
            System.out.println("Rollback");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback!");
            }
            e.printStackTrace();


        }
        releaseConnection(c);
        return true;
    }

    public boolean banUser (String username) throws RemoteException{//remover a conta de user?

        dbConnection c = getConnection();
        Connection connection = c.connection;

        PreparedStatement statement = null;
        String ban_query="UPDATE user SET banido = true WHERE username = ?;";
        String cancel_query="UPDATE leilao SET estado = 1 WHERE username = ? AND estado = 0;";
        String msg_query="INSERT INTO mensagem (leilaoid,username,texto) SELECT licitacao.leilaoid, \"admin\" , \"Im sorry, the user "+username+" was banned\"  FROM licitacao WHERE username = ? GROUP BY leilaoid;";

        String bid_query = "UPDATE licitacao a SET quantia = (SELECT min(quantia) FROM (SELECT * FROM licitacao) b WHERE b.leilaoid = a.leilaoid AND b.username = ? ) " +
                                "WHERE a.quantia = (SELECT min(quantia) FROM (SELECT * FROM licitacao) d WHERE d.leilaoid = a.leilaoid) " +
                                "AND EXISTS(SELECT * FROM (SELECT * FROM licitacao) c WHERE c.leilaoid = a.leilaoid AND c.username = ? );";

        String del_bid_user_query = "DELETE FROM licitacao WHERE username = ? AND leilaoid IN " +
                                    "(SELECT leilaoid FROM leilao WHERE estado = 0);";



        String del_bid_query = "DELETE a FROM licitacao a WHERE " +
                                    "a.licitacaoid < ALL(SELECT b.licitacaoid FROM (SELECT * FROM licitacao) b WHERE b.quantia = (SELECT min(c.quantia) FROM (SELECT * FROM licitacao) c WHERE c.leilaoid=b.leilaoid)) " +
                                    "AND a.quantia<=(SELECT min(d.quantia) FROM (SELECT * FROM licitacao) d WHERE d.leilaoid=a.leilaoid);";

        try {
            statement = connection.prepareStatement(ban_query);
            statement.setString(1,username);
            statement.execute();

            statement = connection.prepareStatement(cancel_query);
            statement.setString(1,username);
            statement.execute();

            statement = connection.prepareStatement(msg_query);
            statement.setString(1,username);
            statement.execute();

            statement = connection.prepareStatement(bid_query);
            statement.setString(1,username);
            statement.setString(2,username);
            statement.execute();

            System.out.println("passou");
            statement = connection.prepareStatement(del_bid_user_query);
            statement.setString(1,username);
            statement.execute();

            statement = connection.prepareStatement(del_bid_query);
            statement.execute();


            connection.commit();

            releaseConnection(c);
            return true;

        } catch (SQLException e) {
            System.out.println("Rollback!");
            try {
                connection.rollback();
            } catch (SQLException e1) {
                System.out.println("Error doing rollback");
            }
            e.printStackTrace();
            releaseConnection(c);
            return false;
        }


    }


    private static void start(){
        try {
            RMI_Server h = new RMI_Server();

            Registry r = LocateRegistry.createRegistry(Integer.parseInt(primaryRmi[1]));
            r.rebind("ibei", h);

            System.out.println("RMI Server ready.");

            //thread para verificar o termino dos leiloes

            new Thread() {
                public void run() {
                    while (true) {
                        try {
                            //h.verifica_terminoLeiloes();
                            Thread.sleep(60000);
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

    public synchronized String teste() throws RemoteException {
        return "Check primary RMI server";
    }

    private synchronized static void verifica(RMI_Interface h) {
        try {
            h= (RMI_Interface) LocateRegistry.getRegistry(primaryRmi[0], 7000).lookup("ibei");
            String teste = h.teste();
            System.out.println(teste);
            try {
                Thread.sleep(3000);
                verifica(h);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (RemoteException e){
            if(count > 30) {
                start();
  /*              importObjLogged();
                importObjTCPServers();
                System.out.println("[Base dados] Users loggados imported: " + loggados);
    */           count = 0;
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
        }
    }

    public static void main(String args[]) {
        if (args.length != 3 && args.length != 4) {
            System.out.println("Usage: <Primary RMI Host> <RMI Port> <DB Host>");
            System.out.println("Optional (Secondary RMI on other machine): ... <secondary RMI server ip> <secondary RMI port>");
            System.exit(0);
        } else if (args.length == 3) {
            primaryRmi[0] = args[0];
            primaryRmi[1] = args[1];
            dbHost = args[2];
        } else {
            primaryRmi[0] = args[0];
            primaryRmi[1] = args[1];
            backupRmi[0] = args[2];
            backupRmi[1] = args[3];
        }

        System.out.println("Establish connection with RMI Host: "+primaryRmi[0]);

        try {
            System.setProperty("java.rmi.server.hostname", primaryRmi[0]);
            RMI_Interface h = (RMI_Interface) LocateRegistry.getRegistry(primaryRmi[0], Integer.parseInt(primaryRmi[1])).lookup("ibei");
            verifica(h);
        } catch (RemoteException | NotBoundException re) {
            start();
        }
    }
}
