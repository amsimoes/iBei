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
        	 
                console.log("notificaoca");
                NotfMessage();
                NotfBid();
                increaseCounter();
        
        
            
        }
        
        function onClose(event) {
            console.log("Socket closed");
        }
        
        function onMessage(message) { // print the received message
        	 if (message.data.startsWith("[COUNTER]")){
        		 var info = message.data.split(" ");
        		 var counter = document.getElementById('count');
        		 counter.innerHTML = info[1];
        	 }
        	else{
        		alert(message.data);
        	}
        }
        
        function onError(event) {
            console.log("ERROR");
        }
        
        function increaseCounter(){
        	doSend("detail");
        }
        

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

  

