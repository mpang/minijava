package ir.tree;

import util.IndentingWriter;
import util.List;
import ir.interp.Callable;
import ir.interp.Word;
import ir.interp.X86_64SimFrame;
import ir.visitor.Visitor;

public class CALL extends IRExp {
	public IRExp func;
	public List<IRExp> args;
	
	public CALL(IRExp f, List<IRExp> a) {func=f; args=a;}
	
	@Override
	public void dump(IndentingWriter out) {
		out.println("CALL(");
		out.indent();
		
		out.print(func);
		for (IRExp arg : args) {
			out.println(",");
			out.print(arg);
		}
		out.outdent();
		out.print(")");
	}

	@Override
	public IRExp build(List<IRExp> kids) {
		return new CALL(kids.head(), kids.tail());
	}

	@Override
	public List<IRExp> kids() {
		return List.cons(func, args);
	}

	@Override
	public Word interp(X86_64SimFrame env) {
		Callable procVal = (Callable) func.interp(env);
		List<Word> argVals = List.list();
		for (IRExp arg : args) {
			argVals.add(arg.interp(env));
		}
		return procVal.call(env.getInterp(), argVals);
	}

	public IRExp getFunc() {
		return func;
	}

	public List<IRExp> getArgs() {
		return args;
	}
	
	@Override
	public boolean mentionsMemOrCall() {
		return true;
	}

	@Override
	public <R> R accept(Visitor<R> v) {
		return v.visit(this);
	}
}

