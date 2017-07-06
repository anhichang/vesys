package bank.requests;


import java.io.IOException;

import bank.Account;
import bank.Bank;

public class GetAccountRequest extends Request{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6681119281917494733L;
	private String owner;
	private String number;
	
	public GetAccountRequest(String number){
		this.number = number;
	}
	
	public String getOwner(){
		return owner;
	}
	
	@Override
	public void handleRequest(Bank b) {
		try {
			Account a = b.getAccount(this.number);
			if(a != null){
				this.owner = a.getOwner();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

}
