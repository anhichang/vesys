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

	private static Bank bank;
	private static Thread t;

	public static void main(String args[]) throws IOException {
		// int address =Integer.parseInt(args[3]);
		int port = Integer.parseInt(args[1]);
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
			out = new PrintWriter(s.getOutputStream(), true);
			;
		}

		public void run() {
			String input;
			System.out.println("connection from " + s);
			try {
				input = in.readLine();
				while (input != null && !input.equals("")) {
					System.out.println("Server input: " + input);
					String[] inputs = input.split("/");
					switch ((inputs[0]).toLowerCase()) {
					case "createacc":
						String accNumber = bank.createAccount(inputs[1]);
						out.println(accNumber);
						out.flush();
						break;

					case "close":
						boolean closedOpen = bank.closeAccount(inputs[1]);
						out.println(Boolean.toString(closedOpen));
						out.flush();
						break;
					case "transfer":
						try {
							bank.transfer(bank.getAccount(inputs[1]), bank.getAccount(inputs[2]),
									Double.parseDouble(inputs[3]));
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
						if (inputs.length == 1) {
							out.println("null");
							out.flush();
							break;
						}
						Account acc = (Account) bank.getAccount(inputs[1]);
						if (acc != null) {
							out.println(acc.getNumber() + "/" + acc.getBalance() + "/" + acc.getOwner() + "/"
									+ acc.isActive());
							out.flush();
						} else {
							out.println("null");
							out.flush();
						}
						break;
					case "getaccountnumbers":
						Set<String> accNum = bank.getAccountNumbers();
						if (accNum.size() == 0) {
							out.println("null");
							out.flush();
							break;
						}
						String allAccountNumber = "";
						for (String acc1 : accNum) {
							allAccountNumber += acc1 + "/";
						}
						out.println(allAccountNumber);
						out.flush();
						break;
					case "getbalance":
						Account accBalance = (Account) bank.getAccount(inputs[1]);
						if (accBalance == null)
							out.println("null");
						double balance = accBalance.getBalance();
						out.println(balance);
						out.flush();
						break;
					case "getowner":
						Account accOwner = (Account) bank.getAccount(inputs[1]);
						if (accOwner == null)
							out.println("null");
						String owner = accOwner.getOwner();
						out.println(owner);
						out.flush();
						break;
					case "isactive":
						Boolean active = bank.getAccount(inputs[1]).isActive();
						out.println(Boolean.toString(active));
						out.flush();
						break;
					case "deposit":
						try {
							Account accDeposit = (Account) bank.getAccount(inputs[1]);
							if (accDeposit == null) {
								out.println("InactiveException");
								out.flush();
							}
							accDeposit.deposit(Double.parseDouble(inputs[2]));
							out.println("ok");
							out.flush();
						} catch (NumberFormatException e1) {
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
					default:
						throw new IllegalArgumentException();
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
