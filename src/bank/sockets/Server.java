/*
 * Copyright (c) 2000-2016 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;
import bank.sockets.DriverServer.Account;

public class Server {

	static Bank bank;
	private static Thread t;
	
	public static void main(String args[]) throws IOException {
		//int address =Integer.parseInt(args[3]);
		System.out.println("HauptServer");
		int port =Integer.parseInt(args[4]);
		//int clientPort = 5678;
		DriverServer driver = new DriverServer();
		driver.connect(args); 
		bank = driver.getBank();
		
		try (ServerSocket server = new ServerSocket(port)) {
			System.out.println("HauptServer: Startet Server on port " + port);
			while (true) {
				Socket s = server.accept();
				t = new Thread(new ClientHandler(s));
				t.start();
			}
		}
	}

	private static class ClientHandler implements Runnable {
		private final Socket s;
		private final BufferedReader in;
		private final PrintWriter out;
		
		private ClientHandler(Socket s) throws IOException {
			this.s = s;
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = new PrintWriter(s.getOutputStream(),true); ;
		}

		public void run() {
			String input;
			System.out.println("connection from " + s);
			try {
				input = in.readLine();
				while (input != null && !input.equals("")) {
				System.out.println("Server input: " + input);
				String[] inputs = input.split("/");	
				switch((inputs[0]).toLowerCase()){ 
		        case "createacc": 
		            String accNumber = bank.createAccount(inputs[1]);
		            out.println(accNumber);
		            out.flush();
		            break; 
		            
		        case "close": 
		            boolean closedOpen= bank.closeAccount(inputs[1]);
		            out.println(Boolean.toString(closedOpen));
		            out.flush();
		            break;
		        case "transfer": 		
					try {
						bank.transfer(bank.getAccount(inputs[1]), bank.getAccount(inputs[2]), Double.parseDouble(inputs[3]));
						out.println("ok");
						out.flush();
					} catch (NumberFormatException e2) {
						out.println("NumberFormatException");
						out.flush();
						e2.printStackTrace();
					} catch (IllegalArgumentException e2) {
						out.println("IllegalArgumentException");
						out.flush();
						e2.printStackTrace();
					} catch (OverdrawException e2) {
						out.println("OverdrawException");
						out.flush();
						e2.printStackTrace();
					} catch (InactiveException e2) {
						out.println("InactiveException");
						out.flush();
						e2.printStackTrace();
					}

		            break; 
		        case "getacc": 		       
		        	if(inputs.length ==1){
		        		out.println("null");
		        		out.flush();
		        		break;
		        	}
		        	Account acc = (Account) bank.getAccount(inputs[1]);	
		        	if(acc!= null){		        	
		        	out.println(acc.getNumber() + "/" + acc.getBalance() + "/"  + acc.getOwner() + "/"+ acc.isActive());
		        	out.flush();
		        	}else{
		        		out.println("null");
		        		out.flush();
		        	}
		        	break;
		        case "getaccountnumbers":
		        	Set<String> accNum = bank.getAccountNumbers();
		        	if(accNum.size()==0){
		        		out.println("null");
		        		out.flush();
		        		break;
		        	}
		        	String allAccountNumber = "";
		        	for(String acc1: accNum){
		        		allAccountNumber += acc1 + "/";
		        	}
		        	out.println(allAccountNumber);
		        	out.flush();
		        	break;
		        case "getbank":	
		        	String bk = "";
		        	Set<String> accNumbers = bank.getAccountNumbers();
		        	
		        	for(String accNumb: accNumbers){
		        		bk = bk + accNumb + "/"+ bank.getAccount(accNumb).getOwner()+ "/";
		        	} 
		        	out.println(bk);
		        	out.flush();
		        	break;
		        case "getbalance":	
		        	double balance = bank.getAccount(inputs[1]).getBalance();
		        	out.println(balance);
		        	out.flush();
		        	break;	
		        case "getowner":	
		        	String owner = bank.getAccount(inputs[1]).getOwner();
		        	out.println(owner);
		        	out.flush();
		        	break;	
		        case "getnumber":	
		        	String number = bank.getAccount(inputs[1]).getNumber();
		        	out.println(number);
		        	out.flush();
		        	break;	 
		        case "isactive":	
		        	Boolean active = bank.getAccount(inputs[1]).isActive();
		        	out.println(Boolean.toString(active));
		        	out.flush();
		        	break;
		        case "deposit":	
					try {
						if(inputs.length==3 && bank.getAccountNumbers().size() > 0){
						bank.getAccount(inputs[1]).deposit(Double.parseDouble(inputs[2]));
						
						out.println("ok");
						out.flush();
						System.out.println("---------");
						}else{
							out.println("InactiveException");
							out.flush();
						}						
					}catch (NumberFormatException e1) {
						out.println("NumberFormatException");
						out.flush();
						e1.printStackTrace();
					} catch (IllegalArgumentException e1) {
						out.println("IllegalArgumentException");
						out.flush();
						e1.printStackTrace();
					} catch (InactiveException e1) {
						out.println("InactiveException");
						out.flush();
						e1.printStackTrace();
					}
		        	break;	
		        case "withdraw":	
					try {
						bank.getAccount(inputs[1]).withdraw(Double.parseDouble(inputs[2]));
						out.println("ok");
						out.flush();
					} catch (NumberFormatException e) {
						out.println("NumberFormatException");
						out.flush();
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						out.println("IllegalArgumentException");
						out.flush();
						e.printStackTrace();
					} catch (OverdrawException e) {
						out.println("OverdrawException");
						out.flush();
						e.printStackTrace();
					} catch (InactiveException e) {
						out.println("InactiveException");
						out.flush();
						e.printStackTrace();
					}
		        	break;
		        case "reloadaccs":
		        	String returnAccs = "";
		        	Set<String> accs = bank.getAccountNumbers();
		        	for(String accs1: accs){
		        		Account ac = (Account) bank.getAccount(accs1);
		        		returnAccs+= ac.getNumber() + '/' + ac.getBalance()+ '/' + ac.getOwner() + '/' + ac.isActive() + '/';
		        	}
		        	out.println(returnAccs);
		        	out.flush();
		        	break;
		        default: 
		            System.out.println("falsche eingaben"); 
				}
				input = in.readLine();
				}
				System.out.println("done serving... end of class " + s);
					
			} catch (IOException e) {
				System.err.println(e);
				throw new RuntimeException(e);
			} 
		}
	}
}
