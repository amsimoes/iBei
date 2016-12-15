package iBei.aux;

import java.util.Random;
import java.util.Scanner;
import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.json.simple.JSONObject;

import java.io.IOException;

public final class FacebookRest {
    private static final String NETWORK_NAME = "Facebook";
    private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/v2.8/";
    private final String apiId = "1883105131924636";
    private final String apiSecret = "bfd4694e120a2e380b0135fda678c846";
    private final String callBack = "http://eden.dei.uc.pt/~amaf/echo.php";
    final String secretState = "secret" + new Random().nextInt(999_999);

    private final String username;
    private OAuth2AccessToken accessToken;
    private final OAuth20Service service;

    public FacebookRest(String username, String accessToken) throws IOException {
        this.username = username;
        this.service = new ServiceBuilder()
                .apiKey(this.apiId)
                .apiSecret(this.apiSecret)
                .state(this.secretState)
                .callback(this.callBack)
                .build(FacebookApi.instance());
        this.accessToken = this.checkToken(accessToken);
        System.out.println("[FbRest] ACCESS TOKEN ="+this.accessToken);
    }

    private OAuth2AccessToken checkToken(String token) throws IOException {
        OAuth2AccessToken aT;
        if(token.equals("")) {
            Scanner in = new Scanner(System.in, "UTF-8");

            System.out.println("Fetching the Authorization URL...");
            final String authorizationUrl = service.getAuthorizationUrl();
            System.out.println("Authorize Scribejava here:");
            System.out.println(authorizationUrl);
            System.out.println("And paste the authorization code here");
            System.out.print(">>");
            final String code = in.nextLine();
            System.out.println("And paste the state from server here. We have set 'secretState'='" + secretState + "'.");
            System.out.print(">>");
            final String value = in.nextLine();
            if (secretState.equals(value)) {
                System.out.println("State value does match!");
            } else {
                System.out.println("Ooops, state value does not match!");
                System.out.println("Expected = " + secretState);
                System.out.println("Got      = " + value);
                System.out.println();
            }
            System.out.println("Trading the Request Token for an Access Token...");
            aT = this.service.getAccessToken(code);
            System.out.println("Got the Access Token!");

        } else {
            System.out.println("Token presente. Conta ja associada!");
            aT = new OAuth2AccessToken(token);
        }
        return aT;
    }

    public String getAccessToken() {
        return this.accessToken.getAccessToken();
    }

    public boolean postAuction(String mensagem) {
        String rq = PROTECTED_RESOURCE_URL+"me/feed?message="+mensagem+"&access_token="+this.accessToken.getAccessToken();
        System.out.println(rq);
        final OAuthRequest request = new OAuthRequest(Verb.POST, rq, this.service.getConfig());
        request.addHeader("Content-Type", "application/json; charset=UTF-8");
        JSONObject privacy = new JSONObject();
        privacy.put("value", "EVERYONE");
        request.addParameter("privacy", privacy.toString());
        System.out.println(this.accessToken);
        System.out.println("Request: "+request);
        this.service.signRequest(this.accessToken, request);
        Response response = request.send();
        System.out.println(response);
        if(response.getCode() != 200) {
            return false;
        }
        return true;
    }
}


