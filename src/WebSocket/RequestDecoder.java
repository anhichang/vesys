package WebSocket;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import bank.requests.Request;

public class RequestDecoder implements Decoder.Binary<Request> {

	@Override
	public void destroy() {
	}

	@Override
	public void init(EndpointConfig bytes) {
	}

	@Override
	public Request decode(ByteBuffer bytes) throws DecodeException {
		byte[] buf;
		if (bytes.hasArray()) {
			buf = bytes.array();
		} else {
			buf = new byte[bytes.capacity()];
			bytes.get(buf);
		}	

		try {
			return (Request) new ObjectInputStream(new ByteArrayInputStream(buf)).readObject();
		} catch (ClassNotFoundException | IOException e) {
			throw new DecodeException(bytes, e.getMessage(), e);
		}
	}

	@Override
	public boolean willDecode(ByteBuffer bytes) {
		return true;
	}

}
