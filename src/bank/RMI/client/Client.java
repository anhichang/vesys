package bank.RMI.client;

import java.io.IOException;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.NotBoundException;



import bank.Bank;

import bank.BankDriver2;


public class Client implements BankDriver2{
	
	private RmiBank bank;
	
	@Override
	public void connect(String[] args) throws IOException {
		InetAddress Ilocalhost = java.net.InetAddress.getLocalHost();
		String localhost = Ilocalhost.getHostAddress();
		System.out.println(localhost);
		
	try {
		bank = (RmiBank)Naming.lookup("rmi://"+localhost+":1099/Bank");
		System.out.println("rmi://localhost/Bank");
	} catch (NotBoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
	}

	@Override
	public void disconnect() throws IOException {
		bank = null;
		
	}

	@Override
	public Bank getBank() {
	
		return bank;
	}

	@Override
	public void registerUpdateHandler(UpdateHandler handler) throws IOException {
		// TODO Auto-generated method stub
	  bank.registerUpdateHandler(new Handler(handler));
	}
	
	
	
	
	
	

}
