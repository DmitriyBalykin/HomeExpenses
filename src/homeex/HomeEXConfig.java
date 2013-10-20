package homeex;

public class HomeEXConfig {
	public String hostAddr, hostPort, userName, dbName, tableName, userPass;
	
	private String key = "Encryptbythissequence";
	
	public HomeEXConfig(
			String hostAddr,
			String hostPort,
			String userName,
			String userPass,
			String dbName,
			String tableName,
			boolean needToEncode) throws PasswordFormatException{
		
		this.hostAddr = hostAddr;
		this.hostPort = hostPort;
		this.userName = userName;
		this.userPass = needToEncode ? encode(userPass, key) : userPass;
		this.dbName = dbName;
		this.tableName = tableName;
	}
	public HomeEXConfig() throws PasswordFormatException{
		this("localhost", "3306", "", "",	"homeex", "EX", false);
	}

	public String getUserPass() {
		return (encode(userPass, key));
	}

	private static String encode(String encryptThis, String key){

		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < encryptThis.length(); i++){
			sb.append((char) (encryptThis.charAt(i) ^ key.charAt(i % key.length())));
		}
		return sb.toString();
	}
	
	
	public String toString(){
		return hostAddr+", "+hostPort+", "+userName+", "+getUserPass()+", "+userPass+", "+dbName+", "+tableName;
	}
}
