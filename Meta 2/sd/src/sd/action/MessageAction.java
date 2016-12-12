package sd.action;

import com.opensymphony.xwork2.ActionSupport;

import iBei.aux.Leilao;

import org.apache.struts2.interceptor.SessionAware;

import java.rmi.RemoteException;
import java.util.Map;
import sd.model.Bean;

public class MessageAction extends ActionSupport implements SessionAware {
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	public String Id;
	public String text;
	public Leilao leilao;
	@Override
	public String execute() {
		leilao = this.getBean().msgAuction(Id, text);
		if(leilao == null)
			return "failure";
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
