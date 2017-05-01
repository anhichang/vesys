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
import javax.ws.rs.core.Response;

import bank.InactiveException;
import bank.OverdrawException;



public class ClientDriver implements bank.BankDriver {
	private Bank bank = null;
	static Client c = ClientBuilder.newClient();
	static WebTarget r;
	private static final String BASE_URL = "http://localhost:9999/bank";
	
	@Override
	public void connect(String[] args) throws IOException {
		bank = new Bank();
		r = c.target(BASE_URL);	// XXX ignoriert args.
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
			f.param("owner", owner);
			
			r=c.target(BASE_URL + "/accounts");
			Response response = r.request().accept(MediaType.TEXT_PLAIN).post(Entity.form(f));
			String result = response.readEntity(String.class);
			// XXX hier muss das Resultat geprüft werden. Falls result = "" => return null;
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
			System.out.println("getAccount" + response);
			if(response.equals("null")){
				return null;
			}
			String[] account = response.split("/");
			return new Account(account[0]);	// XXX wobei man hier anstelle von accounts[0] auch number übergeben könnte.
		}

		@Override
		public void transfer(bank.Account a, bank.Account b, double amount)
				throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
			r=c.target(BASE_URL + "/accounts/"+ a.getNumber() + "/" + b.getNumber() + "/" + amount);
			Response response = r.request().put(Entity.entity(amount, MediaType.TEXT_PLAIN));
			String result = response.readEntity(String.class);
			if ("-1".equals(result)){
				throw new InactiveException();
			}else if("-2".equals(result)){
				throw new OverdrawException();
			}else if("-3".equals(result)){
				throw new IllegalArgumentException();
			}else if("-4".equals(result)){
				throw new IOException();
			}
			System.out.println("transfer " + result);
		}
	
	}
	
	static class Account implements bank.Account {
		
		private String number;	// XXX sollte final deklariert werden.
		
		Account(String number){
			this.number = number;
		}


		@Override
		public String getNumber() throws IOException { // XXX "throws IOException" kann weggelassen werden.
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
			return Boolean.parseBoolean(response);
		}

		@Override
		public void deposit(double amount) throws IOException, IllegalArgumentException, InactiveException {
			r=c.target(BASE_URL + "/accounts/" + number + "/deposit/" + amount);	
			System.out.println("deposit " + amount);
			Response response = r.request().put(Entity.entity(amount, MediaType.TEXT_PLAIN));
			String result = response.readEntity(String.class);
			if ("-1".equals(result)){
				throw new InactiveException();
			}else if("-2".equals(result)){
				throw new IllegalArgumentException();
			}else if("-3".equals(result)){
				throw new IOException();
			}
		}

		@Override
		public void withdraw(double amount)
				throws IOException, IllegalArgumentException, OverdrawException, InactiveException {
			r=c.target(BASE_URL + "/accounts/" + number + "/withdraw/" + amount);	
			System.out.println("withdraw " + amount);
			Response response = r.request().put(Entity.entity(amount, MediaType.TEXT_PLAIN));
			String result = response.readEntity(String.class);
			if ("-1".equals(result)){
				throw new InactiveException();
			}else if("-2".equals(result)){
				throw new OverdrawException();
			}else if("-3".equals(result)){
				throw new IllegalArgumentException();
			}else if("-4".equals(result)){
				throw new IOException();
			}
		}

	}

}
