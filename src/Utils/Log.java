package Utils;

import java.io.PrintStream;

public class Log {
	
	public Log(){
	}
	
	public static void log(String message){
		print(message, System.out);
	}
	
	public static void logError(String message){
		print(message, System.err);
	}
	
	private static void print(String message, PrintStream out){
		StackTraceElement logPoint = Thread.currentThread().getStackTrace()[3];
		String callerClassName = logPoint.getClassName();
		String callerMethodName = logPoint.getMethodName();

		out.println("["+callerClassName +" : "+callerMethodName +"()] >> "+message);
	}
}
