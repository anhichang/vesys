package bank.RMI.client;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import bank.BankDriver2;

public class Handler extends UnicastRemoteObject implements RmiBank.RmiUpdateHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3332993627296110477L;
	private BankDriver2.UpdateHandler handler;

	public Handler(BankDriver2.UpdateHandler handler) throws RemoteException {
		super();
		this.handler = handler;
	}

	@Override
	public void accountChanged(String id) throws IOException {
		handler.accountChanged(id);
	}
}
