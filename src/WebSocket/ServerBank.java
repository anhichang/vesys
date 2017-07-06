package WebSocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


import bank.BankDriver2;
import bank.Account;
import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;

public class ServerBank implements Bank {
	
	private final bank.Bank bank;
	private final BankDriver2.UpdateHandler handler;

	public ServerBank(bank.Bank bank, BankDriver2.UpdateHandler handler){
		this.bank = bank;
		this.handler = handler;
	}
	
	@Override
	public String createAccount(String owner) throws IOException {
		String id = bank.createAccount(owner);
		if (id != null) handler.accountChanged(id);
		return id;
	}

	@Override
	public boolean closeAccount(String number) throws IOException {
		boolean res = bank.closeAccount(number);
		if(res) handler.accountChanged(number);
		return res;
	}

	@Override
	public Set<String> getAccountNumbers() throws IOException {
		return new HashSet<String>(bank.getAccountNumbers());
	}

	@Override
	public Account getAccount(String number) throws IOException {
		Account acc = bank.getAccount(number);
		return acc == null ? null : new ServerAccount(acc, handler);
	}

	@Override
	public void transfer(Account a, Account b, double amount)
			throws IOException, IllegalArgumentException, OverdrawException,
			InactiveException {
		bank.transfer(a, b, amount);
		handler.accountChanged(a.getNumber());
		handler.accountChanged(b.getNumber());
	}
	
	public static class ServerAccount implements Account {

		private final bank.Account acc;
		private final bank.BankDriver2.UpdateHandler handler;
		
		public ServerAccount(bank.Account acc, bank.BankDriver2.UpdateHandler handler){
			this.acc = acc;
			this.handler = handler;
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
		public void deposit(double amount) throws IOException,
				IllegalArgumentException, InactiveException {
			acc.deposit(amount); 
			handler.accountChanged(acc.getNumber());
		}

		@Override
		public void withdraw(double amount) throws IOException,
				IllegalArgumentException, OverdrawException, InactiveException {
			acc.withdraw(amount); 
			handler.accountChanged(acc.getNumber());
		}

		@Override
		public double getBalance() throws IOException {
			return acc.getBalance();
		}

	}

}
