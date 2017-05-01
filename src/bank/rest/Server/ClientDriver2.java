package bank.rest.Server;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import bank.InactiveException;
import bank.OverdrawException;

//Alt-> Noch nicht kontrolliert

public class ClientDriver2 implements bank.BankDriver {
	private Bank bank = null;
	static Client c = ClientBuilder.newClient();
	static WebTarget r;
	private static final String BASE_URL = "http://localhost:9998/bank";
	
	@Override
	public void connect(String[] args) throws IOException {
		bank = new Bank();
		r = c.target(BASE_URL);
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
	
		@Override
		public String createAccount(String owner) throws IOException {
			Form f = new Form();
			//nur string möglich
			f.param("owner", owner);
			
			//Wenn mehrere Forms Values
//			MultivaluedMap<String,String> formData = new MultivaluedHashMap<>();
//			formData.add("vorname", "anhi");
//			formData.add("nachname", "chang");
			
			r=c.target(BASE_URL + "/accounts");
			Response response = r.request().accept(MediaType.TEXT_PLAIN).post(Entity.form(f));
			String result = response.readEntity(String.class);
			System.out.println("createAccount " + result);
			return result;
		}
		
		
		// Mit queryParam
		public String createAccount2(String owner) throws IOException {
			r=c.target(BASE_URL + "/accounts");
			// GET
			Response response = r.queryParam("name", owner,"nummer", 3).request().accept(MediaType.TEXT_PLAIN).get();
			String result = response.readEntity(String.class);
			System.out.println("createAccount " + result);
			return result;
		}
		


		@Override
		public boolean closeAccount(String number) throws IOException {
			r=c.target(BASE_URL + "/accounts/" + number);
			String response = r.request().accept(MediaType.TEXT_PLAIN).delete(String.class);
			return Boolean.parseBoolean(response);
		}

		@Override
		public Set<String> getAccountNumbers() throws IOException {
			HashSet<String> accountNumbers = new HashSet<>();
			r=c.target(BASE_URL + "/accounts");
			String response = r.request().accept(MediaType.TEXT_PLAIN).get(String.class);
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
		public Account getAccount(String number) throws IOException {
			r=c.target(BASE_URL + "/accounts/" + number);
			String response = r.request().accept(MediaType.TEXT_PLAIN).get(String.class);
			System.out.println("account " + response);
			if(response.equals("null")){
				return null;
			}
			String[] account = response.split("/");
			return new Account(account[0]);
		}

		@Override
		public void transfer(bank.Account a, bank.Account b, double amount)
				throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
			r=c.target(BASE_URL + "/accounts/"+ a.getNumber() + "/" + b.getNumber() + "/" + amount);
			String response = r.request().put(Entity.entity(amount, MediaType.TEXT_PLAIN)).toString();
			System.out.println("transfer " + response);
		}
	
	}
	
	static class Account implements bank.Account {
		
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
			r=c.target(BASE_URL + "/accounts/getOwner/" + number);
			String response = r.request().accept(MediaType.TEXT_PLAIN).get(String.class);
			return response;
		}
		
		@Override
		public double getBalance() throws IOException {
			r=c.target(BASE_URL + "/accounts/getBalance/" + number);
			String response = r.request().accept(MediaType.TEXT_PLAIN).get(String.class);
			return Double.parseDouble(response);
		}

		@Override
		public boolean isActive() throws IOException {
			r=c.target(BASE_URL + "/accounts/isActive/" + number);
			String response = r.request().accept(MediaType.TEXT_PLAIN).get(String.class);
			System.out.println("isActive:" +response);
			return Boolean.parseBoolean(response);
		}

		@Override
		public void deposit(double amount) throws IOException, IllegalArgumentException, InactiveException {
			r=c.target(BASE_URL + "/accounts/" + number + "/deposit/" + amount);	
			System.out.println("deposit " + amount);
			String response = r.request().put(Entity.entity(amount, MediaType.TEXT_PLAIN)).toString();
			System.out.println(response);
		}

		@Override
		public void withdraw(double amount)
				throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
			r=c.target(BASE_URL + "/accounts/" + number + "/withdraw/" + amount);	
			System.out.println("withdraw " + amount);
			Response response = r.request().put(Entity.entity(amount, MediaType.TEXT_PLAIN));
			String result = response.readEntity(String.class);
			System.out.println(response+"hhhhhh");
			if(result.equals("-1")) throw new OverdrawException();
			if(result.equals("-2")) throw new InactiveException();
			if(result.equals("IllegalArgumentException")) throw new IllegalArgumentException();
			System.out.println(response);
		}

	}

}
