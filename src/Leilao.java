import java.io.*;
import java.net.*;
import java.util.*;
public class Leilao implements Serializable{
    public String username_criador;
    public long artigoId;
    public long id_leilao;
    public ArrayList <String> titulo;
    public ArrayList <String> descricao;
    public double precoMax;
    public int state;//0 se estiver ativo, 1 se tiver cancelado, 2 se tiver terminado
    public Date data_termino;
    public ArrayList<LinkedHashMap <String, String >> mensagens;//key: author, message
    public ArrayList<LinkedHashMap <String, String >> licitacoes;//key: author, bid


    public Leilao(String username, long artigoId, String titulo, String descricao, double precoMax, Date data_termino){
        this.username_criador = username;
        this.artigoId = artigoId;
        this.titulo = new ArrayList<String>();
        this.titulo.add(titulo);
        this.descricao = new ArrayList<String>();
        this.descricao.add(descricao);
        this.precoMax = precoMax;
        this.data_termino = data_termino;
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


}

