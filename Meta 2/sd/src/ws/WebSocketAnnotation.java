package ws;

import java.io.IOException;
import java.util.ArrayList;
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
    public Bean bean;
    private HttpSession httpSession;
    public String location;
    private static final Set<WebSocketAnnotation> users = new CopyOnWriteArraySet<>();

    public WebSocketAnnotation() {
    	
        
    }

    @OnOpen
    public void start(Session session, EndpointConfig config) {
    	httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
    	this.bean = (Bean)httpSession.getAttribute("RMIBean");
    	
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
    	
    	sendMessage(message);
    }
    
    @OnError
    public void handleError(Throwable t) {
    	t.printStackTrace();
    }
    
    public void updateLocation(){
    	this.location = ""; 
    }
    
    private void sendMessage(String text) {
    	// uses *this* object's session to call sendText()
    	//RMI e aqui
    	System.out.println("AQUI");
    	try {
    		/*for(WebSocketAnnotation websocket : users ){
					websocket.session.getBasicRemote().sendText(text);
    			}*/
    		ArrayList<ArrayList<Object>> result = null;
    		String message = "";
    		if(text.equals("bid"))
    			result = this.bean.notificationsBid();
    		else
    			result = this.bean.notificationsMsg();
    		
    		
    		for(ArrayList <Object> data : result){
    			for(Object d: data)
    				System.out.println("element: "+d);
    			System.out.println("");
    			
    			if(data.size() == 0)
        			return;
    		
	    		int id = (int)data.get(0);
	    		
	   
	    		String textMsg = (String) data.get(1);
	    		String userSender = (String) data.get(2);
	    		if(text.equals("bid"))
	    			message = "Bid notification: amount: "+textMsg+" user: "+userSender+" id: "+id;
	    		else
	    			message = "Message notification: text: "+textMsg+" user: "+userSender+" id: "+id;
	    		for(WebSocketAnnotation websocket : users ){
	    			System.out.println("for_socket: "+websocket.bean.getUsername());
	    			System.out.println(websocket.bean.getUsername() + " "+this.bean.getUsername());
	    			//if(!websocket.bean.getUsername().equals(this.bean.getUsername())){
	    				if(data.contains(websocket.bean.getUsername()) && !data.get(2).equals(websocket.bean.getUsername()))
	    					websocket.session.getBasicRemote().sendText(message);
	    				System.out.println("message to: "+websocket.bean.getUsername());
	    			//}
	    				
	    		}
    		}
		} catch (IOException e) {
			// clean up once the WebSocket connection is closed
			e.printStackTrace();
			try {
				this.session.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
    }
}