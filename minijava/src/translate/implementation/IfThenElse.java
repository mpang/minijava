package translate.implementation;

import static ir.tree.IR.ESEQ;
import static ir.tree.IR.JUMP;
import static ir.tree.IR.LABEL;
import static ir.tree.IR.MOVE;
import static ir.tree.IR.SEQ;
import static ir.tree.IR.TEMP;
import ir.temp.Label;
import ir.temp.Temp;
import ir.tree.IRExp;
import ir.tree.IRStm;

public class IfThenElse extends TRExp {
  
  private final TRExp test;
  private final TRExp thn;
  private final TRExp els;
  private final Label t = Label.gen();
  private final Label f = Label.gen();
  private final Label join = Label.gen();
  
  public IfThenElse(TRExp test, TRExp thn, TRExp els) {
    this.test = test;
    this.thn = thn;
    this.els = els;
  }

  @Override
  public IRExp unEx() {
    Temp temp = new Temp();
    return ESEQ(SEQ(test.unCx(t, f),
                    LABEL(t),
                    MOVE(temp, thn.unEx()),
                    JUMP(join),
                    LABEL(f),
                    MOVE(temp, els.unEx()),
                    LABEL(join)),
                TEMP(temp));
  }

  @Override
  public IRStm unNx() {
    return SEQ(test.unCx(t, f),
               LABEL(t),
               thn.unNx(),
               JUMP(join),
               LABEL(f),
               els.unNx(),
               LABEL(join));
  }

  @Override
  public IRStm unCx(Label ifTrue, Label ifFalse) {
    return null;
  }

  @Override
  public IRStm unCx(IRExp dst, IRExp src) {
    return null;
  }

}
