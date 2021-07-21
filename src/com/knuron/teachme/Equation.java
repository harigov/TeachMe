package com.knuron.teachme;


import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Equation {
	private String eqn;
	private String spokenEqn;
	public Equation(String eqn, String spokenEqn) {
		this.eqn = eqn;
		this.spokenEqn = spokenEqn;
	}
	public Equation(String op, Equation lhs, Equation rhs) {
		this.eqn = "(" + op + " " + lhs.getEquation() + " " + rhs.getEquation() + ")";
	}
	
	public boolean hasVar(String var)
	{
		return eqn.contains(var);
	}
	
	public String findUnknown()
	{
		int stIdx = eqn.indexOf('~');
		return eqn.substring(stIdx, stIdx+3);
	}
	
	public int numUnknowns()
	{
		Pattern pattern = Pattern.compile("~\\p{Alpha}~");
		Matcher matcher = pattern.matcher(eqn);
		ArrayList<String> matchedVars = new ArrayList<String>();
		int cnt = 0;
		while(matcher.find()) {
			String varName = eqn.substring(matcher.start(), matcher.end());
			boolean alreadyContains = false;
			for (String v: matchedVars) {
				if (v.equals(varName)) {
					alreadyContains = true;
				}
			}
			if (!alreadyContains) {
				++cnt;
				matchedVars.add(varName);
			}
		}
		return cnt;
	}
	
	public String getEquation()
	{
		return eqn;
	}
	
	public String getSpokenEquation() {
		return spokenEqn;
	}
	
	public String getOp()
	{
		return eqn.substring(1, 2);
	}
	
	public Equation getLHS()
	{
		String subStr = eqn.substring(1, eqn.length()-1);
		int spaceIdx = subStr.indexOf(' ');
		if (subStr.charAt(spaceIdx+1) == '(') {
			return new Equation(subStr.substring(spaceIdx+1, findClosingParen(subStr, spaceIdx+1)+1), "UNKNOWN");
		} else {
			return new Equation(subStr.substring(spaceIdx+1, subStr.indexOf(' ', spaceIdx + 1)), "UKNOWN");
		}
	}
	
	public Equation getRHS()
	{
		String subStr = eqn.substring(1, eqn.length()-1);
		String lhs = getLHS().getEquation();
		return new Equation(subStr.substring(subStr.indexOf(lhs) + lhs.length() + 1, subStr.length()),"UNKNOWN");
	}
	
	public Equation substitute(String var, String value)
	{
		String outEqn = eqn.replaceAll(var, value);
		return new Equation(outEqn, "UNKNOWN");
	}
	
	public double evaluate() throws Exception
	{
		if (eqn.startsWith("(")) {
			String op = getOp();
			double lval = getLHS().evaluate();
			double rval = getRHS().evaluate();
			if (op.equals("+")) return (lval+rval);
			else if (op.equals("-")) return (lval-rval);
			else if (op.equals("*")) return (lval*rval);
			else if (op.equals("/")) return (lval/rval);
			throw new Exception("simpleSolve is passed illegal operator");
		} else {
			return Double.parseDouble(eqn);
		}
	}
	
	private int findClosingParen(String input, int startIdx) {
		int cIdx = startIdx, cnt = 1;
		while (cIdx < input.length() && cnt > 0) {
			++cIdx;
			if (input.charAt(cIdx) == '(') ++cnt;
			else if (input.charAt(cIdx) == ')') --cnt;
		}
		return cIdx;
	}
}
