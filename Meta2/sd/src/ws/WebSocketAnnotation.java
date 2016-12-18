package ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
        //sendOnlineUsers();
        for(WebSocketAnnotation w:users)
        	System.out.println("for: "+w.username);
        System.out.println("tamanho: "+users.size());
    }

    @OnClose
    public void end() {
    	// clean up once the WebSocket connection is closed
    	System.out.println(this.username+" removed");
    	users.remove(this);
    	sendOnlineUsers();
    }

    @OnMessage
    public void receiveMessage(String message) {
		// one should never trust the client, and sensitive HTML
        // characters should be replaced with &lt; &gt; &quot; &amp;
    	if(message.equals("detail"))
    		sendDetail(message);
    	else if(message.equals("list"))
    		sendOnlineUsers();
    	else
    		sendMessage(message);
    }
    
    @OnError
    public void handleError(Throwable t) {
    	t.printStackTrace();
    }
    
    public void sendOnlineUsers(){
    	String msg = "[USERS ONLINE]"+"<br />";
    	for(WebSocketAnnotation websocket : users ){
    		msg += websocket.username+"<br />";
    	}
    	System.out.println("USERS TAMANHO: "+users.size());
    	for(WebSocketAnnotation ws : users ){
    		try {
				ws.session.getBasicRemote().sendText(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

    public void sendDetail(String message){
    	Map<Integer,Integer> counter  = new HashMap<>();
    	
	        for(WebSocketAnnotation websocket : users ){
	            int id = (Integer) websocket.httpSession.getAttribute("detail_id");
	            if( id != 0){
	            	if(counter.containsKey(id)){
	                    int count = counter.get(id) + 1;
	                    counter.put(id, count);
	                }else{
	                    counter.put(id, 1);
	                }
	            
	            }
	        }
	        
	        
	        for (WebSocketAnnotation websocket : users){
	            int id = (Integer) websocket.httpSession.getAttribute("detail_id");
	            System.out.println("aquiaqui"+id);
	            if(id != 0){
	            	int count = counter.get(id);
	            	System.out.println(count + " aqui "+id);
	            	try {
	                	System.out.println("DETAIL "+websocket.username);
	                    websocket.session.getBasicRemote().sendText("[COUNTER] " + String.valueOf(count));
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
        
    
        
        
        
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
    		else if(text.equals("message"))
    			result = this.bean.notificationsMsg();
    		
    		if(result == null)
    			return;
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