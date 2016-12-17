package sd.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.rmi.RemoteException;
import java.util.Map;
import sd.model.Bean;

public class Autentication extends ActionSupport implements SessionAware {
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	public String username;
	public String password;
	public String print="";
	
	
	public String register() {
		// any username is accepted without confirmation (should check using RMI)
		Boolean result = false;
		try {
			if(!username.equals("") && !password.equals(""))
				result = this.getBean().register(username, password);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		username="";
		password="";
		if(!result){
			print="Username already exists";
			return "failure";
		}
		else{
			print="Registered successfully";
		}
			
		return SUCCESS;
		
	}
	
	
	public String login() {
		// any username is accepted without confirmation (should check using RMI)
		Boolean result = false;
		try {
			result = this.getBean().login(username, password);
			System.out.println("Bean username: "+this.getBean().getUsername());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!result){
			username="";
			password="";
			print="Username or Password wrong";
			return "failure";
		}
		if(username.equals("admin"))
			return "adminUser";
		this.session.put("detail_id", 0);
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