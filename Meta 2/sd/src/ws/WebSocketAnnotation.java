package ws;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.server.ServerEndpoint;

import sd.model.Bean;

import javax.websocket.OnOpen;
import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnError;
import javax.websocket.Session;

@ServerEndpoint(value = "/ws", configurator = HandShake.class)
public class WebSocketAnnotation {
    private static final AtomicInteger sequence = new AtomicInteger(1);
    private String username;
    private Session session;
    private Bean bean;
    private HttpSession httpSession;
    private static final Set<WebSocketAnnotation> users = new CopyOnWriteArraySet<>();

    public WebSocketAnnotation() {
    	
        
    }

    @OnOpen
    public void start(Session session, EndpointConfig config) {
    	httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
    	bean = (Bean)httpSession.getAttribute("RMIBean");
    	
    	this.username = bean.username;
    	//System.out.print(config.getUserProperties().get(HttpSession.class.getName()));
        this.session = session;
        String message = "*" + username + "* connected.";
        users.add(this);
        System.out.println(this.username+" added");
        for(WebSocketAnnotation w:users)
        	System.out.println("for: "+w.username);
        System.out.println("tamanho: "+users.size());
    }

    @OnClose
    public void end() {
    	// clean up once the WebSocket connection is closed
    	System.out.println(this.username+" removed");
    	users.remove(this);
    }

    @OnMessage
    public void receiveMessage(String message) {
		// one should never trust the client, and sensitive HTML
        // characters should be replaced with &lt; &gt; &quot; &amp;
    	String upperCaseMessage = message.toUpperCase();
    	sendMessage(message);
    }
    
    @OnError
    public void handleError(Throwable t) {
    	t.printStackTrace();
    }
    
    private void sendMessage(String text) {
    	// uses *this* object's session to call sendText()
    	try {
    		for(WebSocketAnnotation websocket : users )
    			websocket.session.getBasicRemote().sendText(text);
		} catch (IOException e) {
			// clean up once the WebSocket connection is closed
			try {
				this.session.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
    }
}