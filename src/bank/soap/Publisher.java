package bank.soap.server;

import javax.xml.ws.Endpoint;

public class Publisher {
	
	/*create wsdl with:
	 * wsimport -keep -p bank.soap.client.jaxws -d bin -s src http://localhost:9876/bank?wsdl
	 */

	public static void main(String[] args) {
		String url = "http://127.0.0.1:9876/bank";
		Endpoint.publish(url, new ServiceImpl());
		System.out.println("service published");
		System.out.println("WSDL available at " + url + "?wsdl");
	}
	
}

	
	