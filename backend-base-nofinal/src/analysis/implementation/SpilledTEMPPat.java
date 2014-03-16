package analysis.implementation;

import util.IndentingWriter;
import util.List;
import ir.tree.IRExp;
import ir.tree.TEMP;
import codegen.patterns.Matched;
import codegen.patterns.Pat;

public class SpilledTEMPPat extends Pat<IRExp> {
	
	private Pat<SpillColor> color;

	public SpilledTEMPPat(Pat<SpillColor> color) {
		this.color = color;
	}

	@SuppressWarnings("unchecked") @Override
	public Pat<IRExp> build(List<Pat<?>> children) {
		return new SpilledTEMPPat((Pat<SpillColor>) children.get(0));
	}

	@Override
	public List<Pat<?>> children() {
		return List.list(new Pat<?>[] {color});
	}

	@Override
	public void match(IRExp toMatch, Matched matched) throws Failed {
		TEMP tmp = (TEMP) toMatch;
		SpillColor color = (SpillColor) tmp.getColor();
		if (color == null) fail();
		this.color.match(color, matched);
	}

	@Override
	public void dump(IndentingWriter out) {
		out.print("SPILLED(");
		out.print(color);
		out.print(")");
	}

}
