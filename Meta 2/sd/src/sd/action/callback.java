package sd.action;

import java.rmi.RemoteException;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;

import iBei.aux.Leilao;

public class callback extends ActionSupport implements SessionAware {
	private static final long serialVersionUID = 4L;
	private Map<String, Object> session;
	public String message;
	public String access_token;
	public String code;
	//public String idid;
	@Override
	public String execute() {
		//HttpServletRequest request;
		//String parameter = ServletActionContext.getRequest().getParameter("access_token");
		System.out.println(code);
		
		return SUCCESS;
	
	}
	
	public String getAccess_token(){
		return this.access_token;
	}
	
	@Override
	public void setSession(Map<String, Object> arg0) {
		// TODO Auto-generated method stub
		this.session = arg0;
	}	
}