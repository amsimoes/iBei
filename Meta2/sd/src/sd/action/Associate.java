package sd.action;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;

import com.github.scribejava.core.oauth.OAuth20Service;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import sd.model.Bean;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class Associate extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;

    private final static String apiId = "1883105131924636";
    private final static String apiSecret = "bfd4694e120a2e380b0135fda678c846";
    private final String secretState = "secret" + new Random().nextInt(999_999);
    private final String callBack = "http://localhost:8080/associate.jsp";

    public String code;

    public String execute() throws IOException {
        OAuth20Service service = new ServiceBuilder()
                .apiKey(apiId)
                .apiSecret(apiSecret)
                .state(this.secretState)
                .callback(this.callBack)
                .build(FacebookApi.instance());
        OAuth2AccessToken access_token = service.getAccessToken(code);
        String user_id = sd.action.Callback.inspectToken(access_token.getAccessToken(), service);
        String username = (String) this.getBean().getUsername();
        if(user_id != null) {
            if(this.getBean().associate(username, user_id)) {
                session.put("service", service);
                session.put("access_token", access_token);
                System.out.println("SUCESSO A ASSOCIAR CONTA DO USER = "+username);
                return SUCCESS;
            }
        }
        return "failure";
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
