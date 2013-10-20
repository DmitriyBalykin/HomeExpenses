package homeex;

import java.sql.ResultSet;
import java.sql.SQLException;
import static homeex.DataAccess.SEPARATOR;

public class DataTuple{
	public String prodType, prodName;
	public float prodNum, prodCost, prodTotCost;
	public long id;
	public int date;
	public DataTuple(long uid, int d, String pType, String pName, float pNum, float pCost, float pTotCost) {
		id = uid;
		date = d;
		prodType = pType;
		prodName = pName;
		prodNum = pNum;
		prodCost = pCost;
		prodTotCost = pTotCost;
	}
	public DataTuple(ResultSet res) {
		try {
			id = new Long(res.getInt("ID"));
			date = res.getInt("DATE");
			prodType = res.getString("TYPE");
			prodName = res.getString("NAME");
			prodNum = res.getFloat("NUMBER");
			prodCost = res.getFloat("ONECOST");
			prodTotCost = res.getFloat("TOTCOST");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public DataTuple(String s) throws StringFormatException {
		String[] arr = s.split(",");
		if(arr.length != 7) throw new StringFormatException();
		id = new Long(arr[0]);
		date = new Integer(arr[1]);
		prodType = arr[2];
		prodName = arr[3];
		prodNum = new Float(arr[4]);
		prodCost = new Float(arr[5]);
		prodTotCost = new Float(arr[6]);
	}
	/** Returning Date formatted to String*/
	private String getDateString(){
		String s = Integer.toString(date);
		
		return s.substring(0, 4)+"-"+s.substring(4,6)+"-"+s.substring(6,8); 
	}
	public String toString(){
		return "ID: "+id+", Date: "+date+", type: "+prodType+", name: "+prodName+", number: "+prodNum+", cost: "+prodCost+", totCost: "+prodTotCost;
	}
	/** Returning DataTuple representation formatted to string with comma-separated values*/
	public String toCSVString(){
		return id+SEPARATOR+date+SEPARATOR+prodType+SEPARATOR+prodName+SEPARATOR+prodNum+SEPARATOR+prodCost+SEPARATOR+prodTotCost;
	}
	/** Returning DataTuple representation formatted to string in HTML row */
	public String toHTMLRowString(){
		return "<TD>"+getDateString()+"</TD><TD>"+prodType+"</TD><TD>"+prodName+"</TD><TD>"+prodNum+"</TD><TD>"+prodCost+"</TD><TD>"+prodTotCost;
	}
}