package sd.action;

import com.opensymphony.xwork2.ActionSupport;

import iBei.aux.Leilao;
import iBei.aux.User;

import org.apache.struts2.interceptor.SessionAware;

import java.rmi.RemoteException;
import java.util.Map;
import sd.model.Bean;

public class FacebookRemove extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    public String username;
    public String message;
    boolean result = false;

    @Override
    public String execute() {
        // any username is accepted without confirmation (should check using RMI)
        try {
            //System.out.println(this.getBean().getUsername());
            result = this.getBean().removeFacebook(username);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if(!result){
            System.out.println("[Fb Remove] Error removing fb account");
            message = username+" has no facebook account associated.";
            return "failure";
        }
        message = "Facebook account deassociated with success!";
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
