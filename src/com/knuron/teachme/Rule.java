package com.knuron.teachme;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Rule {
	private static int counter = 0;
	private Pattern pattern;
	private String response;
	private boolean conjunctive;
	private int id;

	public Rule(String pattern, String response, boolean conjunctive) {
		this.pattern = Pattern.compile(pattern);
		this.response = response;
		this.conjunctive = conjunctive;
		this.id = ++counter;
	}
	
	public String getResponse() {
		return response;
	}
	
	public boolean isConjunctive() {
		return conjunctive;
	}
	
	public int getID() {
		return id;
	}
	
	public boolean isMatching(String input, ArrayList<String> bindings) {
		Matcher matcher = pattern.matcher(input);
		if (!matcher.matches()) return false;
		for (int i = 1; i <= matcher.groupCount(); ++i) {
			bindings.add(matcher.group(i));
		}
		return matcher.matches();
	}
}