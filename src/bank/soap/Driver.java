/*
 * Copyright (c) 2000-2017 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.soap;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jws.WebService;
import bank.InactiveException;
import bank.OverdrawException;
import bank.soap.client.jaxws.ServiceImplService;
import bank.soap.client.jaxws.IOException_Exception;
import bank.soap.client.jaxws.InactiveException_Exception;
import bank.soap.client.jaxws.OverdrawException_Exception;
import bank.soap.client.jaxws.ServiceImpl;
public class Driver implements bank.BankDriver {
	private Bank bank = null;
	
	private static ServiceImplService service = new ServiceImplService();
	private static ServiceImpl port = service.getServiceImplPort();
	
	@Override
	public void connect(String[] args) {
		bank = new Bank();
		System.out.println("DriverServer: connected...");
	}

	@Override
	public void disconnect() {
		bank = null;
		System.out.println("DriverServer: disconnected...");
	}
	
	@Override
	public Bank getBank() {
		return bank;
	}

	@WebService
	static class Bank implements bank.Bank {

		@Override
		public Set<String> getAccountNumbers() throws IOException  {
			try {
				return new HashSet<String>(port.getAccountNumbers());
			} catch (IOException_Exception e) {
				throw new IOException();
			}
		}

		@Override
		public String createAccount(String owner) throws IOException {
			try {
				return port.createAccount(owner);
			} catch (IOException_Exception e) {
				throw new IOException();
			}
		}

		@Override
		public boolean closeAccount(String number) throws IOException {
			try {
				return port.closeAccount(number);
			} catch (IOException_Exception e) {
				throw new IOException();
			}
		}

		@Override
		public bank.Account getAccount(String number) throws IOException {
			try {
				if(port.getAccountNumbers().contains(number)) return new Account(number);
				else return null;
			} catch (IOException_Exception e) {
				throw new IOException();
			}
		}

		@Override
		public void transfer(bank.Account from, bank.Account to, double amount)
				throws IOException, InactiveException, OverdrawException {
			try {
				port.transfer(from.getNumber(), to.getNumber(), amount);
			} catch (IOException_Exception | InactiveException_Exception | OverdrawException_Exception e) {
				throw new IOException();
			}
			}
	}
	
	@WebService
	static class Account implements bank.Account {
		private final String number;
		private final String owner;
		private static int iDCountter = 0;

		Account(String owner) {
			this.owner = owner;
			this.number = "CS_" + iDCountter++;
		}

		@Override
		public double getBalance() throws IOException {
			try {
				return port.getBalance(this.number);
			} catch (IOException_Exception e) {
				throw new IOException();
			}
		}

		@Override
		public String getOwner() throws IOException {
			try {
				return port.getOwner(this.number);
			} catch (IOException_Exception e) {
				throw new IOException();
			}
		}

		@Override
		public String getNumber() throws IOException{
			return this.number;
		}

		@Override
		public boolean isActive() throws IOException {
			try {
				return port.isActive(number);
			} catch (IOException_Exception e) {
				throw new IOException();
			}
		}

		@Override
		public void deposit(double amount) throws IOException{
			try {
				port.deposit(this.number, amount);
			} catch (IOException_Exception | InactiveException_Exception e) {
				throw new IOException();
			}
		}

		@Override
		public void withdraw(double amount) throws IOException {
			try {
				port.withdraw(number, amount);
			} catch (IOException_Exception | InactiveException_Exception | OverdrawException_Exception e) {
				throw new IOException();
			}
		}
	}
}
