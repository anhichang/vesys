package bank.requests;


import java.io.IOException;

import bank.Account;
import bank.Bank;

public class GetBalanceRequest extends Request {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -746501627936419316L;
	private String number;
	private double balance;
	
	public GetBalanceRequest(String number){
		this.number = number;
	}
	
	public double getBalance(){
		return this.balance;
	}
	
	@Override
	public void handleRequest(Bank b) {
		try {
			Account a = b.getAccount(this.number);
			if (a != null){
					this.balance = a.getBalance();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
