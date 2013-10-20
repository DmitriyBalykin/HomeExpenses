package homeex;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import com.mysql.jdbc.Connection;



public class DataAccess{
	private static long newId;
	private static Connection conn = null;
	public PropertyChangeSupport msgNode = new PropertyChangeSupport(this);
	public static final String confFile = "conf.dat",
								dataFileName = "homeexData.csv";
	HomeEXConfig config;
	boolean writeSuccess = false;
	public static String SEPARATOR = ";";
	
	public DataAccess(HomeEXConfig config){
		this.config = config;
	}
	public DataAccess(HomeEXConfig config, PropertyChangeListener listener){
		this(config);
		msgNode.addPropertyChangeListener(listener);
	}
	
	private Connection getConnection(){

		final Properties connInfo = new Properties();
		connInfo.put("user", config.userName);
		connInfo.put("password", config.getUserPass());

		connInfo.put("useUnicode","true"); // (1)
		connInfo.put("charSet", "UTF-8"); // (2)
		
		try {
			conn = (Connection) DriverManager.getConnection("jdbc:mysql://"+config.hostAddr+":"+config.hostPort+"/"+config.dbName,connInfo);
			System.out.println("Соединение успешно установлено");
			sendMessage("Соединение успешно установлено");
		} catch (SQLException e) {
			System.err.println("SQLException: "+e);
			System.err.println("SQLState: "+e.getSQLState());
			System.err.println("Vendor error: "+e.getErrorCode());
		}
		
		return conn;
		
	}

	public boolean addDataTuple(DataTuple dt){
		//saving to database
		dt.id = ++newId;
		boolean resDb = writeToDb(dt),
				resFile = writeToFile(dt);
		
		//saving to local file, true if one of writes succeeded
		return resDb || resFile;
	}
	private boolean writeToDb(DataTuple dt){
		String q = "INSERT INTO EX(ID, DATE, TYPE, NAME, NUMBER, ONECOST, TOTCOST) VALUES " +
				"('"+dt.id+"','"+dt.date+"','"+dt.prodType+"','"+dt.prodName+"','"+dt.prodNum+"','"+dt.prodCost+"','"+dt.prodTotCost+"');";
		sentQuery(q);
		
		return writeSuccess;	//writeSuccess sets true by sentQuery as result of null Result set but not 0 updateCount
	}
	private boolean writeToFile(BufferedWriter writer, DataTuple dt){
		//version assigned to multiple use to avoid reopen writer, needs to external flush and close operations
		try {
			writer.append(dt.toCSVString()).append("\n");
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			sendMessage("Ошибка записи файла");
			e.printStackTrace();
			return false;
		}
	}
	private boolean writeToFile(DataTuple dt){
		//version assigned to single use
		File file = new File(dataFileName);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
			writeToFile(writer, dt);
			writer.flush();
			writer.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			sendMessage("Ошибка записи файла");
			e.printStackTrace();
			return false;
		}
	}
	public List<String> getNamesList(){
		List<String> list = new ArrayList<String>();
		for(DataTuple dt:readFile()){
			list.add(dt.prodName);
		}

		Collections.sort(list);
		return list;
	}
	private void sendMessage(String msg){
		msgNode.firePropertyChange("TextReport", null, msg);
	}
	public boolean syncFileToDB(){
		Map<Long, String> fileMap = new HashMap<Long, String>();
		long syncRows = 0;
		boolean resErr = false;
		//set with id from file
		Set<Long> idFileSet = new TreeSet<Long>();
		for(DataTuple dt:readFile()){
			idFileSet.add(dt.id);
			fileMap.put(dt.id, dt.toCSVString());
		}

		//searching for the biggest exist id number
		newId = (Long) idFileSet.toArray()[idFileSet.size()-1];
		
		Connection conn = getConnection();

		if(conn == null) {
			sendMessage("Соединение с базой данных не установлено. Работаем с локальным файлом.");
			return false;
		}
		
		String query = "SELECT ID FROM EX;";
		//set with id from db
		ResultSet res = sentQuery(conn, query);
		Set<Long> idDBresSet = new TreeSet<Long>();
		try {
			while(res.next()){
				idDBresSet.add(new Long(res.getInt(1)));
			}
			resErr = true;
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			sendMessage("Ошибка соединения с базой данных");
			e1.printStackTrace();
		}
		
		
		//searching for the biggest exist id number
		long newId2 = 0;
		if(idDBresSet.size() > 0)
			newId2 = (Long) idDBresSet.toArray()[idDBresSet.size()-1];
		if(newId2 > newId) newId = newId2;

		//comparing sets from db and file
		Set<Long> absentInFile = new TreeSet<Long>(idDBresSet);
		Set<Long> absentInDb = new TreeSet<Long>(idFileSet);
		
		//subtraction file set from db set
		absentInFile.removeAll(idFileSet);
		//subtraction db set from file set
		absentInDb.removeAll(idDBresSet);
		
		syncRows = absentInFile.size() + absentInDb.size();
		//Synchronizing db to file
		try {
			BufferedWriter buff = new BufferedWriter(new FileWriter(new File(dataFileName), true));
			for(Long val:absentInFile){
				String query1 = "SELECT * FROM EX WHERE ID='"+Long.toString(val)+"';";
				res = sentQuery(conn, query1);
				res.next();
				writeToFile(buff, new DataTuple(res));
			}
			buff.flush();
			buff.close();
		} catch (IOException e) {
			resErr = false;
			e.printStackTrace();
		} catch (SQLException e) {
			resErr = false;
			e.printStackTrace();
		}
		
		//Synchronizing file to db
		
		try {
			for(Long val:absentInDb){
				resErr = writeToDb(new DataTuple(fileMap.get(val)));
				if(!resErr) break;
			}
		} catch (StringFormatException e) {
			resErr = false;
			e.printStackTrace();
		}
		//correct exit
		if(resErr)
			sendMessage("База данных доступна. "+syncRows+" записей синхронизировано");
		else
			sendMessage("Ошибка в целостности базы данных");
		return resErr;
	}
	//временн
	public List<DataTuple> getDataSet(Map<String, String> queryMap){
		List<DataTuple> dt = new ArrayList<DataTuple>();
		String eqString = " = '";
		int count = 0;
		DataTuple datatuple;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM EX WHERE ");
		
		for(String s:queryMap.keySet()){
			String val = queryMap.get(s);
			if(count > 0)
				sb.append(" AND ");
			if(val.contains("--")){
				int ind = val.indexOf("--");
				sb.append(s)
				.append(" >= '")
				.append(val.substring(0, ind))
				.append("' AND ")
				.append(s)
				.append(" <= '")
				.append(val.substring(ind + 2));
			}
			else if(val.contains("%"))
				sb.append(s).append(" LIKE '").append(val);	//for case of selecting with mask
			else
				sb.append(s).append(eqString).append(val);
			
			sb.append("'");
			
			count++;
		}
		sb.append(";");
		ResultSet rs = sentQuery(sb.toString());
		if(rs != null)
		try {
			while(rs.next()){
				//создаем новую строку
				datatuple = new DataTuple(rs);

				//добавляем строку в общий список
				dt.add(datatuple);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		else{
			dt = readFile(sb.toString());
		}
			
		return dt;
	}
	public void dbToFile(){
		ResultSet rs = sentQuery("SELECT * FROM EX");
		DataTuple datatuple;
		try {
			while(rs.next()){
				//создаем новую строку
				datatuple = new DataTuple(rs);

				//добавляем строку в общий список
				writeToFile(datatuple);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public ResultSet sentQuery(String query){
		System.out.println(query);
		
		if(conn != null)
			return sentQuery(conn, query);
		else
			return null;
	}
	public ResultSet sentQuery(Connection connGlob, String query){
		Statement stm = null;
		ResultSet rs = null;

		try {
			stm = connGlob.createStatement();
			stm.execute(query);
			rs = stm.getResultSet();
			if(stm.getUpdateCount() > 0) writeSuccess = true; //writeSuccess sets true by sentQuery as result of null Result set but not 0 updateCount
			else writeSuccess = false;
			System.out.println(rs);
			
		} catch (SQLException e) {
			e.printStackTrace();
			sendMessage("Problem with database consistency!!");
			return null;
		}
		return rs;
	}
	private List<DataTuple> readFile(String query){
		//this method selects from file rows, without projecting by columns 
		List<DataTuple> list = new ArrayList<DataTuple>();
		try {
			
			BufferedReader reader = new BufferedReader(new FileReader(new File(dataFileName)));
			String s = "";
			reader.readLine();
			while((s = reader.readLine()) != null){
				
				String arr[] = s.split(SEPARATOR);
				list.add(new DataTuple(new Long(arr[0]), new Integer(arr[1]), arr[2], arr[3], new Float(arr[4]), new Float(arr[5]), new Float(arr[6])));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			sendMessage("Ошибка чтения файла: файл не найден");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			sendMessage("Ошибка чтения файла");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			sendMessage("Ошибка чтения файла: неправильный формат чисел");
			e.printStackTrace();
		}
		
		//deleting records, which shouldn't be selected
		
		//DATE, TYPE, NAME, TOTCOST
		List<DataTuple> tempList = new ArrayList<DataTuple>();
		String searchString = "DATE >= '";
		if(query.contains(searchString)){
			int start = query.indexOf(searchString) + searchString.length();
			int val = new Integer(query.substring(start, query.indexOf("'", start)));			
			for(DataTuple dt:list){
				if(dt.date >= val)
					tempList.add(dt);
			}
			list = new ArrayList<DataTuple>(tempList);
			tempList.clear();
		}
		searchString = "DATE <= '";
		if(query.contains(searchString)){
			int start = query.indexOf(searchString) + searchString.length();
			int val = new Integer(query.substring(start, query.indexOf("'", start)));			
			for(DataTuple dt:list){
				if(dt.date <= val)
					tempList.add(dt);
			}
			list = new ArrayList<DataTuple>(tempList);
			tempList.clear();
		}
		searchString = "TYPE = '";
		if(query.contains(searchString)){
			int start = query.indexOf(searchString) + searchString.length();
			String val = query.substring(start, query.indexOf("'", start));			
			for(DataTuple dt:list){
				if(dt.prodType.equals(val))
					tempList.add(dt);
			}
			list = new ArrayList<DataTuple>(tempList);
			tempList.clear();
		}
		searchString = "NAME = '";
		if(query.contains(searchString)){
			int start = query.indexOf(searchString) + searchString.length();
			String val = query.substring(start, query.indexOf("'", start));
			
			for(DataTuple dt:list){
				if(dt.prodName.equals(val))
					tempList.add(dt);
			}
			list = new ArrayList<DataTuple>(tempList);
			tempList.clear();
		}
		searchString = "NAME LIKE '";
		if(query.contains(searchString)){
			int start = query.indexOf(searchString) + searchString.length();
			String val = query.substring(start, query.indexOf("'", start));
			
			//using regex for search
			if(val.contains("%"))
				val = val.replace("%", ".*");
			
			for(DataTuple dt:list){
				if(dt.prodName.matches(val))
					tempList.add(dt);
			}
			list = new ArrayList<DataTuple>(tempList);
			tempList.clear();
		}
		searchString = "TOTCOST >= '";
		if(query.contains(searchString)){
			int start = query.indexOf(searchString) + searchString.length();
			String val = query.substring(start, query.indexOf("'", start));
			Float f = new Float(val);

			for(DataTuple dt:list){
				if(dt.prodTotCost >= f)
					tempList.add(dt);
			}
			list = new ArrayList<DataTuple>(tempList);
			tempList.clear();
		}
		searchString = "TOTCOST <= '";
		if(query.contains(searchString)){
			int start = query.indexOf(searchString) + searchString.length();
			String val = query.substring(start, query.indexOf("'", start));
			Float f = new Float(val);	
			
			for(DataTuple dt:list){
				if(dt.prodTotCost <= f)
					tempList.add(dt);
			}
			list = new ArrayList<DataTuple>(tempList);
			tempList.clear();
		}
		return list;
	}
	public void deleteRows(List<Long> listToDelete){
		//mark deleted rows in database as deleted to prevent import of this rows to backup files
		for(Long val: listToDelete){
			sentQuery("UPDATE EX SET NAME = 'deleted' WHERE ID='"+val+"';");
		}
	}
	private List<DataTuple> readFile(){
		return readFile("SELECT * FROM EX");
	}
}
