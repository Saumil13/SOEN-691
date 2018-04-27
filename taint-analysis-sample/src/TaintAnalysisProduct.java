

import java.util.Collection;
import soot.Value;
import java.lang.reflect.Method;
import soot.ValueBox;
import soot.Unit;
import java.util.Set;
import soot.jimple.InvokeStmt;
import soot.jimple.InvokeExpr;
import soot.SootMethod;
import java.util.HashSet;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.VirtualInvokeExpr;

public class TaintAnalysisProduct {
	public boolean containsValues(Collection<Value> vs, Object s) {
		for (Value v : vs)
			if (containsValue(v, s))
				return true;
		return false;
	}

	public boolean containsValue(Value v, Object s) {
		try {
			Method m = s.getClass().getMethod("getUseBoxes");
			for (ValueBox b : (Collection<ValueBox>) m.invoke(s))
				if (b.getValue().equals(v))
					return true;
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isTaintedPublicSink(Unit u, Set<Value> in) {
		if (u instanceof InvokeStmt) {
			InvokeExpr e = ((InvokeStmt) u).getInvokeExpr();
			SootMethod m = e.getMethod();
			if (m.getName().equals("println") && m.getDeclaringClass().getName().equals("java.io.PrintStream")
					&& containsValues(in, e))
				return true;
		}
		return false;
	}

	public Set<Value> newTaintedValues(Set<Value> in, Unit node) {
		Set<Value> out = new HashSet();
		if (containsValues(in, node)) {
			if (node instanceof AssignStmt) {
				out.add(((AssignStmt) node).getLeftOpBox().getValue());
			} else if (node instanceof IfStmt) {
				IfStmt i = (IfStmt) node;
				if (i.getTarget() instanceof AssignStmt)
					out.add(((AssignStmt) i.getTarget()).getLeftOpBox().getValue());
			}
		} else if (node instanceof AssignStmt) {
			AssignStmt assn = (AssignStmt) node;
			if (isPrivateSource(assn.getRightOpBox().getValue()))
				out.add(assn.getLeftOpBox().getValue());
		}
		return out;
	}

	public boolean isPrivateSource(Value u) {
		if (u instanceof VirtualInvokeExpr) {
			VirtualInvokeExpr e = (VirtualInvokeExpr) u;
			SootMethod m = e.getMethod();
			if (m.getName().equals("readLine") && m.getDeclaringClass().getName().equals("java.io.Console"))
				return true;
		}
		return false;
	}
}