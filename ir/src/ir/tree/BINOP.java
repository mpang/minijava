package ir.tree;

import ir.interp.Word;
import ir.interp.X86_64SimFrame;
import ir.visitor.Visitor;
import util.IndentingWriter;
import util.List;

public class BINOP extends IRExp {

	public Op binop;
	public IRExp left;
	public IRExp right;
	
	public IRExp getLeft() {
		return left;
	}

	public IRExp getRight() {
		return right;
	}

	public enum Op {
		PLUS, MINUS, MUL, DIV, 
		AND,OR,LSHIFT,RSHIFT,ARSHIFT,XOR;
	}

	public BINOP(Op b, IRExp l, IRExp r) {
		binop=b; left=l; right=r; 
	}

	@Override
	public void dump(IndentingWriter out) {
		out.print("BINOP(");
		out.print(binop);
		out.println(",");
		out.indent();
		
		out.print(left);
		out.println(", ");
		out.print(right);
		
		out.outdent();
		out.print(")");
	}

	@Override
	public IRExp build(List<IRExp> kids) {
		return new BINOP(binop, kids.get(0), kids.get(1));
	}

	@Override
	public List<IRExp> kids() {
		return List.list(left,right);
	}

	@Override
	public Word interp(X86_64SimFrame env) {
		Word l = left.interp(env);
		Word r = right.interp(env);
		switch (binop) {
		case PLUS:
			return l.add(r);
		case MINUS:
			return l.minus(r);
		case MUL:
			return l.mul(r);
		case DIV:
			return l.div(r);
		case ARSHIFT:
			return l.arshift(r);
			//TODO: implement the other binops if you want to use them!
		case AND:
		  return l.and(r);
		default:
			throw new Error("Binop case missing? "+binop);
		}
	}

	public Op getOp() {
		return binop;
	}

	@Override
	public <R> R accept(Visitor<R> v) {
		return v.visit(this);
	}
}

