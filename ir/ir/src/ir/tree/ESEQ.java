package ir.tree;

import util.IndentingWriter;
import util.List;
import ir.interp.Word;
import ir.interp.X86_64SimFrame;
import ir.visitor.Visitor;

public class ESEQ extends IRExp {
	public final IRStm stm;
	public final IRExp exp;
	public ESEQ(IRStm s, IRExp e) {stm=s; exp=e;}
	@Override
	public void dump(IndentingWriter out) {
		out.println("ESEQ(");
		out.indent();
		
		out.print(stm);
		out.println(",");
		out.print(exp);
		
		out.outdent();
		out.print(")");
	}
	@Override
	public IRExp build(List<IRExp> kids) {
		throw new Error("Not applicable to ESEQ");
	}
	@Override
	public List<IRExp> kids() {
		throw new Error("Not applicable to ESEQ");
	}
	@Override
	public Word interp(X86_64SimFrame env) {
		throw new Error("ESEQ is not atomic! Can only interp atomic statments!\n" +
				        "  (linearized IR should not have any ESEQ!)");
	}

	@Override
	public <R> R accept(Visitor<R> v) {
		return v.visit(this);
	}
}

