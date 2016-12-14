package sd.action;

import com.opensymphony.xwork2.ActionSupport;

import iBei.aux.Leilao;

import org.apache.struts2.interceptor.SessionAware;

import java.rmi.RemoteException;
import java.util.Map;
import sd.model.Bean;

public class DetailAction extends ActionSupport implements SessionAware {
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	public String Id;
	public Leilao leilao;
	public String message;
	@Override
	public String execute() {
		// any username is accepted without confirmation (should check using RMI)
		try {
			System.out.println(this.getBean().getUsername());
			leilao = this.getBean().detailAuction(Id);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(leilao == null){
			System.out.println("null returned");
			message = "Auction not found";
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
