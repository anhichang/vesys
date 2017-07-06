package WebSocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Exchanger;

import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.OnMessage;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;

import bank.Bank;
import bank.BankDriver2;
import bank.requests.Request;
import bank.requests.RequestHandler;

@ClientEndpoint(decoders = RequestDecoder.class, encoders = RequestEncoder.class)
public class Client implements BankDriver2 {
	private ClientBank bank;
	private final Exchanger<Request> ex = new Exchanger<>();
	private Session session;
	private List<UpdateHandler> listeners = new LinkedList<>();

	@OnMessage
	public void onMessage(Request msg) {
		exchange(msg);
	}

	@OnMessage
	public void onMessage(String id) {
		for (UpdateHandler h : listeners) {
			try {
				h.accountChanged(id);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void connect(String[] args) throws IOException {
		URI uri;
		try {
			uri = new URI("ws://localhost:2222/websockets/bank");
			System.out.println("connecting to " + uri);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		ClientManager client = ClientManager.createClient();
		try {
			session = client.connectToServer(this, uri);
		} catch (DeploymentException e) {
			throw new IOException(e);
		}
		bank = new ClientBank(new WebSocketsHandler());
	}

	private class WebSocketsHandler implements RequestHandler {
		@Override
		public Request handle(Request req) throws IOException {
			try {
				// hier wird die nachricht richtig gesendet
				session.getBasicRemote().sendObject(req);
			} catch (EncodeException e) {
				throw new IOException(e);
			}
			return (Request) exchange(null);
		}
	}

	public void disconnect() throws IOException {
		bank = null;
		session.close();
		System.out.println("disconnected...");
	}

	public Bank getBank() {
		return bank;
	}

	private Request exchange(Request x) {
		try {
			return ex.exchange(x);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void registerUpdateHandler(UpdateHandler handler) {
		listeners.add(handler);
	}

}
