
package bank.soap.client.jaxws;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebFault(name = "OverdrawException", targetNamespace = "http://server.soap.bank/")
public class OverdrawException_Exception
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private OverdrawException faultInfo;

    /**
     * 
     * @param faultInfo
     * @param message
     */
    public OverdrawException_Exception(String message, OverdrawException faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param faultInfo
     * @param cause
     * @param message
     */
    public OverdrawException_Exception(String message, OverdrawException faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: bank.soap.client.jaxws.OverdrawException
     */
    public OverdrawException getFaultInfo() {
        return faultInfo;
    }

}
