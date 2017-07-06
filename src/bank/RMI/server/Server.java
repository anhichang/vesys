package bank.RMI.server;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;

import bank.Bank;
import bank.RMI.client.BankImpl;
import bank.local.Driver;

public class Server {
	public static void main(String[] args) {
		try {// use e.g. local (threadsafe) bank
			//System.setProperty("java.rmi.server.hostname","127.0.0.1");
			Driver driver = new Driver();
			Bank localBank = driver.getBank();
			BankImpl bank = new BankImpl(localBank);
			LocateRegistry.createRegistry(1099);
			System.out.println("cdvvwvb dsvvds");
			Naming.rebind("Bank", (Remote) bank);
			System.out.println("Server started...");
			System.out.println(LocateRegistry.getRegistry());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}