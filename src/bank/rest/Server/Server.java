package bank.rest.Server;

import java.io.IOException;
import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class Server {

	public static void main(String[] args) throws IOException{
		final String baseUri = "http://localhost:9998/";

		// @Singleton annotation is respected,
		// but classes without a @Singleton annotation can also returned as singletons over the application definition
		final ResourceConfig rc = new ResourceConfig().packages("bank.rest.Server");
		
		System.out.println("Starting grizzly...");
		HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), rc);

		System.out.println(String.format("Jersey app started with WADL available at "
								+ "%sapplication.wadl\nTry out %smsg\nHit enter to stop it...",
								baseUri, baseUri));
		// XXX der Kommentar, dass man unter http://localhost:9998/msg Informationen findet stimmt natürlich nicht mehr.
		
		System.in.read();
		httpServer.shutdown();
	}

}
