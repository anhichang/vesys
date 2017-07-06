package bank.RMI.server;

import java.net.UnknownHostException;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			System.out.println(java.net.InetAddress.getLocalHost());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
