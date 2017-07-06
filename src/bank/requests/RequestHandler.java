package bank.requests;

import java.io.IOException;

public interface RequestHandler {
	Request handle(Request req) throws IOException;
}
