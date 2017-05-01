package bank.rest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;
import bank.local.Driver;

@Singleton
@Path("/bank")
public class BankResourceHTML {

	static Driver driver = new Driver();
	static{
		driver.connect(new String[]{"start"});
	}
	Bank bank = driver.getBank();
	
	@GET
	@Path("accounts")
	public String getAllAccounts( @Context Request r) throws IOException {
		StringBuffer buf = new StringBuffer();
		buf.append("<html><body><h1>Bank</h1>" + "<h3>All Accounts</h3>" + "<br>");
		
		if(!bank.getAccountNumbers().isEmpty()){
		buf.append("<TABLE>");
		buf.append("<table border=2>");
		HashSet<String> accountNumbers = (HashSet<String>) bank.getAccountNumbers();
		for (String name : accountNumbers) {
			buf.append(String.format(
					"<tr><td width=300>" + "<a href=" + "/bank/showUser/"
							+ bank.getAccount(name).getNumber() + '>' + bank.getAccount(name).getNumber() + "</a>"					
							+ "</td><td width=300>%s</td><td width=100 align=right>%20.2f</td></tr>",
					bank.getAccount(name).getOwner(), bank.getAccount(name).getBalance()));
		}
		buf.append("</table>");
		}else{
			buf.append("<p> Keine Accounts </p>");
		}			
		
		buf.append("<form action='/bank/createaccount' >");
	    buf.append("<input type='submit' value='Create Account' />");
	    buf.append("</form>");
		buf.append("</body></html>");
		return buf.toString();
	}
	
	@GET
	@Path("/createaccount")
	public String createAccounts() throws IOException {
		StringBuffer buf = new StringBuffer();
		buf.append("<html><body><h1>Bank</h1>" + "<h3> Create Account </h3>" + "<br>");
		String response = "";

		buf.append("<form action='/bank/accounts\' method=POST>");
		buf.append("<TABLE>");
		buf.append("<TR><TD>Name:</TD> <TD><input size=40 maxlength=40 type='text' name = 'name' ></TD></TR>");
		buf.append("<TR><TD>Amount:</TD><TD><input size=40 maxlength=40 type='number' step=0.01 name='money' ></TD></TR>");
		buf.append("</TABLE>");
		buf.append("<p>");
		buf.append("<input name='created' type=submit value='Absenden'>");
		buf.append("</form>");
		buf.append("<button onclick='javascript:history.back()'>back</a>");
		buf.append("</body></html>");
		response = buf.toString();
		return response;
	}

	@POST
	@Path("accounts")
	public Response createAccounts2(@FormParam("name") String name, @FormParam("money") int money) throws IOException, IllegalArgumentException, InactiveException, URISyntaxException {
		String accNumber = bank.createAccount(name);
		bank.getAccount(accNumber).deposit(money);
		URI targetURIForRedirection = new URI("http://localhost:9999/bank/accounts");
//	     Response.temporaryRedirect(targetURIForRedirection).build();
	    return Response.seeOther(targetURIForRedirection).build();
	}
	
	@GET
	@Path("/showUser/{id}")
	public String showUser(@PathParam("id") String id) throws IOException {
		String response = "";
		StringBuilder buf = new StringBuilder();
		buf.append("<HTML><BODY><H1>Bank</H1>");

		buf.append("<table>");
		buf.append(String.format("<TR><TD><b>ID:</b> %s</TD></TR>", bank.getAccount(id).getNumber()));
		buf.append(String.format("<TR><TD><b>Owner:</b> %s</TD></TR>", bank.getAccount(id).getOwner()));
		buf.append(String.format("<TR><TD><b>Balance:</b> %20.2f</TD></TR>", bank.getAccount(id).getBalance()));
		buf.append("</table><br/>");

		// Deposit
		buf.append("<p><b>Deposit<b><br/>");
		buf.append("<form action='/bank/deposit\' method=POST>");
		buf.append("<TABLE>");
		buf.append("<TR><TD><b>Deposit:</b></TD><TD><input size=40 maxlength=40 type='number' step=0.01 name='deposit'></TD></TR>");
		buf.append("<input name='id'" + "value=" + "'" + id + "'" + "hidden>");
		buf.append("</TABLE>");
		buf.append("<input name='deposit' type=submit value='Absenden'>");
		buf.append("</form><br/>");

		// Withdraw
		buf.append("<p><b>Withdraw<b><br/>");
		buf.append("<form action='/bank/withdraw\' method=POST>");
		buf.append("<TABLE>");
		buf.append("<TR><TD><b>Withdraw:</b></TD><TD><input size=40 maxlength=40 type='number' step=0.01 name='withdraw'></TD></TR>");
		buf.append("<input name='id'" + "value=" + "'" + id + "'" + "hidden>");
		buf.append("</TABLE>");
		buf.append("<input name='withdraw' type=submit value='Withdraw'>");
		buf.append("</form><br/>");

		// transfer
		buf.append("<p><b>Transfer<b><br/>");
		buf.append("<form action='/bank\' method=POST>");
		buf.append("<TR><TD><b>Withdraw:</b></TD><TD><input size=40 maxlength=40 type='number' step=0.01 name='withdraw'></TD></TR>");
		buf.append("<input name='parameter1'" + "value='" + id + "' hidden>");
		buf.append("<select name='parameter2'>");
		for (String accNum : bank.getAccountNumbers()) {
			buf.append("<option value='" + accNum + "' >" + accNum + ": " + bank.getAccount(accNum).getOwner()
					+ "</option>");
		}
		buf.append("</select>");
		buf.append("<input name='parameter3'>");
		buf.append("<input type='submit'>");
		buf.append("</form><br/>");

		// Delete Account
		buf.append("<form action='/bank/deleteUser\' method='DELETE'>");
		buf.append("<input type='hidden' name='_method' value='DELETE' >");
		buf.append("<input name='id'" + "value=" + "'" + id + "'" + "hidden>");
		buf.append("<input type=submit value='Delete Account'>");
		buf.append("</form>");

		buf.append("<button onclick='javascript:history.back()'>back</a>");
		buf.append("</body></html>");

		response = buf.toString();
		return response;
	}
	
	@POST
	@Path("deposit")
	public Response deposit(@FormParam("id") String id, @FormParam("deposit") int deposit) throws IOException, IllegalArgumentException, InactiveException, URISyntaxException {
		bank.getAccount(id).deposit(deposit);
		URI targetURIForRedirection = new URI("bank/accounts");
	    //return Response.temporaryRedirect(targetURIForRedirection).build();
	    return Response.seeOther(targetURIForRedirection).build();
	}
	
	@POST
	@Path("withdraw")
	public Response withdraw(@FormParam("id") String id, @FormParam("withdraw") int withdraw) throws IOException, IllegalArgumentException, InactiveException, URISyntaxException, OverdrawException {
		bank.getAccount(id).withdraw(withdraw);
		URI targetURIForRedirection = new URI("http://localhost:9999/bank/accounts");
	    //return Response.temporaryRedirect(targetURIForRedirection).build();
	    return Response.seeOther(targetURIForRedirection).build();
	}
	
	@DELETE
	@Path("deleteUser/{id}")
	public Response delete(@PathParam("id") String id) throws IOException, IllegalArgumentException, InactiveException, URISyntaxException, OverdrawException {
		System.out.println("delete.....");
		bank.closeAccount(id);
		URI targetURIForRedirection = new URI("http://localhost:9999/bank/accounts");
	    //return Response.temporaryRedirect(targetURIForRedirection).build();
	    return Response.seeOther(targetURIForRedirection).build();
	}
	
	
	
	
}
