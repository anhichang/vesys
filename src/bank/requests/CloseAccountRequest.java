package bank.requests;

import java.io.IOException;

import bank.Bank;

public class CloseAccountRequest extends Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3797411762238850365L;
	private boolean isClosed;
	private String number;

	public CloseAccountRequest(String number) {
		this.number = number;
	}

	public boolean isClosed() {
		return isClosed;
	}

	@Override
	public void handleRequest(Bank b) {
		// TODO Auto-generated method stub
		try {
			isClosed = b.closeAccount(number);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
