package ir.tree;

import util.IndentingWriter;
import util.List;
import ir.interp.Ptr;
import ir.interp.Word;
import ir.interp.X86_64SimFrame;
import ir.visitor.Visitor;

public class MEM extends IRExp {
	public final IRExp exp;
	public MEM(IRExp e) {exp=e;}
	@Override
	public void dump(IndentingWriter out) {
		out.print("MEM(");
		out.print(exp);
		out.print(")");
	}
	@Override
	public IRExp build(List<IRExp> kids) {
		return new MEM(kids.head());
	}
	@Override
	public List<IRExp> kids() {
		return List.list(exp);
	}
	@Override
	public Word interp(X86_64SimFrame env) {
		//Subtle point: we only get here if the MEM is being read.
		//The interp for MOVE treats the MEM case in its dst explicitly.
		Ptr p = (Ptr) exp.interp(env);
		return p.get();
	}
	
	@Override
	public void set(Word value, X86_64SimFrame env) {
		Ptr d = (Ptr) exp.interp(env);
		d.set(value);
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

