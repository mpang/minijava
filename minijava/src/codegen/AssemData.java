package codegen;

import static util.List.cons;
import static util.List.list;
import ir.frame.Frame;
import ir.frame.x86_64.X86_64Frame;
import ir.temp.Label;
import ir.temp.Temp;
import ir.tree.IR;
import ir.tree.IRExp;
import ir.tree.IRData;
import ir.tree.IRStm;
import junit.framework.Assert;
import codegen.assem.A_LABEL;
import codegen.assem.A_OPER;
import codegen.assem.Instr;
import codegen.muncher.Muncher;
import codegen.x86_64.X86_64Muncher;
import translate.DataFragment;
import translate.ProcFragment;
import util.IndentingWriter;
import util.List;
import util.Utils;

public class AssemData extends AssemFragment {

	private DataFragment dataIR;
	private List<Instr> asmBody;
	private boolean optimize = false;

	public void setOptimize(boolean optimize) {
		this.optimize = optimize;		
	}
	public AssemData(DataFragment dataIR) {
		this.dataIR = dataIR;
		rewrite(); // Actually not "re" writing, but writing for the first time :-)
	}

	@Override
	public void dump(IndentingWriter out) {
		out.println(".data");

		out.indent();

		for (Instr instr : getBody()) {
			out.println(instr);
		}

		out.outdent();
	}

	public void remove(Instr instr) {
		// Should check that this doesn't break things horribly
		asmBody = asmBody.delete(instr);
	}

	public void replace(Instr oldi, Instr newi) {
		// Should check that this doesn't break things horribly
		Assert.assertFalse(oldi instanceof A_LABEL);
		Assert.assertFalse(newi instanceof A_LABEL);

		asmBody = asmBody.replace(oldi, newi);
	}

	public Label getLabel() {
		return dataIR.getBody().getLabel();
	}

	public List<Instr> getBody() {
		return asmBody;
	}

	/**
	 * After doing register allocation with spilled registers. You can use
	 * this method to rewrite the instructions in the body from the IR. 
	 * <p>
	 * To make this actually work, you will need to do something to define
	 * proper code generation rules to handle spilled Temps.
	 * <p>
	 * (At least) two options are available:
	 *  - define special patterns and rules to match spilled temps explicitly.
	 *  - alter the implementation of MEMPat to allow it to match not just
	 *  MEM nodes but also spilled Temp nodes.
	 */

	public Muncher newMuncher() {
		return new X86_64Muncher(null);
	}

	public void rewrite() {
		IRData body = dataIR.getBody();
		Muncher m = newMuncher();
		m.munch(IR.LABEL(body.getLabel()));
		for (IRExp e : body) {
			m.munchData(e);
		}
		asmBody = m.getInstructions();
	}
}
