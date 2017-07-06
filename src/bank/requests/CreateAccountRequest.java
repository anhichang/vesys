package bank.requests;

import java.io.IOException;

import bank.Bank;

public class CreateAccountRequest extends Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1523578646297759630L;
	private String owner;
	private String accNr = null;

	public CreateAccountRequest(String owner) {
		this.owner = owner;
	}

	public String getAccNr() {
		return accNr;
	}

	@Override
	public void handleRequest(Bank b) {
		// TODO Auto-generated method stub
		try {
			accNr = b.createAccount(owner);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

}
