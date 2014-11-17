package x86_64sim.instruction;

import x86_64sim.State;

public class AndR2R extends Instruction {
  String source;
  String target;
  
  public AndR2R(String source, String target) {
    this.source = source;
    this.target = target;
  }

  @Override
  public void execute(State state) {
    long value = state.getReg(target) & state.getReg(source);
    if (state.beVerbose)
      System.out.println(target + " <- " + value);
    state.setReg(target, value);
  }

  @Override
  public String toString() {
    return "\tandq\t" + source + ", " + target;
  }
}
