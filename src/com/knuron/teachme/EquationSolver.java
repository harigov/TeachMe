package com.knuron.teachme;

import java.util.ArrayList;
import java.util.HashMap;

public class EquationSolver {
	
	public EquationSolver() 
	{
	}
	
	public void solve(ArrayList<Equation> eqns, HashMap<String,Double> bindings) throws Exception
	{
		for (Equation eqn: eqns) {
			if (eqn.numUnknowns () == 1) {
				String var = eqn.findUnknown();
				Equation isolatedEqn = isolate(eqn, var);
				double value = isolatedEqn.getRHS().evaluate();
				ArrayList<Equation> substEqns = substitute(eqns, var, value);
				bindings.put(var, value);
				solve (substEqns, bindings);
			}
		}
	}
	
	public Equation isolate(Equation eqn, String var) throws Exception
	{
		Equation lhs = eqn.getLHS();
		Equation rhs = eqn.getRHS();
		if (lhs.getEquation().equals(var)) {
			return eqn;
		} else if (rhs.hasVar(var)) {
			return isolate(new Equation("=", rhs, lhs), var);
		} else if (lhs.getLHS().hasVar(var)) {
			return isolate(new Equation("=", lhs.getLHS(),
					new Equation(inverseOp(lhs.getOp()), rhs, lhs.getRHS())), var);
		} else if (isCommutative(lhs.getOp())) {
			return isolate(new Equation("=", lhs.getLHS(), 
					new Equation(inverseOp(lhs.getOp()), rhs, lhs.getRHS())), var);
		} else {
			return isolate(new Equation("=", lhs.getRHS(),
					new Equation(lhs.getOp(), lhs.getLHS(), rhs)), var);
		}
	}
	
	private ArrayList<Equation> substitute(ArrayList<Equation> eqns, String var, double value)
	{
		ArrayList<Equation> outEqns = new ArrayList<Equation>();
		String strValue = Double.toString(value);
		for(Equation eqn: eqns) {
			Equation outEqn = eqn.substitute(var, strValue);
			outEqns.add(outEqn);
		}
		return outEqns;
	}
	
	private boolean isCommutative(String op) throws Exception
	{
		return op.equals("+") || op.equals("*") || op.equals("=");
	}
	
	private String inverseOp(String op) throws Exception
	{
		if (op.equals("+")) return "-";
		else if (op.equals("-")) return "+";
		else if (op.equals("*")) return "/";
		else if (op.equals("/")) return "*";
		throw new Exception("Invalid operator");
	}
}
