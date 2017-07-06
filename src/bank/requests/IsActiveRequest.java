package bank.requests;


import java.io.IOException;

import bank.Account;
import bank.Bank;

public class IsActiveRequest extends Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2996698340303085432L;
	private String number;
	private boolean active;
	
	public IsActiveRequest (String number) {
		this.number = number;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	@Override
	public void handleRequest(Bank b) {
		try {
			Account a = b.getAccount(this.number);
			if(a != null){
				this.active = a.isActive();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
