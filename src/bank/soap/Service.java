package bank.soap;

import java.io.IOException;
import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebService;

import bank.InactiveException;
import bank.OverdrawException;

@WebService
public interface Service {
	
	Set<String> getAccountNumbers() throws IOException;
	String createAccount(@WebParam(name = "owner") String owner) throws IOException;
	boolean closeAccount(@WebParam(name = "number") String number) throws IOException;
	//bank.Account getAccount(@WebParam(name = "number") String number) throws IOException;
	void transfer(@WebParam(name = "from") String from, @WebParam(name = "to") String to, @WebParam(name = "amount") double amount) throws IOException, InactiveException, OverdrawException;
	double getBalance(@WebParam(name = "number") String number) throws IOException;
	String getOwner(@WebParam(name = "number") String number) throws IOException;
	boolean isActive(@WebParam(name = "number") String number) throws IOException;
	void deposit(@WebParam(name = "number") String number, @WebParam(name = "amount") double amount) throws InactiveException, IllegalArgumentException, IOException;
	void withdraw(@WebParam(name = "number") String number, @WebParam(name = "amount") double amount) throws InactiveException, OverdrawException, IllegalArgumentException, IOException; 
	
}
