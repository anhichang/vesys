/*
 * Copyright (c) 2000-2017 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sun.accessibility.internal.resources.accessibility;

import bank.InactiveException;
import bank.OverdrawException;

public class DriverServer implements bank.BankDriver {
	private Bank bank = null;

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

	static class Bank implements bank.Bank {

		private final Map<String, Account> accounts = new HashMap<>();

		@Override
		public Set<String> getAccountNumbers() {
			HashSet<String> accountNumbers = new HashSet<>();

			for (Account value : accounts.values()) {
				if (value.isActive()) {
					accountNumbers.add(value.getNumber());
				}
			}

			return accountNumbers;
		}

		@Override
		public String createAccount(String owner) {
			if (owner == null) {
				return null;
			}
			final Account acc = new Account(owner);
			accounts.put(acc.getNumber(), acc);
			return acc.getNumber();

		}

		@Override
		public boolean closeAccount(String number) {
			Account actuelAccount = accounts.get(number);
			if(actuelAccount == null) return false;
			
			if (actuelAccount.getBalance() == 0 && actuelAccount.isActive()) {
				actuelAccount.active = false;
				return true;
			}
			return false;
		}

		@Override
		public bank.Account getAccount(String number) {
			return accounts.get(number);
		}

		@Override
		public void transfer(bank.Account from, bank.Account to, double amount)
				throws IOException, InactiveException, OverdrawException {

			if (!from.isActive() || !to.isActive())
				throw new InactiveException();
			if (amount > from.getBalance())
				throw new OverdrawException();
			if (amount < 0)
				throw new IllegalArgumentException();

			from.withdraw(amount);
			to.deposit(amount);

		}

	}

	static class Account implements bank.Account {
		private final String number;
		private final String owner;
		private double balance = 0;
		private boolean active = true;
		private static int iDCountter = 0;

		Account(String owner) {
			this.owner = owner;
			this.number = "CS_" + iDCountter++;
		}

		@Override
		public double getBalance() {
			return balance;
		}

		@Override
		public String getOwner() {
			return owner;
		}

		@Override
		public String getNumber() {
			return number;
		}

		@Override
		public boolean isActive() {
			return active;
		}

		@Override
		public void deposit(double amount) throws InactiveException {
			if (!this.active)
				throw new InactiveException();
			if (amount < 0)
				throw new IllegalArgumentException();
			balance += amount;
		}

		@Override
		public void withdraw(double amount) throws InactiveException, OverdrawException {
			if (!active)
				throw new InactiveException();
			if (amount < 0)
				throw new IllegalArgumentException();
			if (balance - amount < 0)
				throw new OverdrawException();
			balance -= amount;
		}

	}

}