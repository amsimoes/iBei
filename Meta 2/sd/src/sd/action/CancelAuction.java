package sd.action;

import com.opensymphony.xwork2.ActionSupport;

import iBei.aux.Leilao;

import org.apache.struts2.interceptor.SessionAware;

import java.rmi.RemoteException;
import java.util.Map;
import sd.model.Bean;

public class CancelAuction extends ActionSupport implements SessionAware {
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	public int id;
	public String message;
	public boolean result = false;
	@Override
	public String execute() {
		// any username is accepted without confirmation (should check using RMI)
		try {
			result = this.getBean().cancelAuction(id);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!result){
			message = "Error canceling auction";
			return "failure";
		}
		message = "Auction canceled successfully";
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
