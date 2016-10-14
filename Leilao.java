import java.io.*;
import java.net.*;
import java.util.*;
public class Leilao{
	
	public long artigoId;
	public long id_leilao;
	public String titulo;
	public String descricao;
	public double precoMax;
	public Date data_termino;
	public ArrayList <String >mensagens;
	public ArrayList<HashMap <String, Double> > licitacoes;//nao sei se e a melhor solucao 


	public Leilao(long artigoId, String titulo, String descricao, double precoMax, Date data_termino){
		this.artigoId = artigoId;
		this.titulo = titulo;
		this.descricao = descricao;
		this.precoMax = precoMax;
		this.data_termino = data_termino;
		this.mensagens = new ArrayList<String>();
		this.licitacoes = new ArrayList<HashMap <String, Double>>();
		this.id_leilao = 356563;//tem de ser um numero aleatorio

	}

	public void printInfo(){
		System.out.println("\nDescricao: "+this.descricao);
		System.out.println("Data do termino: "+this.data_termino.toString());
		

		if(licitacoes.size() == 0)
			System.out.println("Nao foram feitas ainda nenhumas licitacoes");
		else{
			System.out.println("Licitacoes: ");
			for( HashMap<String, Double> data : this.licitacoes){
				for (String key : data.keySet()) {
	                    Double value = data.get(key);
	                    System.out.println(key + " : " + value);
	               }
			}
		}

		if(mensagens.size() == 0)
			System.out.println("Nao existem mensagens no mural.");
		else{
			System.out.println("Mensagens: ");
			for(String m : this.mensagens){
				System.out.println(" "+m);
			}

		}
	}


}

