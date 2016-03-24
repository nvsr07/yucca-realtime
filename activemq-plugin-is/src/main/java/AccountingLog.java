public class AccountingLog {

	private String protocol="-";
	private String connectionId="-";
	private String uniqueid="-";
	private String tenantcode="-";
	private String destination="-"; // output......
	private String operation="-"; // CONNECT, POST, SEND, ADD CONSUMER...

	private String ipOrigin;
	private long elapsed=-1;
	private String errore="-";
	private int numeroEventi; // si recupera da esbin
	private String sensorStream="-" ;// si recupera da esbin
	


	
	public AccountingLog() {
		
	}
	

	public String toString() {
		String logAccountingMessage="";

		//id
		logAccountingMessage=logAccountingMessage+"\""+uniqueid.replace("\"", "\"\"")+"\"";
		//forwardedfor
		logAccountingMessage=logAccountingMessage+",\""+connectionId.replace("\"", "\"\"")+"\"";
		//jwt
		logAccountingMessage=logAccountingMessage+",\""+protocol.replace("\"", "\"\"")+"\"";
		
		
		//path
		logAccountingMessage=logAccountingMessage+",\""+tenantcode.replace("\"", "\"\"")+"\"";
		//apicode
		logAccountingMessage=logAccountingMessage+",\""+destination.replace("\"", "\"\"")+"\"";

		//datasetCode
		logAccountingMessage=logAccountingMessage+",\""+operation.replace("\"", "\"\"")+"\"";

		//tenantCode
		logAccountingMessage=logAccountingMessage+",\""+ipOrigin.replace("\"", "\"\"")+"\"";
		
		
		
		//querString
		logAccountingMessage=logAccountingMessage+",\""+sensorStream.replace("\"", "\"\"")+"\"";
		

		//dataIn
		logAccountingMessage=logAccountingMessage+","+numeroEventi;
		//elapsed
		logAccountingMessage=logAccountingMessage+","+elapsed;
		
		
		
		//error
		logAccountingMessage=logAccountingMessage+",\""+errore+"\"";
		
		return logAccountingMessage;
	}
	
}
