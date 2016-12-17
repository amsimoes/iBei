var websocket = null;

        window.onload = function() { // URI = ws://10.16.0.165:8080/WebSocket/ws
            connect('ws://' + window.location.host + '/sd/ws');
           console.log("conectado");
            //document.getElementById("chat").focus();
        }

        function connect(host) { // connect to the host websocket
            if ('WebSocket' in window)
                websocket = new WebSocket(host);
            else if ('MozWebSocket' in window)
                websocket = new MozWebSocket(host);
            else {
                writeToHistory('Get a real browser which supports WebSocket.');
                return;
            }

            websocket.onopen    = onOpen; // set the event listeners below
            websocket.onclose   = onClose;
            websocket.onmessage = onMessage;
            websocket.onerror   = onError;
            
            
        }

        function onOpen(event) {
        	//document.getElementById('chat').onkeydown = function(key) {
              //  if (key.keyCode == 13){
                    //window.location.href = "http://localhost:8080/sd/autentication.jsp";
                    //websocket.send("ligar ao websocketannotation");
                    if(window.location.href == "http://" + window.location.host + "/sd/detailAuction.jsp?"){
                		console.log("detail action");
                		var x = document.getElementById("count").innerText;
                    	document.getElementById("count").innerHTML = parseInt(x)+1;
                	}
                    else{
                    	console.log("error");
                    }   
                    console.log("notificaoca");
                    NotfMessage();
                    NotfBid();
                    
                //}
            //};
        	
            console.log("http://" + window.location.host + "/sd/autentication.jsp");
            
        }
        
        function onClose(event) {
            console.log("Socket closed");
        }
        
        function onMessage(message) { // print the received message
            alert(message.data);
        }
        
        function onError(event) {
            console.log("ERROR");
        }
        
        function waitForSocketConnection(){
            setTimeout(
                function(){
                	if (websocket.readyState === 1) {
                        if(callback !== undefined){
                            callback();
                        }
                        return;
                    } else {
                        waitForSocketConnection(socket,callback);
                    }
                }, 5);
        };
        
        function NotfMessage(){
        	console.log("ffff");
        	doSend("message");
        }
        
        function NotfBid(){
        	console.log("ffff");
        	doSend("bid");
        }
        
        function IncreaseCount(){
        	var x = document.getElementById("count").innerText;
        	console.log(x);
        	document.getElementById("count").innerHTML = parseInt(x)+1;
        	x.appendChild(x);
            x.scrollTop = history.scrollHeight;
        	
        	
        }
        
        function doSend(message) {
            websocket.send(message);
        }

  
