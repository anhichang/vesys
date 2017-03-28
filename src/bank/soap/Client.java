package bank.soap.client;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import bank.InactiveException;
import bank.OverdrawException;
import bank.soap.client.jaxws.IOException_Exception;
import bank.soap.client.jaxws.InactiveException_Exception;
import bank.soap.client.jaxws.OverdrawException_Exception;
import bank.soap.client.jaxws.ServiceImpl;
import bank.soap.client.jaxws.ServiceImplService;

public class Client implements bank.BankDriver {

	private Bank bank = null;
	static ServiceImplService service = new ServiceImplService();
	static ServiceImpl port = service.getServiceImplPort();
	
	@Override
	public void connect(String[] args) throws IOException {
		bank = new Bank();
		System.out.println("DriverServer: connected...");
	}

	@Override
	public void disconnect() throws IOException {
		bank = null;
		System.out.println("DriverServer: disconnected...");
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	static class Bank implements bank.Bank{

		@Override
		public String createAccount(String owner) throws IOException {
			try {
				return port.createAccount(owner);
			} catch (IOException_Exception e) {
				e.printStackTrace();
				throw new IOException();
			}
		}

		@Override
		public boolean closeAccount(String number) throws IOException {
			try {
				return port.closeAccount(number);
			} catch (IOException_Exception e) {
				e.printStackTrace();
				throw new IOException();
			}
		}

		@Override
		public Set<String> getAccountNumbers() throws IOException {
			try {
				return new HashSet<String>(port.getAccountNumbers());
			} catch (IOException_Exception e) {
				e.printStackTrace();
				throw new IOException();
			}
		}

		@Override
		public Account getAccount(String number) throws IOException {
			try {
				if(port.getAccount(number))
					return new Account(number);
			} catch (IOException_Exception e) {
				e.printStackTrace();
				throw new IOException();
			}
			return null;
		}

		@Override
		public void transfer(bank.Account a, bank.Account b, double amount)
				throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
			try {
				port.transfer(a.getNumber(), b.getNumber(), amount);
			} catch (IOException_Exception e) {
				throw new IOException();
			} catch (InactiveException_Exception e) {
				throw new InactiveException();
			} catch (OverdrawException_Exception e) {
				throw new OverdrawException();
			}
			
		}

	}
	
	static class Account implements bank.Account{
		private String number;
		
		Account(String number){
			this.number = number;
		}
		
		@Override
		public String getNumber() throws IOException {
			return number;
		}

		@Override
		public String getOwner() throws IOException {
			try {
				return port.getOwner(number);
			} catch (IOException_Exception e) {
				e.printStackTrace();
				throw new IOException();
			}
		}

		@Override
		public boolean isActive() throws IOException {
			try {
				return port.isActive(number);
			} catch (IOException_Exception e) {
				e.printStackTrace();
				throw new IOException();
			}
		}

		@Override
		public void deposit(double amount) throws IOException, IllegalArgumentException, InactiveException {
			try {
				port.deposit(number, amount);
			} catch (IOException_Exception e) {
				e.printStackTrace();
				throw new IOException();
			} catch (InactiveException_Exception e){
				throw new InactiveException();
			}
		}

		@Override
		public void withdraw(double amount)
				throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
			try {
				port.withdraw(number, amount);
			} catch (IOException_Exception e) {
				e.printStackTrace();
				throw new IOException();
			} catch (InactiveException_Exception e){
				throw new InactiveException();
			} catch (OverdrawException_Exception e) {
				throw new OverdrawException();
			}
		}

		@Override
		public double getBalance() throws IOException {
			try {
				return port.getBalance(number);
			} catch (IOException_Exception e) {
				e.printStackTrace();
				throw new IOException();
			}
		}
		
	}
	
}
