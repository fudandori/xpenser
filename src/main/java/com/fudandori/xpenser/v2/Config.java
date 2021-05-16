package com.fudandori.xpenser.v2;

import java.util.ArrayList;
import java.util.List;

import com.fudandori.xpenser.v2.process.Group;

public class Config {

	private int concept;
	private int expenses;
	private int date;
	private int start;
	private int balance;
	private String bank;
	private String lang;
	private List<Group> groups;


	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public int getConcept() {
		return concept;
	}

	public void setConcept(int concept) {
		this.concept = concept;
	}

	public int getExpenses() {
		return expenses;
	}

	public void setExpenses(int expenses) {
		this.expenses = expenses;
	}

	public int getDate() {
		return date;
	}

	public void setDate(int date) {
		this.date = date;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public List<Group> getGroups() {
		return groups != null ? groups : new ArrayList<>();
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}
	
	public void setParams(int concept, int expenses, int date, int start, int balance) {
		this.concept = concept;
		this.expenses = expenses;
		this.date = date;
		this.start = start;
		this.balance = balance;
	}
	
	public boolean hasBank() {
		return getBank() != null && !getBank().isEmpty();
	}

}
