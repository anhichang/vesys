package bank.RMI.client;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import bank.Account;
import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;

public class BankImpl extends java.rmi.server.UnicastRemoteObject implements bank.RMI.client.RmiBank {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6522629187925965880L;
	private final Bank bank;

	public BankImpl(Bank bank) throws RemoteException {
		this.bank = bank;
	}

	public String createAccount(String owner) throws IOException {
		String id = bank.createAccount(owner);
		if (id != null)
			notifyListeners(id);
		return id;
	}

	@Override
	public boolean closeAccount(String number) throws IOException {
		boolean res= bank.closeAccount(number);
		if(res) notifyListeners(number);
		return res;
	}

	@Override
	public Set<String> getAccountNumbers() throws IOException {

		return new HashSet<String>(bank.getAccountNumbers());
	}

	@Override
	public Account getAccount(String number) throws IOException {

		bank.Account a = bank.getAccount(number);
		return a == null ? null : new AccountImpl(a);
	}

	@Override
	public void transfer(Account a, Account b, double amount)
			throws IOException, IllegalArgumentException, OverdrawException, InactiveException {

		bank.transfer(a, b, amount);

	}

	@Override
	public void registerUpdateHandler(RmiUpdateHandler handler) throws RemoteException {
		listeners.add(handler);

	}

	private static List<RmiBank.RmiUpdateHandler> listeners = new CopyOnWriteArrayList<>();

	static void notifyListeners(String id) throws IOException {
		for (RmiBank.RmiUpdateHandler h : listeners) {
			h.accountChanged(id);
		}
	}

	

}