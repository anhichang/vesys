package WebSocket;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import bank.requests.Request;

public class RequestEncoder implements Encoder.Binary<Request> {
	
	@Override
	public void destroy() {}

	@Override
	public void init(EndpointConfig arg0) {}

	@Override
	public ByteBuffer encode(Request obj) throws EncodeException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			oos.flush(); oos.close();
			return ByteBuffer.wrap(baos.toByteArray());
		} catch (IOException e){
			throw new EncodeException(obj, e.getMessage(), e);
		}
	}

}
