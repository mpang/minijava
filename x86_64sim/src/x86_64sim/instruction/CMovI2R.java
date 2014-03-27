package x86_64sim.instruction;

import x86_64sim.State;

public class CMovI2R extends Instruction {
  
  String condition, target;
  long source;
  
  public CMovI2R(String condition, String source, String target) {
    this.condition = condition;
    this.source = Long.parseLong(source);
    this.target = target;
  }

  @Override
  public void execute(State state) {
    if (state.conditionTrue(condition)) {
      if (state.beVerbose)
        System.out.println("cmov" + condition + " true " + target + " <- " + source);
      state.setReg(target, source);
    }
  }

  @Override
  public String toString() {
    return "\tcmov" + condition + "\t$" + source + ", " + target;
  }
}
