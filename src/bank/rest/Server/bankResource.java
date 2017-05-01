package bank.rest.Server;

import java.io.IOException;
import java.util.HashSet;

import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import bank.Bank;
import bank.InactiveException;
import bank.OverdrawException;
import bank.local.Driver;

@Singleton
@Path("/bank")
public class bankResource {

	private Driver localDriver =new Driver();
	private Bank bank = localDriver.getBankSoap();

	public bankResource() {
		System.out.println("BankResource() called");
	}
	
	@GET
	@Path("/accounts")
	@Produces("text/plain")
	public String getAccountNumbers() throws IOException {
		HashSet<String> accountNumbers = new HashSet<>();	// XXX es macht keinen Sinn hier ein HashSet zu erzeugen (mit new), denn die Referenz wird mit der 
		accountNumbers = (HashSet<String>) bank.getAccountNumbers();	// nächsten Anweisung überschrieben. Und als Typ könnten Sie Set<String> verwenden.
		String accN = "";
		if (accountNumbers.size() == 0) {
			accN = "null";
		}
		for (String acc1 : accountNumbers) {
			accN += acc1 + "/";	// XXX funktioniert solange die Kontonummern kein "/" enthalten.
		}
		return accN;
	}
	
	@POST
	@Path("/accounts")
	@Produces("text/plain")
	public String createAccount(@FormParam("owner") String owner) throws IOException {
		String accN = bank.createAccount(owner);
		return  accN;	// XXX ich frage mich gerade, ob null richtig übertagen wird, d.h. falls das Konto (aus welchen Gründen auch immer) nicht erzeugt werden konnte.
						//     => habe es ausprobiert: Auf Klientenseite wird in diesem Fall "" als Resultatstring ausgelesen.
	}
	
	@DELETE
	@Path("/accounts/{number}")
	@Produces("text/plain")
	public String closeAccount(@PathParam("number") String number) throws IOException {
		String action = Boolean.toString(bank.closeAccount(number));
		return action;// XXX ich meinte, dass die Konversion von boolean nach String automatisch gemacht wrid, d.h. das Boolean.toString ist nicht falsch aber auch nicht nötig.
						//    => ja, habe ich eben ausprobiert. Die Signatur der Methode muss dann natürlich in boolean closeAccount(...) geändert werden.
	}
	
	@GET
	@Path("/accounts/{number}")
	@Produces("text/plain")
	public String getAccount(@PathParam("number") String number) throws IOException {
		bank.Account acc = bank.getAccount(number);
		if (acc != null) {
			return acc.getNumber() + "/" + acc.getBalance() + "/" + acc.getOwner() + "/"
					+ acc.isActive();
		} else {
			return "null";	// XXX hier wäre ein Fehler 404 angezeigt.
		}
	}
	
	@PUT	// XXX wenn schon: POST
	@Path("/accounts/{from}/{to}/{amount}")
	@Produces("text/plain")
	public String transfer(@PathParam("from") String from, @PathParam("to") String to, @PathParam("amount") double amount){
		try {
			bank.transfer(bank.getAccount(from), bank.getAccount(to), amount);		} catch (InactiveException e) {
			return "-1";
		} catch(OverdrawException e){
			return "-2";
		}catch (IllegalArgumentException e) {
			return "-3";
		} catch (IOException e) {
			return "-4";
		}
		return "0";
	}
	
	@GET
	@Path("/accounts/getBalance/{number}")
	@Produces("text/plain")
	
//	XXX nicht nötig, denn mit einem GET auf
//	@GET
//	@Path("/accounts/{number}")
//	erhält man bereits alle Daten.
	public String getBalance(@PathParam("number") String number) throws IOException {
		String balance = Double.toString(bank.getAccount(number).getBalance());
		return balance;
	}
	
	@GET
	@Path("/accounts/getOwner/{number}") // XXX ditto
	@Produces("text/plain")
	public String getOwner(@PathParam("number") String number) throws IOException {
		return bank.getAccount(number).getOwner();
	}
	
	@GET
	@Path("/accounts/isActive/{number}")
	@Produces("text/plain")
	public String isActive(@PathParam("number") String number) throws IOException {
		String active = Boolean.toString(bank.getAccount(number).isActive());
		System.out.println("isActive " + active);
		return active;
	}

	@PUT
	@Path("/accounts/{number}/deposit/{amount}")	// XXX wenn schon: POST
	@Produces("text/plain")
	public String deposit(@PathParam("number") String number, @PathParam("amount") double amount) {
		try {
			bank.getAccount(number).deposit(amount);
		} catch (InactiveException e) {
			return "-1";
		} catch (IllegalArgumentException e) {
			return "-2";
		} catch (IOException e) {
			return "-3";
		}
		return "0";
	}
	
	@PUT
	@Path("/accounts/{number}/withdraw/{amount}")
	@Produces("text/plain")
	public String withdraw(@PathParam("number") String number, @PathParam("amount") double amount) {
		try {
			bank.getAccount(number).withdraw(amount);
		} catch (InactiveException e) {
			return "-1";
		} catch(OverdrawException e){
			return "-2";
		}catch (IllegalArgumentException e) {
			return "-3";
		} catch (IOException e) {
			return "-4";
		}
		return "0";
	}
	
}
