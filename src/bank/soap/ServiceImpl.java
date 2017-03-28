package bank.soap.server;

import java.io.IOException;
import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebService;

import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;
import bank.local.Driver;


@WebService
public class ServiceImpl implements Service{
	
	private Driver localDriver =new Driver();
	
	private Bank bank = localDriver.getBankSoap();

	@Override
	public Set<String> getAccountNumbers() throws IOException {
		return bank.getAccountNumbers();
	}


	@Override
	public String createAccount(@WebParam(name = "owner") String owner) throws IOException {
		return bank.createAccount(owner);
	}

	@Override
	public boolean closeAccount(@WebParam(name = "number") String number) throws IOException {
		return bank.closeAccount(number);
	}
	
	@Override
	public boolean getAccount(String number) throws IOException {
		if(bank.getAccount(number) != null){
			return true;
		}
		return false;
	}

	@Override
	public void transfer(@WebParam(name = "from") String from, @WebParam(name = "to") String to, @WebParam(name = "amount") double amount)
			throws IOException, InactiveException, OverdrawException {
		bank.transfer(bank.getAccount(from), bank.getAccount(to), amount);
	}

	@Override
	public double getBalance(@WebParam(name = "number") String number) throws IOException {
		return bank.getAccount(number).getBalance();
	}

	@Override
	public String getOwner(@WebParam(name = "number") String number) throws IOException {
		return bank.getAccount(number).getOwner();
	}

	@Override
	public boolean isActive(@WebParam(name = "number") String number) throws IOException {
		return bank.getAccount(number).isActive();
	}

	@Override
	public void deposit(@WebParam(name = "number") String number, @WebParam(name = "amount") double amount) throws InactiveException, IllegalArgumentException, IOException {
		bank.getAccount(number).deposit(amount);
	}

	@Override
	public void withdraw(@WebParam(name = "number") String number, @WebParam(name = "amount") double amount) throws InactiveException, OverdrawException, IllegalArgumentException, IOException {
		bank.getAccount(number).withdraw(amount);
	}
		
	
}
