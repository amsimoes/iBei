import java.io.*;
import java.net.*;
import java.util.*;
public class Leilao implements Serializable{
    public String username_criador;
    public String artigoId;
    public long id_leilao;
    public ArrayList <String> titulo;
    public ArrayList <String> descricao;
    public double precoMax;
    public int state;//0 se estiver ativo, 1 se tiver cancelado, 2 se tiver terminado
    public Date data_inicio;
    public Date data_termino;
    public ArrayList<LinkedHashMap <String, String >> mensagens;//key: author, message
    public ArrayList<LinkedHashMap <String, String >> licitacoes;//key: author, bid


    public Leilao(String username, String artigoId, String titulo, String descricao, double precoMax, Date data_termino){
        this.username_criador = username;
        this.artigoId = artigoId;
        this.titulo = new ArrayList<String>();
        this.titulo.add(titulo);
        this.descricao = new ArrayList<String>();
        this.descricao.add(descricao);
        this.precoMax = precoMax;
        this,data_inicio=new Date();
        this.data_termino = data_termino;
        System.out.println("data_termino: "+data_termino);
        System.out.println("this.data_termino: "+this.data_termino);
        this.mensagens = new ArrayList<LinkedHashMap <String, String>>();
        this.licitacoes = new ArrayList<LinkedHashMap <String, String>>();
        //random number generator
        this.id_leilao = new Date().getTime();
        this.state = 0;

    }

    public void printInfo(){
        System.out.print("\nDescricao: ");
        for(String d : descricao){
            System.out.print(d+", ");
        }
        System.out.println("\nData do termino: "+this.data_termino.toString());


        if(licitacoes.size() == 0)
            System.out.println("Nao foram feitas ainda nenhumas licitacoes");
        else{
            System.out.println("Licitacoes: ");
            for( LinkedHashMap<String, String> data : this.licitacoes){
                for (String key : data.keySet()) {
                    String value = data.get(key);
                    System.out.print(key + " : " + value+", ");
                }
                System.out.print("\n");
            }
        }

        if(mensagens.size() == 0)
            System.out.println("Nao existem mensagens no mural.");
        else{
            System.out.println("Mensagens: ");
            for( LinkedHashMap<String, String> data : this.mensagens){
                for (String key : data.keySet()) {
                    String value = data.get(key);
                    System.out.print(key + " : " + value+", ");
                }
                System.out.print("\n");
            }

        }
    }


    public String getUsername_criador() {
        return username_criador;
    }

    public String getArtigoId() {
        return artigoId;
    }

    public long getId_leilao() {
        return id_leilao;
    }

    public ArrayList<String> getTitulo() {
        return titulo;
    }

    public ArrayList<String> getDescricao() {
        return descricao;
    }

    public double getPrecoMax() {
        return precoMax;
    }

    public int getState() {
        return state;
    }

    public Date getData_termino() {
        return data_termino;
    }

    public ArrayList<LinkedHashMap<String, String>> getMensagens() {
        return mensagens;
    }

    public ArrayList<LinkedHashMap<String, String>> getLicitacoes() {
        return licitacoes;
    }

    public void setUsername_criador(String username_criador) {
        this.username_criador = username_criador;
    }

    public void setArtigoId(String artigoId) {
        this.artigoId = artigoId;
    }

    public void setId_leilao(long id_leilao) {
        this.id_leilao = id_leilao;
    }

    public void setTitulo(ArrayList<String> titulo) {
        this.titulo = titulo;
    }

    public void setDescricao(ArrayList<String> descricao) {
        this.descricao = descricao;
    }

    public void setPrecoMax(double precoMax) {
        this.precoMax = precoMax;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setData_termino(Date data_termino) {
        this.data_termino = data_termino;
    }

    public void setMensagens(ArrayList<LinkedHashMap<String, String>> mensagens) {
        this.mensagens = mensagens;
    }

    public void setLicitacoes(ArrayList<LinkedHashMap<String, String>> licitacoes) {
        this.licitacoes = licitacoes;
    }

    public boolean lastWeek(){
        if (((new Date()).getTime()-data.data_inicio.getTime())>(604800)){//seconds in a week
            return false;
        }
        return true;
    }
}

