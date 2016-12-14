package sd.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;
import sd.model.Bean;

public class CreateAuction extends ActionSupport implements SessionAware {
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	public String title;
	public String description;
	public String deadline;
	public String code;
	public String amount;
	public String message;
	@Override
	public String execute() {
		boolean result = this.getBean().createAuction(title,description,deadline,code,amount);
		if(!result){
			message = "Error creating auction";
			return "failure";
		}
		message = "Auction created successfully";
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
