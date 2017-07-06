package bank.jms.jmscorrect;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import WebSocket.ClientBank;
import bank.Bank;


public class Client implements bank.BankDriver2{

	private Bank bank;
	private JMSContext requestContext;
	private JMSContext listenerContext;

	public void connect(String[] args) {
		try {
			Context jndiContext = new InitialContext();
			ConnectionFactory factory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
			requestContext = factory.createContext();
			Queue queue = (Queue) jndiContext.lookup("/queue/BANK");
			// ClientBank kommt aus dem WebSockets package und RequestHandler im Konstruktor entgegennehmen
			bank = new ClientBank(new JmsHandler(requestContext, queue));
			listenerContext = factory.createContext();
			Topic topic = (Topic) jndiContext.lookup("/topic/BANK");

			JMSConsumer consumer = listenerContext.createConsumer(topic);
			consumer.setMessageListener(msg -> {
				try {
					String id = msg.getBody(String.class);
					for (UpdateHandler h : listeners) {
						try {
							h.accountChanged(id);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} catch (JMSException e1) {
					e1.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void disconnect() {
		requestContext.close();
		listenerContext.close();
		bank = null;
		System.out.println("disconnected...");
	}

	public Bank getBank() {
		return bank;
	}

	private final List<UpdateHandler> listeners = new CopyOnWriteArrayList<>();

	@Override
	public void registerUpdateHandler(UpdateHandler handler) {
	listeners.add(handler);
	}
		

}