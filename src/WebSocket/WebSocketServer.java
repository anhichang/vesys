package WebSocket;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.glassfish.tyrus.server.Server;

import bank.Bank;
import bank.BankDriver2;
import bank.BankDriver2.UpdateHandler;
import bank.local.Driver;
import bank.requests.Request;


@ServerEndpoint(value = "/bank",
		decoders = RequestDecoder.class,
		encoders = RequestEncoder.class)
public class WebSocketServer {

	private static ServerBank bank;
	private static List<Session> sessions = new CopyOnWriteArrayList<>();
	
	public static void main(String[] args) throws Exception {
						
		Server server = new Server("localhost", 2222, "/websockets",null, WebSocketServer.class);
		server.start();
		System.out.println("Server started...");
		// so ist es möglich, eine interface zu instanzieren
		BankDriver2.UpdateHandler handler = new BankDriver2.UpdateHandler() {
			@Override
			public void accountChanged(String id) {
				for(Session s: sessions){
					try {s.getBasicRemote().sendText(id);}
					catch(Exception e){
						System.out.println(e);
						sessions.remove(s);
					}
				}
			}
		};
		Driver driver = new Driver();
		driver.connect(args);
		
		bank = new ServerBank(driver.getBank(), handler);
		
		System.out.println("Server started, key to stop the server"); 
		System.in.read();
	}
	
	@OnOpen
	public void onOpen( Session session ) {
		sessions.add(session);
	}

	@OnClose
	public void onClose( Session session, CloseReason closeReason ) {
		sessions.remove(session);
	}
	//hier empfangen
	@OnMessage
	public Request onMessage(Request r, Session session) {
		r.handleRequest(bank);
		return r;	
	}

	@OnError
	public void onError( Throwable exception, Session session) {
		System.out.println( "an error occured on connection " + session.getId() + ":" + exception );
	}

}
