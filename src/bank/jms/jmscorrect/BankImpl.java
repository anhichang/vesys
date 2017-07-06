package bank.jms.jmscorrect;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import bank.Account;
import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;
import bank.BankDriver2.UpdateHandler;;

public class BankImpl implements Bank {

	private final Bank bank;
	private final UpdateHandler handler;

	public BankImpl(LocalBank bank, UpdateHandler handler) {
		this.bank = bank;
		this.handler = handler;
	}

	@Override
	public String createAccount(String owner) throws IOException {

		String id = bank.createAccount(owner);
		if (id != null)
			handler.accountChanged(id);
		return id;

	}

	@Override
	public boolean closeAccount(String number) throws IOException {
		boolean isClosed = bank.closeAccount(number);
		if (isClosed)
			handler.accountChanged(number);

		return isClosed;
	}

	@Override
	public Set<String> getAccountNumbers() throws IOException {

		return new HashSet<String>(bank.getAccountNumbers());
	}

	@Override
	public Account getAccount(String number) throws IOException {
		Account acc = bank.getAccount(number);
		return acc == null ? null : new AccountImpl(acc, handler);

	}

	@Override
	public void transfer(Account a, Account b, double amount)
			throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
		bank.transfer(a, b, amount);

		handler.accountChanged(a.getNumber());
		handler.accountChanged(b.getNumber());
	}

}
