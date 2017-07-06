package bank.requests;

import java.io.IOException;
import java.util.Set;

import bank.Bank;

public class GetAccountNumbersRequest extends Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = 443628756866000114L;
	private Set<String> accountNumbers;

	public Set<String> getAccNumbers() {
		return accountNumbers;
	}

	@Override
	public void handleRequest(Bank b) {

		try {

			accountNumbers = b.getAccountNumbers();

		} catch (IOException e) {

			throw new RuntimeException(e);

		}

	}

}
