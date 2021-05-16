package com.fudandori.xpenser.v2.process;

public class Group {
	private String name;
	private String value;
	private Regex regex;

	public enum Regex {
		STARTS, CONTAINS, ENDS 
	}

	public Group(String name, String value, int regex) {
		this.name = name;
		this.value = value;

		switch (regex) {
		case 2:
			this.regex = Regex.CONTAINS;
			break;
		case 3:
			this.regex = Regex.ENDS;
			break;
		case 1:
		default:
			this.regex = Regex.STARTS;
			break;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Regex getRegex() {
		return regex;
	}

	public void setRegex(Regex regex) {
		this.regex = regex;
	}
	
	public String generateRegex(String value) {
		
				
		switch(this.regex) {
		case CONTAINS:
			value = ".*" + value + ".*";
			break;
		case ENDS:
			value = ".*" + value + "$";
			break;
		case STARTS:
			value = "^" + value + ".*"; 
			break;
		default:
			break;
		}
		
		return value;
	}
}
