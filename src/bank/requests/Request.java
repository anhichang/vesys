package bank.requests;

import java.io.Serializable;

import bank.Bank;

public abstract class Request implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5627156462293179522L;
	private Exception e;
	
	public void setException(Exception e){
		this.e = e;
	}
	
	public Exception getException(){
		return e;
	}
	
	public abstract void handleRequest(Bank b);
	
}
