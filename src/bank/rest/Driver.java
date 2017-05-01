/*
 * Copyright (c) 2000-2017 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.Entity;


import bank.InactiveException;
import bank.OverdrawException;

//Client
public class Driver implements bank.BankDriver {
	private static Bank bank = null;
	private static Client client = ClientBuilder.newClient();
	private static WebTarget webTarget;
	private static final String BASE_URL = "http://localhost:9999/bank";

	@Override
	public void connect(String[] args) throws IOException {
		bank = new Bank();
		webTarget = client.target(BASE_URL);		
	}

	@Override
	public void disconnect() throws IOException {
		bank = null;
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
				webTarget = client.target(BASE_URL + "/accounts");
				String response = webTarget.request().accept(MediaType.TEXT_PLAIN).get(String.class);
				System.out.println(response);
				if (response.equals("null"))
				return accountNumbers;
				String[] accountNumbersString = response.split("/");
				for (String value : accountNumbersString) {
				accountNumbers.add(value);
				}
				return accountNumbers;
				}
		

		@Override
		public String createAccount(String owner) throws IOException {
			System.out.println("Client: create");
			WebTarget target = client.target(BASE_URL);
			Form f = new Form();
			f.param("owner", owner);
			Response response = target.request().accept(MediaType.TEXT_PLAIN).post(Entity.form(f));
			String id = response.readEntity(String.class);
			System.out.println("createAccount id: " + id);
			return id;
		}

		@Override
		public boolean closeAccount(String number) throws IOException {
			WebTarget target = client.target(BASE_URL + "/" + number);
			Response response = target.request().accept(MediaType.APPLICATION_XML).get();
			String resp = response.readEntity(String.class);
			return Boolean.parseBoolean(resp);
		}
       
		@Override
		public bank.Account getAccount(String number) throws IOException {			
			WebTarget target = client.target(BASE_URL + "/getAccount").queryParam("number", number);
			Response response = target.request().accept(MediaType.APPLICATION_XML) .get();
			Account account = response.readEntity(Account.class);			
			System.out.println("client: " + account);
			return account;
		}

		@Override
		public void transfer(bank.Account from, bank.Account to, double amount) throws IOException, InactiveException, OverdrawException {

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
			System.out.println(BASE_URL+ "/getAccount");
			WebTarget request = client.target(BASE_URL + "/getAccount").queryParam("number", number);
			String response = request.request().accept(MediaType.TEXT_PLAIN).get(String.class);

			return Double.parseDouble(response.split("#@#@#")[2]);
		}

		@Override
		public String getOwner() throws IOException {
			System.out.println("Drvier getOwner");
			WebTarget request = client.target(BASE_URL + "/getAccount").queryParam("number", number);
			String response = request.request().accept(MediaType.TEXT_PLAIN).get(String.class);

			return response.split("#@#@#")[1];
		}

		@Override
		public String getNumber() throws IOException {
			return this.number;
		}

		@Override
		public boolean isActive() throws IOException {
			WebTarget request = client.target(BASE_URL + "/getAccount").queryParam("number", number);
			String response = request.request().accept(MediaType.TEXT_PLAIN).get(String.class);
			return Boolean.parseBoolean(response.split("#@#@#")[3]);
		}

		@Override
		public void deposit(double amount) throws InactiveException, IOException {
			WebTarget request = client.target(BASE_URL + "/deposit").queryParam("number", number, "amount", amount);
			Integer response = Integer.parseInt(request.request().accept(MediaType.TEXT_PLAIN).get(String.class));
	
			switch (response) {
			case -1:
				throw new IllegalArgumentException();

			case -2:
				throw new IOException();

			case -3:
				throw new InactiveException();
			}		
		}

		@Override
		public void withdraw(double amount) throws InactiveException, OverdrawException, IOException {
			WebTarget request = client.target(BASE_URL + "/getAccount").queryParam("number", number);
			Integer response = Integer.parseInt(request.request().accept(MediaType.TEXT_PLAIN).get(String.class));

			switch (response) {
			case -1:
				throw new IllegalArgumentException();

			case -2:
				throw new IOException();

			case -3:
				throw new InactiveException();

			case -4:
				throw new OverdrawException();
			}
		}

	}

}