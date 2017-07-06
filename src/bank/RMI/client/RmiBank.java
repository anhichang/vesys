package bank.RMI.client;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import bank.Bank;

public interface RmiBank extends Bank, Remote {

	public String NAME = RmiBank.class.getName();

	interface RmiUpdateHandler extends Remote {
		void accountChanged(String id) throws IOException;
	}

	public void registerUpdateHandler(RmiUpdateHandler handler) throws RemoteException;

}
