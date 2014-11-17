package ir.tree;

import util.IndentingWriter;
import util.List;
import ir.interp.Word;
import ir.interp.X86_64SimFrame;
import ir.temp.Label;
import ir.visitor.Visitor;
public class NAME extends IRExp {
	public Label label;
	public NAME(Label l) {label=l;}
	@Override
	public void dump(IndentingWriter out) {
		out.print("NAME(");
		out.print(label);
		out.print(")");
	}
	@Override
	public IRExp build(List<IRExp> kids) {
		return this;
	}
	@Override
	public List<IRExp> kids() {
		return List.empty();
	}
	@Override
	public Word interp(X86_64SimFrame env) {
		return env.getLabel(label);
	}
	public Label getLabel() {
		return label;
	}

	@Override
	public <R> R accept(Visitor<R> v) {
		return v.visit(this);
	}
}

