package sd.action;

import com.opensymphony.xwork2.ActionSupport;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import iBei.aux.Leilao;
import iBei.aux.User;

import org.apache.struts2.interceptor.SessionAware;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Map;
import sd.model.Bean;

public class Stats extends ActionSupport implements SessionAware {
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	public User [] statsLeiloes;
	public User [] statsVitorias;
	public int statsLastWeek = 0;
	@Override
	public String execute() {
		// any username is accepted without confirmation (should check using RMI)
		try {
			
			statsLeiloes = this.getBean().getStatsLeiloes();
			statsVitorias = this.getBean().getStatsVitorias();
			statsLastWeek = this.getBean().getStatsLastWeek();
			
			System.out.println("leiloes: "+statsLeiloes.length);
			System.out.println("vitorias: "+statsVitorias.length);
			System.out.println("count: "+statsLastWeek);
			
			statsLeiloes = this.reverseArray(statsLeiloes);
			statsVitorias = this.reverseArray(statsVitorias);
			
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return SUCCESS;
	
	}
	
	public User [] reverseArray(User [] array){
		for(int i = 0; i < array.length / 2; i++)
		{
		    User temp = array[i];
		    array[i] = array[array.length - i - 1];
		    array[array.length - i - 1] = temp;
		}
		
		return array;
	}
	
	public Bean getBean() {
		if(!session.containsKey("RMIBean"))
			this.setBean(new Bean());
		
		return (Bean) session.get("RMIBean");
	}

	public void setBean(Bean Bean) {
		System.out.println("novo bean");
		this.session.put("RMIBean", Bean);
	}

	@Override
	public void setSession(Map<String, Object> session) {
		System.out.println("acede ao Bean");
		this.session = session;
	}
}
