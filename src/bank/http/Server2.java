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
import com.sun.net.httpserver.Filter.Chain;

import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;
import bank.http.DriverServer.Account;

public class Server2 {

	private static Bank bank;

	public static void main(String args[]) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(1234), 0);
		DriverServer driver = new DriverServer(); // Object von Local-Logic
		driver.connect(args); // nur damit es initalisiert wird
		bank = driver.getBank();
		server.createContext("/startbank", new BankHandler()).getFilters().add(new ParameterParser());
		server.start();
		String nr = bank.createAccount("anhi");
		try {
			bank.getAccount(nr).deposit(20000);
		} catch (IllegalArgumentException | InactiveException e) {
			e.printStackTrace();
		}
		String nr2 = bank.createAccount("dog");
		try {
			bank.getAccount(nr2).deposit(0);
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
				Map<String, Object> params = (Map<String, Object>) exchange.getAttribute("parameters");
				String cmd = (String)params.get("cmd");
				String parameter1 = (String)params.get("parameter1");
				String parameter2 = (String)params.get("parameter2");
				String parameter3 = (String)params.get("parameter3");

				if(params.size()==0 || cmd.equals("showAll")){
					response = allUsersPage();
				}else if (cmd.equals("createAcc")){
					response = createUsersPage();
				}else if(cmd.equals("created")){
					String accNum = handle("createacc" + "/" + parameter1);
					handle("deposit" + "/" + accNum+ "/" + parameter2);					
					response = allUsersPage();
				}else if(cmd.equals("delete")){
					String closed = handle("close/" + parameter1);
					response = allUsersPage();
					if(!closed.equals("true")){
						response = errorPage("Can't close: user still has money");
					}
				}else if (cmd.equals("showuser")){
					response = showUserPage(parameter1);
				}else if(cmd.equals("deposit")){
					if(parameter1 == null || parameter2 == "" || parameter3 == null){
						response = errorPage("no parameter");
					}else{
					String desosit = handle("deposit/" + parameter1 + "/" + parameter2);
					if(desosit.equals("ok")) response = allUsersPage();
					if(!desosit.equals("ok")){
						response = errorPage(desosit);
					}}
				}else if (cmd.equals("withdraw")){
					if(parameter1 == null || parameter2 == ""){
						response = errorPage("no parameter");
					}else{
					String withdraw = handle("withdraw/" + parameter1 + "/" + parameter2);
					if(withdraw.equals("ok")) response = allUsersPage();
					if(!withdraw.equals("ok")){
						response = errorPage(withdraw);
					}}
				}else if (cmd.equals("transfer")){	
					if(parameter1 == null || parameter2 == "" || parameter3 == ""){
						response = errorPage("no parameter");
					}else{
					String transfer = handle(cmd + "/"+ parameter1 + "/" + parameter2 + "/" +parameter3);	
					if (!transfer.equals("ok")){
						response = errorPage(transfer);
					}
					else response = allUsersPage();
					}
				}
							
			exchange.getResponseHeaders().add("Content-type", "text/html; charset=UTF-8");
			exchange.sendResponseHeaders(200, 0);
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes(Charset.forName("UTF-8")));
			os.close();
		}


	public static String allUsersPage() throws IOException{
		String response;
		StringBuilder buf = new StringBuilder();
		buf.append("<HTML><BODY><H1>All Users</H1>");
		buf.append("<TABLE>");
		buf.append("<table border=2>");
		HashSet<String> accountNumbers = (HashSet<String>) bank.getAccountNumbers();
		for(String name: accountNumbers) {
			buf.append(String.format("<tr><td width=300>"
					+ "<a href=" + "/startbank?cmd=showuser&parameter1=" + bank.getAccount(name).getNumber() + '>' + bank.getAccount(name).getNumber() + "</a>"
					+ "</td><td width=300>%s</td><td width=100 align=right>%20.2f</td></tr>", bank.getAccount(name).getOwner(), bank.getAccount(name).getBalance()));
		}
		buf.append("</table>");
		buf.append("<form action='/startbank' method='GET'>");
		buf.append("<input name='cmd' value='createAcc' hidden>");
		buf.append("<input type=submit value='Create Account'>");
		buf.append("</form>");
		buf.append("</body></html>");
		response = buf.toString();
		return response;
	}

	public static String createUsersPage(){
			String response = "";
			StringBuilder buf = new StringBuilder();
			
			buf.append("<HTML><BODY><H1>Create User</H1>");
			buf.append("<form action='/startbank\' method=GET>");			
			buf.append("<TABLE>");
			buf.append("<input name='cmd' value='created' hidden>");
			buf.append("<TR><TD>Name:</TD> <TD><input size=40 maxlength=40 name='parameter1'></TD></TR>");
			buf.append("<TR><TD>Amount:</TD><TD><input size=40 maxlength=40 type='number' step=0.01 name='parameter2'></TD></TR>");
			buf.append("</TABLE>");		
			buf.append("<p>");
			buf.append("<input name='created' type=submit value='Absenden'>");
			buf.append("</form>");
			buf.append("<button onclick='javascript:history.back()'>back</a>");		
			buf.append("</body></html>");
			response = buf.toString();
			return response;
	}
	
	public static String showUserPage(String id) throws IOException{
		String response = "";
		StringBuilder buf = new StringBuilder();
		System.out.println("showUserPage: " + id);
		buf.append("<HTML><BODY><H1>User Infos</H1>");
		
		buf.append("<table>");
		buf.append(String.format("<TR><TD><b>ID:</b> %s</TD></TR>", bank.getAccount(id).getNumber()));
		buf.append(String.format("<TR><TD><b>Owner:</b> %s</TD></TR>", bank.getAccount(id).getOwner()));
		buf.append(String.format("<TR><TD><b>Balance:</b> %20.2f</TD></TR>", bank.getAccount(id).getBalance()));
		buf.append("</table><br/>");
		
		
		buf.append("<p><b>Deposit<b><br/>");
		// Deposit
		buf.append("<form action='/startbank\' method=GET>");
		buf.append("<TABLE>");
		buf.append("<input name='cmd' value='deposit' hidden>");
		buf.append("<input name='parameter1'" + "value=" + "'" +id+"'" + "hidden>");	
		buf.append("<TR><TD><b>Amount:</b></TD><TD><input size=40 maxlength=40 type='number' step=0.01 name='parameter2'></TD></TR>");
		buf.append("</TABLE>");	
		buf.append("<input name='deposit' type=submit value='Absenden'>");
		buf.append("</form><br/>");
		
		buf.append("<p><b>Withdraw<b><br/>");
		// Withdraw
		buf.append("<form action='/startbank\' method=GET>");
		buf.append("<TABLE>");
		buf.append("<input name='cmd' value='withdraw' hidden>");
		buf.append("<input name='parameter1'" + "value=" + "'" +id+"'" + "hidden>");	
		buf.append("<TR><TD><b>Withdraw:</b></TD><TD><input size=40 maxlength=40 type='number' step=0.01 name='parameter2'></TD></TR>");
		buf.append("</TABLE>");	
		buf.append("<input name='withdraw' type=submit value='Withdraw'>");
		buf.append("</form><br/>");
		
		buf.append("<p><b>Transfer<b><br/>");
		
		// transfer
		buf.append("<form action='/startbank\' method=GET>");
		buf.append("<input name='cmd' value='transfer' hidden>");
		buf.append("<input name='parameter1'" + "value='" + id + "' hidden>");	
		buf.append("<select name='parameter2'>");
		for(String accNum: bank.getAccountNumbers()){
			buf.append("<option value='" + accNum + "' >" + accNum +": " + bank.getAccount(accNum).getOwner() + "</option>");
		}
		buf.append("</select>");
		buf.append("<input name='parameter3'>");	
		buf.append("<input type='submit'>");
		buf.append("</form><br/>");
		
		//Delete Account
		buf.append("<form action='/startbank' method='GET'>");
		buf.append("<input name='cmd' value='delete' hidden>");
		buf.append("<input name='parameter1'" + "value=" + "'" +id+"'" + "hidden>");	
		buf.append("<input type=submit value='Delete Account'>");
		buf.append("</form>");
	
		buf.append("<button onclick='javascript:history.back()'>back</a>");
		buf.append("</body></html>");
		
		response = buf.toString();
		return response;
	}
	
	public static String handle(String cmd) throws IOException {
		String input = cmd;

		while (input != null && !input.equals("")) {
			System.out.println("HANDLE: Server input: " + input);
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
		return errorPage("Error Argument");

	}

	public static String errorPage(String error){
		String response;
		StringBuilder buf = new StringBuilder();
		buf.append("<HTML><BODY><H1>" + error + "</H1>");
		buf.append("<button onclick='javascript:history.back()'>back</a>");
		buf.append("</body></html>");
		response = buf.toString();
		return response;
	}

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
