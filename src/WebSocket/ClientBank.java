package WebSocket;

import java.io.IOException;
import java.util.Set;

import bank.Account;
import bank.InactiveException;
import bank.OverdrawException;
import bank.requests.CloseAccountRequest;
import bank.requests.CreateAccountRequest;
import bank.requests.DepositRequest;
import bank.requests.GetAccountNumbersRequest;
import bank.requests.GetAccountRequest;
import bank.requests.GetBalanceRequest;
import bank.requests.IsActiveRequest;
import bank.requests.RequestHandler;
import bank.requests.TransferRequest;
import bank.requests.WithdrawRequest;

public class ClientBank implements bank.Bank{

	RequestHandler handler;
	
	public ClientBank(RequestHandler handler){
		this.handler = handler;
	}
	
	@Override
	public String createAccount(String owner) throws IOException {
		CreateAccountRequest r = (CreateAccountRequest) handler.handle(new CreateAccountRequest(owner));
		return r.getAccNr();
	}

	@Override
	public boolean closeAccount(String number) throws IOException {
		CloseAccountRequest r = (CloseAccountRequest) handler.handle(new CloseAccountRequest(number));
		return r.isClosed();
	}

	@Override
	public Set<String> getAccountNumbers() throws IOException {
		GetAccountNumbersRequest r = (GetAccountNumbersRequest) handler.handle(new GetAccountNumbersRequest());
		return r.getAccNumbers();
	}

	@Override
	public Account getAccount(String number) throws IOException {
		GetAccountRequest r = (GetAccountRequest) handler.handle(new GetAccountRequest(number));
		String owner = r.getOwner();
		if (owner == null){
			return null;
		} else{
			return new ClientAccount(number,r.getOwner(),handler);
		}
	}

	@Override
	public void transfer(Account a, Account b, double amount)
			throws IOException, IllegalArgumentException, OverdrawException,
			InactiveException {

		TransferRequest r = (TransferRequest) handler.handle(new TransferRequest(a.getNumber(), b.getNumber(),amount));
		Exception exc = r.getException();
		
		if(exc != null){
			if(exc instanceof IllegalArgumentException ) {
				throw (IllegalArgumentException)exc;
			} else if(exc instanceof InactiveException){
				throw (InactiveException) exc;
			} else if(exc instanceof OverdrawException){
				throw (OverdrawException) exc;
			}
		}
		
	}
	
	public class ClientAccount implements Account{
		
		private RequestHandler handler;
		private String number;
		private String owner;
		
		public ClientAccount(String number, String owner, RequestHandler handler){
			this.number = number;
			this.owner = owner;
			this.handler = handler;
		}

		@Override
		public String getNumber() throws IOException {
			return number;
		}

		@Override
		public String getOwner() throws IOException {
			return owner;
		}

		@Override
		public boolean isActive() throws IOException {
			IsActiveRequest r = (IsActiveRequest) handler.handle(new IsActiveRequest(number));
			Exception exc = r.getException();
			if(exc != null) exc.printStackTrace();
			return r.isActive();
		}

		@Override
		public void deposit(double amount) throws IOException,
				IllegalArgumentException, InactiveException {
			DepositRequest r = (DepositRequest) handler.handle(new DepositRequest(number, amount));
			Exception exc = r.getException();
			
			if(exc != null){
				if(exc instanceof IllegalArgumentException ) {
					throw (IllegalArgumentException)exc;
				} else if(exc instanceof InactiveException){
					throw (InactiveException) exc;
				} 
			}	
			
		}

		@Override
		public void withdraw(double amount) throws IOException,
				IllegalArgumentException, OverdrawException, InactiveException {
			WithdrawRequest r = (WithdrawRequest) handler.handle(new WithdrawRequest(number, amount));
			Exception exc = r.getException();
			
			if(exc != null){
				if(exc instanceof IllegalArgumentException ) {
					throw (IllegalArgumentException)exc;
				} else if(exc instanceof InactiveException){
					throw (InactiveException) exc;
				} else if(exc instanceof OverdrawException){
					throw (OverdrawException) exc;
				}
			}
		}

		@Override
		public double getBalance() throws IOException {
			GetBalanceRequest r = (GetBalanceRequest) handler.handle(new GetBalanceRequest(number));			
			return r.getBalance();
		}
	}
}
