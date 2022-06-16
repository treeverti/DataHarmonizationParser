
public class Style {
	private String name = "";
	private String details = "";
	
	public Style() {
		name = "";
		details = "";
	}
	
	public Style(String name, String details) {
		this.name = name;
		this.details = details;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}
	
	
	public String toString() {
		return name+":"+details;
	}

}
