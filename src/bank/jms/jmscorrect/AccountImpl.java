package bank.jms.jmscorrect;

import java.io.IOException;

import bank.InactiveException;
import bank.OverdrawException;

public class AccountImpl implements bank.Account{
	
	private final bank.Account acc;
	private final bank.BankDriver2.UpdateHandler handler;
	
	public AccountImpl(bank.Account acc, bank.BankDriver2.UpdateHandler handler){
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
	public void deposit(double amount) throws IOException, IllegalArgumentException, InactiveException {
		acc.deposit(amount);
		handler.accountChanged(acc.getNumber());
		
	}

	@Override
	public void withdraw(double amount)
			throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
		acc.withdraw(amount);
		handler.accountChanged(acc.getNumber());
	}

	@Override
	public double getBalance() throws IOException {
		return acc.getBalance();
	}

}
