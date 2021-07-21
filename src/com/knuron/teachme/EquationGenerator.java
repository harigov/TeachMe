package com.knuron.teachme;

import java.util.ArrayList;
import java.util.HashMap;

public class EquationGenerator {
	private ArrayList<Rule> rules;
	private HashMap<String,String> varMap;
	private HashMap<String,String> actualWordMap;

	public EquationGenerator() {
		varMap = new HashMap<String,String>();
		actualWordMap = new HashMap<String,String>();
		
		rules = new ArrayList<Rule>();
		
		rules.add(new Rule("(.*)\\.", "~1~", true));
		rules.add(new Rule("(.*)\\,", "~1~", true));
		rules.add(new Rule("(.*)\\?", "~1~", true));
		rules.add(new Rule("(.*)\\. (.*)", "(~1~ ~2~)", true));
		rules.add(new Rule("if (.*)\\. then (.*)", "(~1~ ~2~)", true));
		rules.add(new Rule("if (.*) then (.*)", "(~1~ ~2~)", true));
		rules.add(new Rule("if (.*), (.*)", "(~1~ ~2~)", true));
		rules.add(new Rule("(.*), and (.*)", "(~1~ ~2~)", true));
		rules.add(new Rule("find (.*) and (.*)", "((= to-find-1 ~1~) (= to-find-2 ~2~))", false));
		rules.add(new Rule("find (.*)", "(= to-find ~1~)", false));
		rules.add(new Rule("(.*) equals (.*)", "(= ~1~ ~2~)", false));
		rules.add(new Rule("(.*) same as (.*)", "(= ~1~ ~2~)", false));
		rules.add(new Rule("(.*) = (.*)", "(= ~1~ ~2~)", false));
		rules.add(new Rule("(.*) is equal to (.*)", "(= ~1~ ~2~)", false));
		rules.add(new Rule("(.*) is (.*)", "(= ~1~ ~2~)", false));
		rules.add(new Rule("(.*) - (.*)", "(- ~1~ ~2~)", false));
		rules.add(new Rule("(.*) minus (.*)", "(- ~1~ ~2~)", false));
		rules.add(new Rule("difference between (.*) and (.*)", "(- ~1~ ~2~)", false));
		rules.add(new Rule("difference (.*) and (.*)", "(- ~1~ ~2~)", false));
		rules.add(new Rule("(.*) \\+ (.*)", "(+ ~1~ ~2~)", false));
		rules.add(new Rule("(.*) plus (.*)", "(+ ~1~ ~2~)", false));
		rules.add(new Rule("sum (.*) and (.*)", "(+ ~1~ ~2~)", false));
		rules.add(new Rule("product (.*) and (.*)", "(* ~1~ ~2~)", false));
		rules.add(new Rule("(.*) \\* (.*)", "(* ~1~ ~2~)", false));
		rules.add(new Rule("(.*) times (.*)", "(* ~1~ ~2~)", false));
		rules.add(new Rule("(.*) / (.*)", "(/ ~1~ ~2~)", false));
		rules.add(new Rule("(.*) per (.*)", "(/ ~1~ ~2~)", false));
		rules.add(new Rule("(.*) divided by (.*)", "(/ ~1~ ~2~)", false));
		rules.add(new Rule("half (.*)", "(/ ~1~ 2)", false));
		rules.add(new Rule("one half (.*)", "(/ ~1~ 2)", false));
		rules.add(new Rule("twice (.*)", "(* ~1~ 2)", false));
		rules.add(new Rule("square (.*)", "(* ~1~ ~1~)", false));
		rules.add(new Rule("(.*) % less than (.*)", "(* ~2~ (/ (- 100 ~1~) 100))", false));
		rules.add(new Rule("(.*) % more than (.*)", "(* ~2~ (/ (+ 100 ~1~) 100))", false));
		rules.add(new Rule("(.*) % (.*)", "(* (/ ~1~ 100) ~2~)", false));
		rules.add(new Rule("(.*)% less than (.*)", "(* ~2~ (/ (- 100 ~1~) 100))", false));
		rules.add(new Rule("(.*)% more than (.*)", "(* ~2~ (/ (+ 100 ~1~) 100))", false));
		rules.add(new Rule("(.*)% (.*)", "(* (/ ~1~ 100) ~2~)", false));
	}
	
	public HashMap<String,String> getVariableMap() {
		return varMap;
	}
	
	public String getActualName(String varName) {
		for(String varWord: varMap.keySet()) {
			String name = varMap.get(varWord);
			if (name.equals(varName)) return actualWordMap.get(varWord);
		}
		return varName;
	}
	
	public void clear() {
		varMap.clear();
	}
	
	public ArrayList<Equation> generate(String input) throws Exception
	{
		input = input.toLowerCase();
		input = removeNoiseWords(input);
		ArrayList<Equation> output = new ArrayList<Equation>();
		translate(input, output);
		//simplifyEquations(eqns, output);
		return output;
	}
	
	public void printVariables() {
		System.out.println("Printing all the variables");
		for (String key: varMap.keySet()) {
			System.out.println(key + " = " + varMap.get(key));
		}
	}
	
	private String removeNoiseWords(String input)
	{
		String[] noiseWords = new String[] {"a", "an", "the", "this", "number", "of", "$"};
		String[] splitStrs = input.split(" ");
		String output = null;
		for (String str: splitStrs) {
			boolean cont = false;
			for (String nw: noiseWords) {
				if (str.equals(nw)) {
					cont = true;
					break;
				}
			}
			if (!cont) {
				if (output == null) output = str;
				else output += " " + str;
			}
		}
		output = output.replaceAll("\\'s", "");
		return output;
	}

	private String translate(String input, ArrayList<Equation> eqns) {
		System.out.println("translate called with " + input);
		input = input.trim();
		for (Rule rule : rules) {
			ArrayList<String> bindings = new ArrayList<String>();
			if (rule.isMatching(input, bindings))
			{
				System.out.println ("Rule " + rule.getID() + " is matching");
				String result = null;
				if (rule.isConjunctive()) {
					for (int i = 0; i < bindings.size(); ++i) {
						String transStr = translate(bindings.get(i), eqns);
						if (transStr != null) {
							eqns.add(new Equation(transStr, bindings.get(i)));
						}
					}
					return null;
				} else {
					result = new String(rule.getResponse());
					for (int i = 0; i < bindings.size(); ++i) {
						String translatedStr = translate(bindings.get(i), eqns);
						result = result.replaceAll("~" + Integer.toString(i + 1)
								+ "~", translatedStr);
					}
					if (result.contains("to-find"))
						result = result.replaceAll("to-find", createVariable("to-find"));
					else if (result.contains("to-find-1"))
						result = result.replaceAll("to-find-1", createVariable("to-find-1"));
					else if (result.contains("to-find-2"))
						result = result.replaceAll("to-find-2", createVariable("to-find-2"));
					System.out.println("Returning " + result);
					return result;
				}
			}
		}
		System.out.println("Returning " + createVariable(input));
		return createVariable(input);
	}
	
	private String createVariable(String input) {
		System.out.println("Before split " + input);
		String word = input.split(" ")[0];
		System.out.println("After split " + word);
		if (varMap.containsKey(word)) {
			return varMap.get(word);
		} 
		try {
			Double.parseDouble(word);
			return word;
		} catch (NumberFormatException e) {
			
		}

		char c = (char) (65 + varMap.size());
		String varName = "~" + Character.toString(c) + "~";
		varMap.put(word, varName);
		actualWordMap.put(word, input);
		return varName;
	}
}