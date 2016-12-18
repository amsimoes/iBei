package sd.action;

import com.opensymphony.xwork2.ActionSupport;

import iBei.aux.Leilao;

import org.apache.struts2.interceptor.SessionAware;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import sd.model.Bean;

public class MyAction extends ActionSupport implements SessionAware {
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	public ArrayList <Leilao> leiloes = new ArrayList<Leilao>();
	public String message;
	
	@Override
	public String execute() {
		try {
			System.out.println(this.getBean().getUsername());
			this.leiloes = this.getBean().myAuctions();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(leiloes.size() == 0){
			message = "You havent participate in any auction";
			return "failure";
		}
		return SUCCESS;
		
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