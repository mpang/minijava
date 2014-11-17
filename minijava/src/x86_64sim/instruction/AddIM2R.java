package x86_64sim.instruction;

import x86_64sim.State;

public class AddIM2R extends Instruction {
  
  long offset;
  String source;
  String target;

  public AddIM2R(String offset, String source, String target) {
    this.offset = Long.parseLong(offset);
    this.source = source;
    this.target = target;
  }
  
  @Override
  public void execute(State state) {
    long value = state.ram.read(state.getReg(source) + offset) + state.getReg(target);
    if (state.beVerbose)
      System.out.println(target + " <- " + value);
    state.setReg(target, value);
  }

  @Override
  public String toString() {
    return "\taddq\t" + offset + "(" + source + ")" + ", " + target;
  }

}
