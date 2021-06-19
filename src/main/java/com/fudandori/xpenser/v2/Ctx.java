package com.fudandori.xpenser.v2;

import javafx.geometry.Insets;

public class Ctx {
	
	private Ctx() {}
	
	public static Config config;
	
	public static final Insets PADDING = new Insets(10d);
	
	public static String balanceColumn;
	public static String conceptColumn;
	public static String expensesColumn;
	public static String dateColumn;
	public static String firstRowText;
	public static String settings;
	public static String save;
	public static String add;
	public static String groups;
	public static String name;
	public static String value;
	public static String startsWith;
	public static String endsWith;
	public static String contains;
	public static String removed;
	public static String remove;
	public static String close;

	public static void load() {
		config = Utility.getConfig();
	}
}
