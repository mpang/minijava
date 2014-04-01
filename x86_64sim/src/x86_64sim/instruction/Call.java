package x86_64sim.instruction;

import x86_64sim.Memory;
import x86_64sim.State;

public class Call extends Instruction {
	String label;
	String register;

	public Call(String label) {
		this.label = label;
	}

	public Call(String star, String register) {
		this.register = register;
	}

	@Override
	public void execute(State state) {
		if (state.beVerbose)
			System.out.println("Call " + label);

		if (register != null) {
			// An indirect call
			long sp = state.getReg(Instruction.SP) - Memory.SIZEOF_LONG;
			state.ram.write(sp, state.pc);
			state.setReg(Instruction.SP, sp);
			long value = state.getReg(register);
			state.do_call();
			state.jump(value);
		} else {
			if (label.equals("_cs411println")  || label.equals("cs411println")) {
				// Simulate println
				state.out.println(state.getReg(Instruction.ARG1));
				if (state.beVerbose)
					System.out.println("Return to " + state.pc + " " + state.p.getFunctionAt(state.pc));
			} else if (label.equals("_cs411newobject") || label.equals("cs411newobject")) {
				// Simulate new object
				state.setReg(Instruction.AX, state.alloc(state.getReg(Instruction.ARG1)));
				if (state.beVerbose)
					System.out.println("Return to " + state.pc + " " + state.p.getFunctionAt(state.pc));
			} else if (label.equals("_cs411newarray") || label.equals("cs411newarray")) {
				// Simulate new array
				state.setReg(Instruction.AX, state.allocArray(state.getReg(Instruction.ARG1)));
				if (state.beVerbose)
					System.out.println("Return to " + state.pc + " " + state.p.getFunctionAt(state.pc));
			} else if (label.equals("_cs411error") || label.equals("cs411error")) {
				// Simulate error
				// System.err.println("MiniJava failure " + state.getReg(Instruction.ARG1));
				throw new Error("MiniJava failure " + state.getReg(Instruction.ARG1) + "\n");
				// Bail
			} else {
				// push ra
				long sp = state.getReg(Instruction.SP) - Memory.SIZEOF_LONG;
				state.ram.write(sp, state.pc);
				state.setReg(Instruction.SP, sp);
				state.do_call();
				state.jump(label);
			}
		}
	}

	@Override
	public String toString() {
		if (label == null) 
			return "\tcall\t*" + register;
		else
			return "\tcall\t" + label;

	}
}
