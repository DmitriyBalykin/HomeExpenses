package Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import core.DataAccess;
import core.DataTuple;
import core.HomeEXConfig;
import exception.PasswordFormatException;

public class ServiceTools {

	private static void fileToCSV(){
		
		File fileRead = new File("data.txt");
		File fileWrite = new File("homeexData.txt");
		long counter = 0;
		
		try {
			FileReader reader = new FileReader(fileRead);
			FileWriter writer = new FileWriter(fileWrite, true);
			BufferedReader buffReader = new BufferedReader(reader);
			BufferedWriter buffWriter = new BufferedWriter(writer);
			
			String s = "";
			String[] line;
			StringBuilder sb = null;
			buffWriter.append("Date, type, name, number, cost, totCost\n");
			while((s = buffReader.readLine()) != null){
				line = s.split(", ");
				sb = new StringBuilder();
				for(int i = 0; i < line.length; i++){
					sb.append(line[i].substring(line[i].indexOf(": ")+2)).append(",");

				}
				s = sb.toString();
				buffWriter.append(counter + ",").append(s).append("\n");
				counter++;
			}
			buffWriter.flush();
			buffWriter.close();
			writer.close();
			
			buffReader.close();
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void csvToDB() throws PasswordFormatException{
		HomeEXConfig conf = new HomeEXConfig("localhost", "3306", "netaccess", "paraplan", "homeex", "EX", true);
		DataAccess da = new DataAccess(conf);
		File fileRead = new File("homeexData.txt");
		FileReader reader;
		try {
			reader = new FileReader(fileRead);
			BufferedReader buffReader = new BufferedReader(reader);
			String s = "";
			buffReader.readLine();
			while((s = buffReader.readLine()) != null){
				String[] line = s.split(",");
				DataTuple dt = new DataTuple(
						new Integer(line[0]),
						new Integer(line[1]),
						line[2],
						line[3],
						new Float(line[4]),
						new Float(line[5]),
						new Float(line[6]));
				da.addDataTuple(dt);
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public static void main(String[] args) throws PasswordFormatException {
		csvToDB();
	}
}
