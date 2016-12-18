package sd.action;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.IOException;
import java.util.Map;
import java.util.Random;

import sd.model.Bean;


public class Callback extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    public String code;

    private final String apiId = "1883105131924636";
    private final String apiSecret = "bfd4694e120a2e380b0135fda678c846";
    private final String appToken = "b728b07d2b02fff68650133d4d93c515";
    private final String secretState = "secret" + new Random().nextInt(999_999);
    private final String callBack = "http://localhost:8080/callback.jsp";

    private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/v2.8/";

    @Override
    public String execute() throws IOException {
        Boolean loggado = (Boolean) session.get("loggado");
        System.out.println("LOGGADO DA SESSION="+loggado);
        String username = (String) session.get("username");
        System.out.println("USERNAME NA SESSION="+username);
        OAuth20Service service = new ServiceBuilder()
                .apiKey(apiId)
                .apiSecret(apiSecret)
                .state(this.secretState)
                .callback(this.callBack)
                .build(FacebookApi.instance());
        OAuth2AccessToken access_token = service.getAccessToken(code);
        String user_id = inspectToken(access_token.getAccessToken(), service);
        if(user_id!=null) {
            System.out.println(user_id);
            if(loggado==null) {
                System.out.println("A FAZER LOGIN...");
                if(this.getBean().loginFacebook(user_id)) {
                    session.put("service", service);
                    session.put("access_token", access_token);
                    session.put("facebook", true);
                    System.out.println("SUCESSO A FAZER LOGIN FACEBOOK");
                    return SUCCESS;
                }
            } else {
                System.out.println("A ASSOCIAR...");
                if(this.getBean().associate(username, user_id)) {
                    session.put("service", service);
                    session.put("access_token", access_token);
                    session.put("facebook", true);
                    System.out.println("SUCESSO A ASSOCIAR CONTA");
                    return SUCCESS;
                } else {
                    return "already_associated";
                }
            }
        }
        System.out.println("FAIL");
        return "failure";
    }

    public String generateAppAccessToken(OAuth20Service service) throws IOException {
        String req = PROTECTED_RESOURCE_URL+"oauth/access_token?client_id="+this.apiId+
                "&client_secret="+apiSecret+"&grant_type=client_credentials";
        OAuthRequest request = new OAuthRequest(Verb.GET, req, service.getConfig());
        Response response = request.send();
        System.out.println(response);
        JSONParser parser = new JSONParser();
        try {
            JSONObject j = (JSONObject) parser.parse(response.getBody());
            if(j.containsKey("access_token"))
                return(j.get("access_token").toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(response.getHeader("access_token"));
        return null;
    }

    public String inspectToken(String input_token, OAuth20Service service) throws IOException {
        String appToken = generateAppAccessToken(service);
        String req = PROTECTED_RESOURCE_URL+"debug_token?input_token="+input_token+
                    "&access_token="+appToken;
        OAuthRequest request = new OAuthRequest(Verb.GET, req, service.getConfig());
        Response response = request.send();
        if(response.getCode()==200) {
            JSONParser parser = new JSONParser();
            try {
                JSONObject j = (JSONObject) parser.parse(response.getBody());
                JSONObject jData = (JSONObject) j.get("data");
                if(jData.containsKey("user_id")) {
                    return(jData.get("user_id").toString());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void setSession(Map<String, Object> arg0) {
        // TODO Auto-generated method stub
        this.session = arg0;
    }

    public String getCode() {
        return code;
    }

    public Bean getBean() {
        if(!session.containsKey("RMIBean"))
            this.setBean(new Bean());
        return (Bean) session.get("RMIBean");
    }

    public void setBean(Bean bean) {
        System.out.println("[Callback] Novo bean");
        this.session.put("RMIBean", bean);
    }
}
