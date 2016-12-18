package sd.action;

import com.opensymphony.xwork2.ActionSupport;

import iBei.aux.Leilao;

import org.apache.struts2.interceptor.SessionAware;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import sd.model.Bean;

public class SearchAction extends ActionSupport implements SessionAware {
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	public String code;
	public ArrayList <Leilao> leiloes = new ArrayList<Leilao>();
	public String message;
	@Override
	public String execute() {
		// any username is accepted without confirmation (should check using RMI)
		try {
			leiloes = this.getBean().searchAuction(code);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(leiloes.size() == 0){
			message = "There is no auction with that code article";
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
		this.session.put("RMIBean", Bean);
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}
}
