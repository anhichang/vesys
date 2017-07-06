package bank.RMI.client;

import java.io.IOException;
import java.rmi.RemoteException;

import bank.Account;
import bank.InactiveException;
import bank.OverdrawException;

public class AccountImpl extends java.rmi.server.UnicastRemoteObject implements bank.RMI.client.RmiAccount {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4671519983598080999L;
	private final Account acc;

	public AccountImpl(Account a) throws RemoteException {
		this.acc = a;
	}

	@Override
	public String getNumber() throws IOException {
	
		return acc.getNumber();
	}

	@Override
	public String getOwner() throws IOException {
		
		return acc.getOwner();
	}

	@Override
	public boolean isActive() throws IOException {
		
		return acc.isActive();
	}

	@Override
	public void deposit(double amount) throws IOException, IllegalArgumentException, InactiveException {
		
		acc.deposit(amount);
		BankImpl.notifyListeners(acc.getNumber());
		
	}

	@Override
	public void withdraw(double amount)
			throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
		
		acc.withdraw(amount);
	}

	@Override
	public double getBalance() throws IOException {
		
		return acc.getBalance();
	}

}