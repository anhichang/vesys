/*
 * Copyright (c) 2000-2017 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sun.accessibility.internal.resources.accessibility;

import bank.InactiveException;
import bank.OverdrawException;
import bank.sockets.DriverServer.Account;

public class Driver implements bank.BankDriver {
	private static Bank bank = null;
	private static Socket s;
	private static BufferedReader in;
	private static PrintWriter out;

	@Override
	public void connect(String[] args) throws IOException {
		System.out.println(args[0]);
		System.out.println(args[1]);
		bank = new Bank();
		String host = args[0];
		int port = Integer.parseInt(args[1]);

		if (args.length > 0) {
			host = args[0];
		}
		if (args.length > 1) {
			port = Integer.parseInt(args[1]);
		}
		System.out.println("LocalDriver: connecting to " + host + ":" + port);
		s = new Socket(host, port);
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new PrintWriter(s.getOutputStream(), true);

		// null means any network interface
		// 0 means any port
		System.out.println("LocalDriver: connected to " + s.getRemoteSocketAddress());
	}

	@Override
	public void disconnect() throws IOException {
		bank = null;
		s.close();
		out.close();
		in.close();
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	static class Bank implements bank.Bank {
		static Map<String, Account> accounts = new HashMap<>();

		@Override
		public Set<String> getAccountNumbers() throws IOException {
			HashSet<String> accountNumbers = new HashSet<>();
			out.println("getAccountNumbers");
			out.flush();
			String as = in.readLine();
			if (as.equals("null"))
				return accountNumbers;
			String[] accountNumbersString = as.split("/");
			for (String value : accountNumbersString) {
				accountNumbers.add(value);
			}
			return accountNumbers;
		}

		@Override
		public String createAccount(String owner) throws IOException {
			out.println("createacc/" + owner);
			out.flush();
			String accNum = in.readLine();
			return accNum;
		}

		@Override
		public boolean closeAccount(String number) throws IOException {
			out.println("close/" + number);
			out.flush();
			String closedOpen = in.readLine();
			return Boolean.parseBoolean(closedOpen);
		}

		@Override
		public bank.Account getAccount(String number) throws IOException {
			out.println("getacc/" + number);
			out.flush();
			String as = in.readLine();
			if (as.equals("null"))
				return null;
			String[] accDaten = as.split("/");
			Account acc = new Account(accDaten[2]);
			try {
				acc.balance = (Double.parseDouble(accDaten[1]));
				acc.active = Boolean.parseBoolean(accDaten[3]);
				acc.number = accDaten[0];
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			return acc;
		}

		@Override
		public void transfer(bank.Account from, bank.Account to, double amount)
				throws IOException, InactiveException, OverdrawException {
			out.println("transfer/" + from.getNumber() + "/" + to.getNumber() + "/" + amount);
			out.flush();

			String input = in.readLine();
			if (input.equals("InactiveException"))
				throw new InactiveException();
			if (input.equals("OverdrawException"))
				throw new OverdrawException();
			if (input.equals("IllegalArgumentException"))
				throw new IllegalArgumentException();
		}
	}

	static class Account implements bank.Account {
		private String number;
		private String owner;
		private double balance;
		private boolean active;
		private static int iDCountter = 0;

		Account(String owner) {
			this.owner = owner;
			this.number = "CS_" + iDCountter++;
			active = true;
		}

		@Override
		public double getBalance() throws IOException {
			out.println("getbalance/" + this.number);
			out.flush();
			String balan = in.readLine();
			if (balan.equals("null"))
				return 0;
			return Double.parseDouble(balan);
		}

		@Override
		public String getOwner() throws IOException {
			out.println("getowner/" + this.number + '/');
			out.flush();
			String owner = in.readLine();
			if (owner.equals("null"))
				return null;
			return owner;
		}

		@Override
		public String getNumber() throws IOException {

			return this.number;
		}

		@Override
		public boolean isActive() throws IOException {
			out.println("isactive/" + this.number);
			out.flush();
			String acti = in.readLine();
			return Boolean.parseBoolean(acti);
		}

		@Override
		public void deposit(double amount) throws InactiveException, IOException {
			out.println("deposit/" + this.number + "/" + amount);
			out.flush();
			String input = in.readLine();
			if (input.equals("null") || input.equals("InactiveException"))
				throw new InactiveException();
			if (input.equals("IllegalArgumentException"))
				throw new IllegalArgumentException();
		}

		@Override
		public void withdraw(double amount) throws InactiveException, OverdrawException, IOException {
			out.println("withdraw/" + this.number + '/' + Double.toString(amount));
			out.flush();
			String input = in.readLine();
			if (input.equals("InactiveException"))
				throw new InactiveException();
			if (input.equals("IllegalArgumentException"))
				throw new IllegalArgumentException();
			if (input.equals("OverdrawException"))
				throw new OverdrawException();
		}

	}

}