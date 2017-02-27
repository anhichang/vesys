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
	private static  Socket s;
	private static PrintWriter out;
	private static BufferedReader in;

	@Override
	public void connect(String[] args) throws IOException  {
		System.out.println(args[0]);
		System.out.println(args[1]);
		bank = new Bank();
		String host = args[0];
		int port = Integer.parseInt(args[1]);

		if (args.length > 0) { host = args[0]; }
		if (args.length > 1) { port = Integer.parseInt(args[1]); }
		System.out.println("LocalDriver: connecting to " + host + ":" + port);

		s = new Socket(host, port);		
		
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new PrintWriter(s.getOutputStream());
		
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
		System.out.println("LocalDriver: disconnected...");
	}

	@Override
	public Bank getBank() {
		System.out.println("LocalDriver: getBank-Method");
		return bank;
	}

	static class Bank implements bank.Bank {
		static Map<String, Account> accounts = new HashMap<>();

		@Override
		public Set<String> getAccountNumbers() throws IOException {
			System.out.println("LocalDriver: " + "Bank.getAccountNumbers");
			HashSet<String> accountNumbers = new HashSet<>();
			out.println("getAccountNumbers");
			out.flush();
			String as = in.readLine();
			if(as.equals("null")) return accountNumbers;
			System.out.println(as+ " bekommen");
			String[] accountNumbersString = as.split("/");
			for (String value : accountNumbersString) {
				System.out.print(value + "" );
				accountNumbers.add(value);
				}
			reloadAccs();
			return accountNumbers;
		}

		@Override
		public String createAccount(String owner) throws IOException {
			System.out.println("Local: Bank.createAccount");
			out.println("createacc/" + owner + '/');
			out.flush();
			System.out.println("Local: Bank.createAccount");
			String accNum = in.readLine();
			reloadAccs();
			return accNum;	
		}

		@Override
		public boolean closeAccount(String number) throws IOException {
			System.out.println("LocalDriver: closeAccount-Method");
			out.println("close/" + number);
			out.flush();

			String closedOpen = in.readLine();
			reloadAccs();
			return Boolean.parseBoolean(closedOpen);
		}

		@Override
		public bank.Account getAccount(String number) throws IOException {
			System.out.println("LocalDriver: getAccount-Method");
			out.println("getacc/" + number);
			out.flush();
			String as = in.readLine();
			System.out.println(as + "hhhhhhh");
			if(as.equals("null")) return null;
			System.out.println("iiiiiii");		
			String[] accDaten = as.split("/");
			Account acc = new Account(accDaten[2], accDaten[0]);
			try {
				acc.deposit( Double.parseDouble(accDaten[1]));
				acc.active = Boolean.parseBoolean(accDaten[3]);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InactiveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			reloadAccs();
			return acc;
		}

		@Override
		public void transfer(bank.Account from, bank.Account to, double amount)
				throws IOException, InactiveException, OverdrawException {
			System.out.println("LocalDriver: transfer-method");
			if(from.isActive() == false || to.isActive() == false) throw new InactiveException();
			if(amount > from.getBalance()) throw new OverdrawException();
			if(amount < 0)throw new IllegalArgumentException();
			
			out.println("transfer/" + from.getNumber() + "/" + to.getNumber()+ "/" + amount);
			out.flush();
			// TODO IOException noch machen
			System.out.println("Bank.transfer");
			reloadAccs();
		}
		
		public static void reloadAccs() throws IOException{
		System.out.println("LocalDriver: reloadAccs-method");
			if(accounts.size()>0){
		
				out.println("reloadaccs/");
				out.flush();
				String reloadedAccs = in.readLine();
				String[] reload = reloadedAccs.split("/");
				System.out.println(reload[0]+ " ..........");
				for(int i=0; i<reload.length-4;i++){
					Account acc = new Account(reload[2], reload[0]);
					try {
						acc.deposit( Double.parseDouble(reload[1]));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InactiveException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					acc.active = Boolean.parseBoolean(reload[3]);
					accounts.put(reload[0],acc );
				}

			System.out.println("LocalDriver: END reloadAccs-method");
		}else {
			accounts = new HashMap<String, Driver.Account>();
		}
		}
	}

	static class Account implements bank.Account {
		private String number;
		private String owner;
		private double balance;
		private boolean active;

		Account(String owner, String accountNumber) {
			this.owner = owner;
			this.number = accountNumber;
			balance = 0;
			active = true;
			// TODO account number has to be set here or has to be passed using the constructor
		}

		@Override
		public double getBalance() throws IOException {
			System.out.println("LocalDriver: getBalance-Method");
			out.println("getbalance/" + this.number + '/');
			out.flush();
			String balan= in.readLine();
			return Double.parseDouble(balan);
		}

		@Override
		public String getOwner() throws IOException {
			System.out.println("LocalDriver: getOwner-Method");
			out.println("getowner/" +this.number + '/');
			out.flush();
			String owner= in.readLine();	
			return owner;
		}

		@Override
		public String getNumber() throws IOException {
			System.out.println("LocalDriver: getNumber-Method");
			out.println("getnumber/" + this.number + '/');
			out.flush();
			String owner= in.readLine();
			Bank.reloadAccs();
			return owner;
		}

		@Override
		public boolean isActive() throws IOException {
			System.out.println("LocalDriver: isActive-Method");
			out.println("isactive/" +this.number + '/');
			out.flush();
			String acti= in.readLine();	
			Bank.reloadAccs();
			return Boolean.parseBoolean(acti);
		}

		@Override
		public void deposit(double amount) throws InactiveException, IOException  {			
			System.out.println("LocalDriver Account.deposit");	
			out.println("deposit/" + this.number + '/' + amount + '/');
			out.flush();
			String input = in.readLine();			
			if(input.equals("wrong")) throw new InactiveException();
			Bank.reloadAccs();
		}

		@Override
		public void withdraw(double amount) throws InactiveException, OverdrawException, IOException {
			System.out.println("LocalDriver: withdraw-Method");
			out.println("withdraw/" +this.number + '/' + Double.toString(amount) + '/');
			out.flush();
			if(!this.active) throw new InactiveException();
			if(amount <0) throw new IllegalArgumentException();
			if(amount < this.balance) throw new OverdrawException();
			Bank.reloadAccs();
			System.out.println("Local Account.withdraw");
			
		}

	}

}