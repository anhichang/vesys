package bank.jms.jmscorrect;

import java.io.IOException;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import bank.Bank;
import bank.local.Driver;
import bank.BankDriver2;
import bank.BankDriver2.UpdateHandler;
import bank.requests.Request;

public class Server {

	static final int deliverMode = DeliveryMode.NON_PERSISTENT;
	
	public static void main(String [] args) throws Exception {
		final Context jndiContext = new InitialContext();
		final ConnectionFactory factory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
		final Queue queue = (Queue) jndiContext.lookup("/queue/JMSBANK");
		final Topic topic = (Topic) jndiContext.lookup("/topic/BANK");
		
		try (JMSContext context = factory.createContext()){
			JMSConsumer queueConsumer = context.createConsumer(queue);
			final JMSProducer queueProducer = context.createProducer().setDeliveryMode(deliverMode);
			
			final JMSProducer topicProducer = context.createProducer().setDeliveryMode(deliverMode);
		
			UpdateHandler handler = new UpdateHandler() {
				@Override
				public void accountChanged(String id) throws IOException {
					topicProducer.send(topic, id);
				}
			};	
			
		Bank bank = new BankImpl(new LocalBank(), handler);
		
		System.out.println("JMS server is running...");
		
		while (true){
			Message msg = queueConsumer.receive();
			Request req = msg.getBody(Request.class);
			req.handleRequest(bank);
			queueProducer.send(msg.getJMSReplyTo(), req);
		}
		
		}
		
	}


	

	
	
}
