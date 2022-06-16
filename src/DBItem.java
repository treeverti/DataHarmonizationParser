
public class DBItem {
	private String header = "";
	private String value = "";
	private String table = "";
	
	public DBItem(String table, String header, String value, String filename, String report) {
		this.table = table;
		this.header = header;
		this.value = value;
	}
	
	public DBItem(String table, String header, String value, String domain, String EWCPTable, String filename, String report) {
		this.table = table;
		this.header = header;
		this.value = value;
	}
	
	public DBItem() {
		table = "";
		header = "";
		value = "";
	}
	
	public String getTable() {
		return table;
	}
	
	public void setTable(String t) {
		table = t;
	}
	
	public String getHeader() {
		return header;
	}
	
	public void setHeader(String h) {
		header = h;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String v) {
		value = v;
	}
	
	public void addToValue(String v) {
		value += v;
	}

	public boolean isFullyFilled() {
		if(!header.equals("") && !value.equals("")) {
			return true;
		}
		return false;
	}
	
	public boolean isOnlyHeaderFilled() {
		if(!header.equals("") && value.equals("")) {
			return true;
		}
		return false;
	}
	
	public String toString() {
		return "\""+table.replaceAll("\"", "")+"\""+","+"\""
					+header.replaceAll("\"", "")+"\""+","+"\""
					+value.replaceAll("\"", "")+"\"";
	}
}
