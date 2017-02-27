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

	private static BufferedReader in;
	private static PrintWriter out;
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
				System.out.println("HauptServer start::::");
				Socket s = server.accept();
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				out = new PrintWriter(s.getOutputStream(),true);
				t = new Thread(new EchoHandler(s));
				t.start();
			}
		}
	}

	private static class EchoHandler implements Runnable {
		private final Socket s;
		
		private EchoHandler(Socket s) {
			this.s = s;
		}

		public void run() {
			String input;
			System.out.println("connection from " + s);
			try {
				in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				System.out.println("HauptServer:::::::::");
				input = in.readLine();
				while (input != null && !input.equals("")) {
				System.out.println("Server input: " + input);
				String[] inputs = input.split("/");	
				System.out.println("Argument0: " + (inputs[0]).toLowerCase());
				System.out.println("Server Bank ObjectReferenze: " + bank);

				switch((inputs[0]).toLowerCase()){ 
		        case "createacc": 
		        	System.out.println("case: createAcc");
		        	System.out.println("createAcc owner: " + inputs[1]);
		            String accNumber = bank.createAccount(inputs[1]);
		            out.write(accNumber+ '\n');
		            out.flush();
		            break; 
		            
		        case "close": 
		        	System.out.println("close");
		            boolean closedOpen= bank.closeAccount(inputs[1]);
		            out.write(Boolean.toString(closedOpen) + '\n');
		            out.flush();
		            break;
		        case "transfer": 
		        	System.out.println("case: transfer");
		        	try{
			            bank.transfer(bank.getAccount(inputs[1]), bank.getAccount(inputs[2]), Double.parseDouble(inputs[3]));
		        	}catch (Exception e) {
						System.out.println(e);
					}
		            break; 
		        case "getacc": 
		        	System.out.println("case: getacc");
		       
		        	if(inputs.length ==1){
		        		out.println("null");
		        		out.flush();
		        		System.out.println("......");
		        		break;
		        	}
		        	Account acc = (Account) bank.getAccount(inputs[1]);	
		        	if(acc!= null){		        	
		        	out.println(acc.getNumber() + "/" + acc.getBalance() + "/"  + acc.getOwner() + "/"+ acc.isActive() + "/");
		        	System.out.println("case: getacc: " +acc.getNumber() + "/" + acc.getBalance() + "/"  + acc.getOwner() + "/"+ acc.isActive() + "/" );
		        	out.flush();
		        	}else{
		        		out.println("null");
		        		out.flush();
		        	}
		        	System.out.println("end of getacc-case");
		        	break;
		        case "getaccountnumbers":
		        	System.out.println("case: getAccountNumbers");
		        	Set<String> accNum = bank.getAccountNumbers();
		        	if(accNum.size()==0){
		        		out.println("null");
		        		out.flush();
		        		break;
		        	}
		        	String allAccountNumber = "";
		        	for(String acc1: accNum){
		        		allAccountNumber += acc1 + "/";
		        		System.out.println(allAccountNumber);
		        	}
		        	out.println(allAccountNumber);
		        	out.flush();
		        	break;
		        case "getbank":	
		        	System.out.println("case: getbank");
		        	String bk = "";
		        	Set<String> accNumbers = bank.getAccountNumbers();
		        	
		        	for(String accNumb: accNumbers){
		        		System.out.println(accNumb);
		        		bk = bk + accNumb + "/"+ bank.getAccount(accNumb).getOwner()+ "/";
		        	} 
		        	System.out.println("getbank case: " + bk);
		        	out.println((bk));
		        	out.flush();
		        	System.out.println("end of case: getbank");
		        	break;
		        case "getbalance":	
		        	System.out.println("case: getBalance");
		        	double balance = bank.getAccount(inputs[1]).getBalance();
		        	out.println(Double.toString(balance));
		        	out.flush();
		        	System.out.println("end of case: getBalance");
		        	break;	
		        case "getowner":	
		        	System.out.println("case: getOwner");
		        	String owner = bank.getAccount(inputs[1]).getOwner();
		        	out.println(owner);
		        	out.flush();
		        	System.out.println("end of case: owner");
		        	break;	
		        case "getnumber":	
		        	System.out.println("case: getNumber");
		        	String number = bank.getAccount(inputs[1]).getNumber();
		        	out.println(number);
		        	out.flush();
		        	System.out.println("end of case: getNumber");
		        	break;	 
		        case "isactive":	
		        	System.out.println("case: isActive");
		        	Boolean active = bank.getAccount(inputs[1]).isActive();
		        	out.println(Boolean.toString(active));
		        	out.flush();
		        	System.out.println("end of case: isActive");
		        	break;
		        case "deposit":	
		        	System.out.println("case: deposit");		        	
					try {
						bank.getAccount(inputs[1]).deposit(Double.parseDouble(inputs[2]));
						out.println("ok");
						out.flush();
					} catch (NumberFormatException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IllegalArgumentException e1) {
						out.println("wrong");
						out.flush();
						e1.printStackTrace();
					} catch (InactiveException e1) {
						out.println("wrong");
						out.flush();
						e1.printStackTrace();
					}	

		        	System.out.println("end of case: deposit");
		        	break;	
		        case "withdraw":	
		        	System.out.println("case: withdraw");			
					try {
						bank.getAccount(inputs[1]).withdraw(Double.parseDouble(inputs[2]));
					} catch (IllegalArgumentException | OverdrawException | InactiveException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
		        	System.out.println("end of case: withdraw");
		        	break;
		        case "reloadaccs":
		        	System.out.println("case: reloadaccs");	
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
