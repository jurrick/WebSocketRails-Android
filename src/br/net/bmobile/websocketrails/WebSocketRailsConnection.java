package br.net.bmobile.websocketrails;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.net.Uri;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpClient.WebSocketConnectCallback;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.WebSocket.StringCallback;

public class WebSocketRailsConnection implements StringCallback, CompletedCallback {

	private URL url;
	private WebSocketRailsDispatcher dispatcher;
	private List<WebSocketRailsEvent> message_queue;
	private WebSocket webSocket;
	
	public WebSocketRailsConnection(URL url, WebSocketRailsDispatcher dispatcher) {
	
        this.url = url;
        this.dispatcher = dispatcher;
        this.message_queue = new ArrayList<WebSocketRailsEvent>();
        
        Uri uri = null;
        try {
			uri = Uri.parse(url.toURI().toString());
			
	        AsyncHttpClient.getDefaultInstance().websocket(
        		new AsyncHttpGet(uri), null, new WebSocketConnectCallback() {
					
					@Override
					public void onCompleted(Exception arg0, WebSocket webSocket) {
						
				        webSocket.setStringCallback(WebSocketRailsConnection.this);
				        webSocket.setClosedCallback(WebSocketRailsConnection.this);

				        WebSocketRailsConnection.this.webSocket = webSocket;
					}
				});
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void trigger(WebSocketRailsEvent event) {
		
	    if (dispatcher.getState() == "connected")
	        message_queue.add(event);
	    else
	        webSocket.send(event.serialize());		
	}
	
	public void flushQueue(String id) {
		
	    for (WebSocketRailsEvent event : message_queue)
	    {
	        String serializedEvent = event.serialize();
	        webSocket.send(serializedEvent);
	    }		
	}

	public void disconnect() {
	
		webSocket.close();
	}

	@Override
	public void onCompleted(Exception arg0) {
	    List<Object> data = new ArrayList<Object>();
		data.add("connection_closed");
		data.add(new HashMap<String, Object>());

		WebSocketRailsEvent closeEvent = new WebSocketRailsEvent(data);
	    dispatcher.setState("disconnected");
	    dispatcher.dispatch(closeEvent);
	}

	@Override
	public void onStringAvailable(String data) {
	
		ObjectMapper mapper = new ObjectMapper();
		 
		// read JSON from a file
		List<Object> list;
		try {
			list = mapper.readValue(
				data,
				new TypeReference<List<Object>>() {
			});
			
			dispatcher.newMessage(list);
			
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}