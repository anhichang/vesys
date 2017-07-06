package bank.jms.jmscorrect;

import java.io.IOException;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;

import bank.requests.Request;
import bank.requests.RequestHandler;

public class JmsHandler implements RequestHandler {

	private final TemporaryQueue responseQueue;
	private final JMSProducer producer;
	private final JMSConsumer consumer;
	private final ObjectMessage request;
	private final Queue queue;

	public JmsHandler(JMSContext context, Queue queue) throws JMSException {
		this.responseQueue = context.createTemporaryQueue();
		this.producer = context.createProducer().setDeliveryMode(Server.deliverMode);
		this.consumer = context.createConsumer(responseQueue);
		this.request = context.createObjectMessage();
		this.queue = queue;

	}

	@Override
	public Request handle(Request req) throws IOException {
		try {
			request.setObject(req);
			request.setJMSReplyTo(responseQueue);
			producer.send(queue, request);
			return consumer.receiveBody(Request.class);
		} catch (JMSException e) {
			throw new IOException(e);
		}
	}

}
