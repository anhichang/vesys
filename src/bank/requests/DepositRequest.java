package bank.requests;

import java.io.IOException;

import bank.Account;
import bank.Bank;

public class DepositRequest extends Request {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3196050511906903436L;
	private String number;
	private double amount;
	
	
	public DepositRequest (String number, double amount){
		this.number = number;
		this.amount = amount;
	}
	
	@Override
	public void handleRequest(Bank b) {
		try {
			Account a = b.getAccount(this.number);
			if (a != null){
				try {
					a.deposit(this.amount);
				} catch (Exception e) {
					this.setException(e);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
