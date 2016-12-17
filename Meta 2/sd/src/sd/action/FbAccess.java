package sd.action;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Random;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;


public class facebook extends ActionSupport implements SessionAware {
	public final String apiId = "1883105131924636";
	public final String apiSecret = "bfd4694e120a2e380b0135fda678c846";
	public final String secretState = "secret" + new Random().nextInt(999_999);
	public final String callBack = "http://localhost:8080/";
	private Map<String, Object> session;
	
	public String execute() {
		System.out.println("teste");
		OAuth20Service service = new ServiceBuilder()
				.apiKey(apiId)
				.apiSecret(apiSecret)
                .state(this.secretState)
                .callback(this.callBack)
                .build(FacebookApi.instance());
		session.put("service",service);
		return SUCCESS;
	}

	
	@Override
	public void setSession(Map<String, Object> arg0) {
		// TODO Auto-generated method stub
		this.session = arg0;
		
	}
	
}