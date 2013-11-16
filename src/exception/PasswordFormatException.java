package exception;

public class PasswordFormatException extends Exception{
	
	public String toString(){
		return "Number of bytes in password string is incorrect";
	}
	
}
