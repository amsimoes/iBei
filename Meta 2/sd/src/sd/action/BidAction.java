package sd.action;

import com.opensymphony.xwork2.ActionSupport;

import iBei.aux.Leilao;

import org.apache.struts2.interceptor.SessionAware;

import java.rmi.RemoteException;
import java.util.Map;
import sd.model.Bean;

public class BidAction extends ActionSupport implements SessionAware {
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	public String Id;
	public String amount;
	public Leilao leilao =null;
	public String message;
	@Override
	public String execute() {
		leilao = this.getBean().bidAuction(Id, amount);
		if(leilao == null){
			message = "Erro making bid, check if the auction is not canceled or terminated and if your bid is the lowest";
			return "failure";
		}
		message = "Bid created";
		return SUCCESS;
	
	}
	
	public Bean getBean() {
		if(!session.containsKey("RMIBean"))
			this.setBean(new Bean());
		return (Bean) session.get("RMIBean");
	}

	public void setBean(Bean Bean) {
		this.session.put("RMIBean", Bean);
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}
