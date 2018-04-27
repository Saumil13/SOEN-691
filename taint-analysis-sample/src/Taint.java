import java.util.*;
import java.lang.reflect.Method;
import soot.*;
import soot.options.*;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.baf.*;
import soot.jimple.*;

public class Taint {
    public static void main(String[] args) {
	PackManager.v().getPack("jtp").add(new Transform("jtp.myTransform", new BodyTransformer() {
		protected void internalTransform(Body body, String phase, Map options) {
		    new TaintAnalysisWrapper(new ExceptionalUnitGraph(body));
		}
	    }));
	soot.Main.main(args);
    }
}

class TaintAnalysisWrapper {
    public TaintAnalysisWrapper(UnitGraph graph) {
	TaintAnalysis analysis = new TaintAnalysis(graph);

	if(analysis.taintedSinks.size() > 0) {
	    G.v().out.print("FAILURE: ");
	    G.v().out.println(analysis.taintedSinks);
	}
    }
}

interface GetUseBoxes {
    public List<ValueBox> getUseBoxes();
}

class TaintAnalysis extends ForwardFlowAnalysis<Unit, Set<Value>> {
    private TaintAnalysisProduct taintAnalysisProduct = new TaintAnalysisProduct();
	public Map<Unit, Set<Set<Value>>> taintedSinks;

    public TaintAnalysis(UnitGraph graph) {
	super(graph);

	taintedSinks = new HashMap();

	doAnalysis();
    }

    protected Set<Value> newInitialFlow() {
	return new HashSet();
    }

    protected Set<Value> entryInitialFlow() {
	return new HashSet();
    }

    protected void copy(Set<Value> src, Set<Value> dest) {
	dest.removeAll(dest);
	dest.addAll(src);
    }

    // Called after if/else blocks, with the result of the analysis from both
    // branches.
    protected void merge(Set<Value> in1, Set<Value> in2, Set<Value> out) {
	out.removeAll(out);
	out.addAll(in1);
	out.addAll(in2);
    }

    protected void flowThrough(Set<Value> in, Unit node, Set<Value> out) {
	out(in, node, out);
	System.out.println(in);
	System.out.println(node);
	taintedSinks(in, node);
    }

	private void taintedSinks(Set<Value> in, Unit node) {
		if (taintAnalysisProduct.isTaintedPublicSink(node, in)) {
			taintedSinksProduct(in, node);
		}
	}

	private void taintedSinksProduct(Set<Value> in, Unit node) {
		if (!taintedSinks.containsKey(node))
			taintedSinks.put(node, new HashSet());
		taintedSinks.get(node).add(in);
	}

	private void out(Set<Value> in, Unit node, Set<Value> out) {
		Set<Value> filteredIn = stillTaintedValues(in, node);
		Set<Value> newOut = taintAnalysisProduct.newTaintedValues(filteredIn, node);
		out.removeAll(out);
		out.addAll(filteredIn);
		out.addAll(newOut);
	}

    protected Set<Value> stillTaintedValues(Set<Value> in, Unit node) {
	return in;
    }

    // It would be sweet if java had a way to duck type, but it doesn't so we have to do this.
    protected boolean containsValues(Collection<Value> vs, Object s) {
	return taintAnalysisProduct.containsValues(vs, s);
    }

    protected boolean containsValue(Value v, Object s) {
	return taintAnalysisProduct.containsValue(v, s);
    }

    protected Set<Value> newTaintedValues(Set<Value> in, Unit node) {
	return taintAnalysisProduct.newTaintedValues(in, node);
    }

    protected boolean isPrivateSource(Value u) {
	return taintAnalysisProduct.isPrivateSource(u);
    }

    protected boolean isTaintedPublicSink(Unit u, Set<Value> in) {
	return taintAnalysisProduct.isTaintedPublicSink(u, in);
    }

}
