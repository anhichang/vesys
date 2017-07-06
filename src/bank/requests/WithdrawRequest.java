package bank.requests;

import java.io.IOException;

import bank.Account;
import bank.Bank;

public class WithdrawRequest extends Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6470767629480152658L;
	private String number;
	private double amount;
	
	
	public WithdrawRequest (String number, double amount){
		this.number = number;
		this.amount = amount;
	}
	
	@Override
	public void handleRequest(Bank b) {
		try {
			Account a = b.getAccount(this.number);
			if (a != null){
				try {
					a.withdraw(this.amount);
				} catch (Exception e) {
					this.setException(e);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
