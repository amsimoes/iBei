package sd.action;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.opensymphony.xwork2.ActionSupport;

import org.apache.struts2.interceptor.SessionAware;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;

import org.json.simple.JSONObject;
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
    private int leilaoId;

	@Override
	public String execute() {
		this.leilaoId = this.getBean().createAuction(title,description,deadline,code,amount);
		if(this.leilaoId == 0){
			message = "Error creating auction";
			return "failure";
		}
		try {
			if((Boolean) session.get("facebook")) {
				String link = "http://localhost:8080/detailAuction?Id="+this.leilaoId;
				OAuth20Service service = (OAuth20Service) session.get("service");
				if(!postAuction(link, service)) {
					System.out.println("Error posting auction link to facebook.");
					message = "Auction created. Error posting auction link to facebook.";
					return "failure";
				}
			}
		} catch (NullPointerException e) {
			message = "Auction created. Error posting auction link to facebook.";
			return "failure";
		}
		message = "Auction created successfully";
		return SUCCESS;
	}

	public boolean postAuction(String link, OAuth20Service service) {
		OAuth2AccessToken accessToken = (OAuth2AccessToken) session.get("access_token");
		System.out.println("ACCESS TOKEN="+accessToken.getAccessToken());
		String rq = "https://graph.facebook.com/v2.8/me/feed?message="+link+"&access_token"+accessToken.getAccessToken();
		OAuthRequest request = new OAuthRequest(Verb.POST, rq, service.getConfig());
		request.addHeader("Content-Type", "application/json; charset=UTF-8");
        JSONObject privacy = new JSONObject();
        privacy.put("value", "SELF");
        request.addParameter("privacy", privacy.toString());
		service.signRequest(accessToken, request);
		Response response = request.send();
		try {
			System.out.println(response.getBody());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response.getCode()==200;
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

