/*
 * Copyright (c) 2000-2016 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package bank.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;
import bank.http.DriverServer.Account;

public class Server {

	private static Bank bank;

	public static void main(String args[]) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(1234), 0);
		DriverServer driver = new DriverServer(); // Object von Local-Logic
		driver.connect(args); // nur damit es initalisiert wird
		bank = driver.getBank();
		server.createContext("/startbank", new BankHandler()).getFilters().add(new ParameterParser());
		server.createContext("/createuser", new CreateUserHandler()).getFilters().add(new ParameterParser());
		server.createContext("/getuser", new GetUserHandler()).getFilters().add(new ParameterParser());
		server.start();
		String nr = bank.createAccount("anhi");
		try {
			bank.getAccount(nr).deposit(20000);
		} catch (IllegalArgumentException | InactiveException e) {
			e.printStackTrace();
		}
		System.out.println("server started on " + server.getAddress());
	}

	static class BankHandler implements HttpHandler {
		@SuppressWarnings("unchecked")
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String response = "";
			
			System.out.println(exchange.getRequestMethod());
			
			if ("GET".equals(exchange.getRequestMethod())) {			
				response = startGET();
			}
			else if ("POST".equals(exchange.getRequestMethod())) {
				Map<String, Object> params = (Map<String, Object>) exchange.getAttribute("parameters");
				String type = (String) params.get("action_type");
				if(type.equals("create")){
					response = startPOST(exchange);
				}
				else if(type.equals("delete")){
					response = startDELETE((String) params.get("id"));
				}
			}
			exchange.getResponseHeaders().add("Content-type", "text/html; charset=UTF-8");
			exchange.sendResponseHeaders(200, 0);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes(Charset.forName("UTF-8")));
			os.close();

		}

		private String startDELETE(String id) throws IOException {
			try {
				bank.closeAccount(id);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			return startGET();
		}

		private String startPOST(HttpExchange exchange) throws IOException {
			System.out.println("funktioniert POST!!!!");

			StringBuilder buf = new StringBuilder();

			Map<String, Object> params = (Map<String, Object>) exchange.getAttribute("parameters");
			String user = (String) params.get("user");
			double amount = Double.parseDouble((String) params.get("amount"));

			String acc = bank.createAccount(user);
			try {
				bank.getAccount(acc).deposit(amount);
			} catch (IllegalArgumentException | InactiveException e) {
				e.printStackTrace();
			}			
			buf.append("<script>window.location='/startbank'</script>");
			return buf.toString();
		}

		private String startGET() throws IOException {
			String response;
			StringBuilder buf = new StringBuilder();
			buf.append("<HTML><BODY><H1>Welcome To CS</H1>");
			
			//TODO Tabelle erstellen mit allen Users
			buf.append("<TABLE>");
			buf.append("<table border=2>");
			HashSet<String> accountNumbers = (HashSet<String>) bank.getAccountNumbers();
			for(String name: accountNumbers) {
				buf.append(String.format("<tr><td width=300>"
						+ "<a href='/getuser?id="+ bank.getAccount(name).getNumber() + "'>" + bank.getAccount(name).getNumber() + "</a>"
						+ "</td><td width=300>%s</td><td width=100 align=right>%20.2f</td></tr>", bank.getAccount(name).getOwner(), bank.getAccount(name).getBalance()));
			}
			buf.append("</table>");
			buf.append("<form action='/createuser' method='GET'>");
			buf.append("<input name='ok' type=submit value='Create Account'>");
			buf.append("</form>");
//				buf.append("<TABLE>");
//				buf.append("<div id='diaaccountNrl' title='accNumers'>");
//				buf.append("<select>");
//
//				buf.append("<form action='/startbank' method='POST'>");
//				buf.append("<select property='accNumbers'>");
//				HashSet<String> accountNumbers = (HashSet<String>) bank.getAccountNumbers();
//				for (String acc : accountNumbers) {
//					buf.append(String.format("<option value='%s'>%s</option>", acc, acc));
//				}
//				buf.append("</select></div>");

//				buf.append("</TABLE>");
//				buf.append("<p>");
//
//				buf.append("<form name='deposit' action='/deposit' method='GET'>");
//				buf.append("<input name='deposit' type=submit value='Deposit'>");
//				buf.append("</form>");
//				buf.append("<form name='withdraw' action='/withdraw' method='GET'>");
//				buf.append("<input name='withdraw' type=submit value='Withdraw'>");
//				buf.append("</form>");
//				buf.append("<form name='transfer' action='/transfer' method='GET'>");
//				buf.append("<input name='transfer' type=submit value='Transfer'>");
//				buf.append("</form>");
//				buf.append("<p>");
//
//				buf.append("<form name='create' action='/createuser' method='GET'>");
//				buf.append("<input name='createaccount' type=submit value='Create Account'>");
//				buf.append("</form>");
//				buf.append("<form name='delete' action='/deleteuser' method='GET'>");
//				buf.append("<input name='deleteaccount' type=submit value='Delete Account'>");
//				buf.append("</form>");
//				buf.append("<p>");

			buf.append("</body></html>");
			response = buf.toString();
			return response;
		}
		
	}
	static class GetUserHandler implements HttpHandler{
		@SuppressWarnings("unchecked")
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			
			String response = "";
			StringBuilder buf = new StringBuilder();
			Map<String, Object> params = (Map<String, Object>) exchange.getAttribute("parameters");
			String accNumer = (String) params.get("id");
			System.out.println("GetUserhandler " + accNumer);
			
			buf.append("<HTML><BODY><H1>User Infos</H1>");
			
			buf.append("<table>");
			buf.append(String.format("<TR><TD>ID: %s</TD></TR>", bank.getAccount(accNumer).getNumber()));
			buf.append(String.format("<TR><TD>Owner: %s</TD></TR>", bank.getAccount(accNumer).getOwner()));
			buf.append(String.format("<TR><TD>Balance: %20.2f</TD></TR>", bank.getAccount(accNumer).getBalance()));
			buf.append("</table>");
			
			buf.append("<p>");
			
//			buf.append("<form name='register' action='/deleteuser/?id='" + bank.getAccount(accNumer).getNumber() + "method=GET>");
//			buf.append("<input name='submit' type=submit value='Delete Account'>");
			buf.append("<a href='/deleteuser/?id="+ accNumer + "'>" + "Delete Account" + "</a>");
			buf.append("</form>");
			buf.append("</body></html>");
			
			response = buf.toString();
			
			exchange.getResponseHeaders().add("Content-type", "text/html; charset=UTF-8");
			exchange.sendResponseHeaders(200, 0);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes(Charset.forName("UTF-8")));
			os.close();
		}		
	}

	static class CreateUserHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String response = "";
			StringBuilder buf = new StringBuilder();
			
			buf.append("<HTML><BODY><H1>Create Account</H1>");
			buf.append("<form name='register' action='/startbank\' method=POST>");
			
			buf.append("<TABLE>");
			buf.append("<TR><TD>Name:</TD> <TD><input size=40 maxlength=40 name='user'></TD></TR>");
			buf.append("<TR><TD>Amount:</TD><TD><input size=40 maxlength=40 type='number' step=0.01 name='amount'></TD></TR>");
			buf.append("</TABLE>");
			
			buf.append("<p>");
			buf.append("<input type='hidden' name='action_type' value='create'>");

			buf.append("<input name='submit' type=submit value='Absenden'>");
			buf.append("</form>");
			buf.append("<form name='register' action='/startbank\' method=GET>");
			buf.append("<input href='/startbank' name='cancel' type=submit value='Cancel'>");
			buf.append("</form>");
			
			//buf.append("<a href='/startbank' > Zurück</a>");
			buf.append("</body></html>");
			response = buf.toString();

			exchange.getResponseHeaders().add("Content-type", "text/html; charset=UTF-8");
			exchange.sendResponseHeaders(200, 0);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes(Charset.forName("UTF-8")));
			os.close();
		}
	}
	
	

	public String handle(String cmd) throws IOException {
		String input = cmd;

		while (input != null && !input.equals("")) {
			System.out.println("Server input: " + input);
			String[] inputs = input.split("/");
			switch ((inputs[0]).toLowerCase()) {
			case "createacc":
				String accNumber = bank.createAccount(inputs[1]);
				return accNumber;

			case "close":
				boolean closedOpen = bank.closeAccount(inputs[1]);
				return Boolean.toString(closedOpen);

			case "transfer":
				try {
					bank.transfer(bank.getAccount(inputs[1]), bank.getAccount(inputs[2]),
							Double.parseDouble(inputs[3]));
					return "ok";
				} catch (NumberFormatException e2) {
					return "NumberFormatException";

				} catch (IllegalArgumentException e2) {
					return "IllegalArgumentException";

				} catch (OverdrawException e2) {
					return "OverdrawException";

				} catch (InactiveException e2) {
					return "InactiveException";
				}

			case "getacc":
				if (inputs.length == 1) {
					return "null";

				}
				Account acc = (Account) bank.getAccount(inputs[1]);
				if (acc != null) {
					return (acc.getNumber() + "/" + acc.getBalance() + "/" + acc.getOwner() + "/" + acc.isActive());

				} else {
					return ("null");
				}

			case "getaccountnumbers":
				Set<String> accNum = bank.getAccountNumbers();
				if (accNum.size() == 0) {
					return "null";

				}
				String allAccountNumber = "";
				for (String acc1 : accNum) {
					allAccountNumber += acc1 + "/";
				}
				return allAccountNumber;

			case "getbalance":
				Account accBalance = (Account) bank.getAccount(inputs[1]);
				if (accBalance == null)
					return "null";
				double balance = accBalance.getBalance();
				return Double.toString(balance);

			case "getowner":
				Account accOwner = (Account) bank.getAccount(inputs[1]);
				if (accOwner == null)
					return "null";
				String owner = accOwner.getOwner();
				return owner;

			case "isactive":
				Boolean active = bank.getAccount(inputs[1]).isActive();
				return (Boolean.toString(active));

			case "deposit":
				try {
					Account accDeposit = (Account) bank.getAccount(inputs[1]);
					if (accDeposit == null) {
						return "InactiveException";

					}
					accDeposit.deposit(Double.parseDouble(inputs[2]));
					return ("ok");

				} catch (NumberFormatException e1) {
					return ("NumberFormatException");

				} catch (IllegalArgumentException e1) {
					return ("IllegalArgumentException");

				} catch (InactiveException e1) {
					return ("InactiveException");

				}

			case "withdraw":
				try {
					bank.getAccount(inputs[1]).withdraw(Double.parseDouble(inputs[2]));
					return ("ok");

				} catch (NumberFormatException e) {
					return ("NumberFormatException");

				} catch (IllegalArgumentException e) {
					return "IllegalArgumentException";

				} catch (OverdrawException e) {
					return ("OverdrawException");

				} catch (InactiveException e) {
					return ("InactiveException");

				}

			default:
				throw new IllegalArgumentException();
			}
		}
		return "nothing........";

	}

	static class ParameterParser extends Filter {

		@Override
		public String description() {
			return "Parses the requested URI for parameters";
		}

		@Override
		public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
			parseGetParameters(exchange);
			parsePostParameters(exchange);
			parseDeleteParameters(exchange);

			chain.doFilter(exchange);
		}

		private void parseGetParameters(HttpExchange exchange) throws UnsupportedEncodingException {
			Map<String, Object> parameters = new HashMap<>();
			URI requestedUri = exchange.getRequestURI();
			String query = requestedUri.getRawQuery();
			parseQuery(query, parameters);
			exchange.setAttribute("parameters", parameters);
		}

		private void parsePostParameters(HttpExchange exchange) throws IOException {
			if ("post".equalsIgnoreCase(exchange.getRequestMethod())) {
				@SuppressWarnings("unchecked")
				Map<String, Object> parameters = (Map<String, Object>) exchange.getAttribute("parameters");
				InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
				BufferedReader br = new BufferedReader(isr);
				String query = br.readLine();
				parseQuery(query, parameters);
			}
		}
		
		private void parseDeleteParameters(HttpExchange exchange) throws IOException {
			if ("delete".equalsIgnoreCase(exchange.getRequestMethod())) {
				@SuppressWarnings("unchecked")
				Map<String, Object> parameters = (Map<String, Object>) exchange.getAttribute("parameters");
				InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
				BufferedReader br = new BufferedReader(isr);
				String query = br.readLine();
				parseQuery(query, parameters);
			}
		}

		@SuppressWarnings("unchecked")
		public static void parseQuery(String query, Map<String, Object> parameters)
				throws UnsupportedEncodingException {
			if (query != null) {
				StringTokenizer st = new StringTokenizer(query, "&");
				while (st.hasMoreTokens()) {
					String keyValue = st.nextToken();
					StringTokenizer st2 = new StringTokenizer(keyValue, "=");
					String key = null;
					String value = "";
					if (st2.hasMoreTokens()) {
						key = st2.nextToken();
						key = URLDecoder.decode(key, System.getProperty("file.encoding"));
					}

					if (st2.hasMoreTokens()) {
						value = st2.nextToken();
						value = URLDecoder.decode(value, System.getProperty("file.encoding"));
					}

					if (parameters.containsKey(key)) {
						Object o = parameters.get(key);
						if (o instanceof List) {
							List<String> values = (List<String>) o;
							values.add(value);
						} else if (o instanceof String) {
							List<String> values = new ArrayList<String>();
							values.add((String) o);
							values.add(value);
							parameters.put(key, values);
						}
					} else {
						parameters.put(key, value);
					}
				}
			}
		}
	}
}
