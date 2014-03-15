package codegen.x86_64;

import static codegen.patterns.IRPat.*;
import static ir.frame.x86_64.X86_64Frame.RAX;
import static ir.frame.x86_64.X86_64Frame.RDX;
import static ir.frame.x86_64.X86_64Frame.RV;
import static ir.frame.x86_64.X86_64Frame.arguments;
import static ir.frame.x86_64.X86_64Frame.callerSave;
import static ir.frame.x86_64.X86_64Frame.special;
import static util.List.list;
import ir.frame.Frame;
import ir.temp.Label;
import ir.temp.Temp;
import ir.tree.CJUMP.RelOp;
import ir.tree.IR;
import ir.tree.IRExp;
import ir.tree.IRStm;
import util.IndentingWriter;
import util.List;
import codegen.assem.A_LABEL;
import codegen.assem.A_MOVE;
import codegen.assem.A_OPER;
import codegen.assem.Instr;
import codegen.muncher.MunchRule;
import codegen.muncher.Muncher;
import codegen.muncher.MuncherRules;
import codegen.patterns.Matched;
import codegen.patterns.Pat;
import codegen.patterns.Wildcard;

/**
 * This Muncher implements the munching rules for a subset of X86 instruction
 * set.
 * 
 * @author kdvolder
 */
public class X86_64Muncher extends Muncher {

  /**
   * If this flag is false, then we only use a bare minimum of small tiles. This
   * should be enough to generate working code, but it generates a lot of
   * instructions (all things operated on are first loaded into a temp).
   */
  private static final List<Temp> noTemps = List.empty();

  private static MuncherRules<IRStm, Void> sm = new MuncherRules<IRStm, Void>();
  private static MuncherRules<IRExp, Temp> em = new MuncherRules<IRExp, Temp>();

  public X86_64Muncher(Frame frame) {
    super(frame, sm, em);
  }

  public X86_64Muncher(Frame frame, boolean beVerbose) {
    super(frame, sm, em, beVerbose);
  }

  // ////////// The munching rules ///////////////////////////////

  static { // Done only once, at class loading time.

    // Pattern "variables" (used by the rules below)

    final Pat<IRExp> _e_ = Pat.any();
    final Pat<IRExp> _l_ = Pat.any();
    final Pat<IRExp> _r_ = Pat.any();

    final Pat<List<IRExp>> _es_ = Pat.any();

    final Pat<Label> _lab_ = Pat.any();
    final Pat<Label> _thn_ = Pat.any();
    final Pat<Label> _els_ = Pat.any();

    final Pat<RelOp> _relOp_ = Pat.any();

    final Pat<Temp> _t_ = Pat.any();

    final Pat<Integer> _i_ = Pat.any();

    final Pat<Integer> _scale_ = new Wildcard<Integer>() {
      @Override
      public void match(Integer toMatch, Matched matched) throws Failed {
        int value = toMatch;
        if (value == 1 || value == 2 || value == 4 || value == 8)
          super.match(toMatch, matched);
        else
          fail();
      }

      public void dump(IndentingWriter out) {
        out.print("1|2|4|8");
      }
    };

    // ############ A basic set of small tiles ############

    // ############ statements ############
    
    sm.add(new MunchRule<IRStm, Void>(LABEL(_lab_)) {
      @Override
      protected Void trigger(Muncher m, Matched children) {
        m.emit(A_LABEL(children.get(_lab_)));
        return null;
      }
    });
    
    sm.add(new MunchRule<IRStm, Void>(JUMP(_e_)) {
      @Override
      protected Void trigger(Muncher m, Matched children) {
        // Expression shouldn't need to emit indirect jumps.
        // (assuming there's a rule to match JUMP(NAME(*))
        throw new Error("Not implemented");
      }
    });
    
    sm.add(new MunchRule<IRStm, Void>(EXP(_e_)) {
      @Override
      protected Void trigger(Muncher m, Matched children) {
        IRExp exp = children.get(_e_);
        m.munch(exp);
        return null;
      }
    });
    
    sm.add(new MunchRule<IRStm, Void>(MOVE(TEMP(_t_), _e_)) {
      @Override
      protected Void trigger(Muncher m, Matched c) {
        m.emit(A_MOV(c.get(_t_), m.munch(c.get(_e_))));
        return null;
      }
    });
    
    sm.add(new MunchRule<IRStm, Void>(MOVE(MEM(_l_), _r_)) {
      @Override
      protected Void trigger(Muncher m, Matched c) {
        Temp d = m.munch(c.get(_l_));
        Temp s = m.munch(c.get(_r_));
        m.emit(A_MOV_TO_MEM(d, s));
        return null;
      }
    });
    
    sm.add(new MunchRule<IRStm, Void>(JUMP(NAME(_lab_))) {
      @Override
      protected Void trigger(Muncher m, Matched c) {
        m.emit(A_JMP(c.get(_lab_)));
        return null;
      }
    });
    
    sm.add(new MunchRule<IRStm, Void>(CJUMP(_relOp_, _l_, _r_, _thn_, _els_)) {
      @Override
      protected Void trigger(Muncher m, Matched c) {
        m.emit(A_CMP(m.munch(c.get(_l_)), m.munch(c.get(_r_))));
        m.emit(A_CJUMP(c.get(_relOp_), c.get(_thn_), c.get(_els_)));
        return null;
      }
    });
    
    sm.add(new MunchRule<IRStm, Void>(CMOVE(_relOp_, _l_, _r_, TEMP(_t_), _e_)) {
      @Override
      protected Void trigger(Muncher m, Matched c) {
        m.emit(A_CMP(m.munch(c.get(_l_)), m.munch(c.get(_r_))));
        m.emit(A_CMOV(c.get(_relOp_), c.get(_t_), m.munch(c.get(_e_))));
        return null;
      }
    });
    
    
    // ############ expressions ############
    
    em.add(new MunchRule<IRExp, Temp>(CALL(_l_, _es_)) {
      @Override
      protected Temp trigger(Muncher m, Matched children) {
        // Expressions shouldn't need to emit indirect calls ( unless we
        // implement VMT and inheritance )
        throw new Error("Not implemented");
      }
    });
    
    em.add(new MunchRule<IRExp, Temp>(CONST(_i_)) {
      @Override
      protected Temp trigger(Muncher m, Matched c) {
        Temp t = new Temp();
        m.emit(A_MOV(t, c.get(_i_)));
        return t;
      }
    });
    
    em.add(new MunchRule<IRExp, Temp>(PLUS(_l_, _r_)) {
      @Override
      protected Temp trigger(Muncher m, Matched c) {
        Temp sum = new Temp();
        m.emit(A_MOV(sum, m.munch(c.get(_l_))));
        m.emit(A_ADD(sum, m.munch(c.get(_r_))));
        return sum;
      }
    });
    
    em.add(new MunchRule<IRExp, Temp>(MINUS(_l_, _r_)) {
      @Override
      protected Temp trigger(Muncher m, Matched c) {
        Temp res = new Temp();
        m.emit(A_MOV(res, m.munch(c.get(_l_))));
        m.emit(A_SUB(res, m.munch(c.get(_r_))));
        return res;
      }
    });
    
    em.add(new MunchRule<IRExp, Temp>(MUL(_l_, _r_)) {
      @Override
      protected Temp trigger(Muncher m, Matched c) {
        Temp res = new Temp();
        m.emit(A_MOV(res, m.munch(c.get(_l_))));
        m.emit(A_IMUL(res, m.munch(c.get(_r_))));
        return res;
      }
    });
    
    em.add(new MunchRule<IRExp, Temp>(TEMP(_t_)) {
      @Override
      protected Temp trigger(Muncher m, Matched c) {
        return c.get(_t_);
      }
    });
    
    em.add(new MunchRule<IRExp, Temp>(MEM(_e_)) {
      @Override
      protected Temp trigger(Muncher m, Matched c) {
        Temp r = new Temp();
        m.emit(A_MOV_FROM_MEM(r, m.munch(c.get(_e_))));
        return r;
      }
    });
    
    em.add(new MunchRule<IRExp, Temp>(CALL(NAME(_lab_), _es_)) {
      @Override
      protected Temp trigger(Muncher m, Matched c) {
        Frame frame = m.getFrame();
        Label name = c.get(_lab_);
        List<IRExp> args = c.get(_es_);
        for (int i = args.size() - 1; i >= 0; i--) {
          IRExp outArg = frame.getOutArg(i).exp(frame.FP());
          m.munch(IR.MOVE(outArg, args.get(i)));
        }
        m.emit(A_CALL(name, args.size()));
        return RV;
      }
    });
    
    // ############ more complicated ones ############
    
    // ############ statements ############
    
    sm.add(new MunchRule<IRStm, Void>(MOVE(TEMP(_t_), CONST(_i_))) {
      @Override
      protected Void trigger(Muncher m, Matched c) {
        m.emit(A_MOV(c.get(_t_), c.get(_i_)));
        return null;
      }
    });

    
    sm.add(new MunchRule<IRStm, Void>(MOVE(MEM(PLUS(_l_, CONST(_i_))), _e_)) {
      @Override
      protected Void trigger(Muncher m, Matched c) {
        m.emit(A_MOV_TO_MEM(c.get(_i_), m.munch(c.get(_l_)), m.munch(c.get(_e_))));
        return null;
      }
    });
    
    sm.add(new MunchRule<IRStm, Void>(MOVE(MEM(MINUS(_l_, CONST(_i_))), _e_)) {
      @Override
      protected Void trigger(Muncher m, Matched c) {
        m.emit(A_MOV_TO_MEM(-1 * c.get(_i_), m.munch(c.get(_l_)), m.munch(c.get(_e_))));
        return null;
      }
    });
    
    sm.add(new MunchRule<IRStm, Void>(CJUMP(_relOp_, _l_, CONST(_i_), _thn_, _els_)) {
      @Override
      protected Void trigger(Muncher m, Matched c) {
        m.emit(A_CMP(m.munch(c.get(_l_)), c.get(_i_)));
        m.emit(A_CJUMP(c.get(_relOp_), c.get(_thn_), c.get(_els_)));
        return null;
      }
    });
    
    sm.add(new MunchRule<IRStm, Void>(CJUMP(_relOp_, MEM(PLUS(CONST(_i_), _e_)), _r_, _thn_, _els_)) {
      @Override
      protected Void trigger(Muncher m, Matched c) {
        m.emit(A_CMP_FROM_MEM(c.get(_i_), m.munch(c.get(_e_)), m.munch(c.get(_r_))));
        m.emit(A_CJUMP(c.get(_relOp_), c.get(_thn_), c.get(_els_)));
        return null;
      }
    });
    
    
    // ############ expressions ############
    
    em.add(new MunchRule<IRExp, Temp>(AND(_l_, _r_)) {
      @Override
      protected Temp trigger(Muncher m, Matched c) {
        Temp res = new Temp();
        m.emit(A_MOV(res, m.munch(c.get(_l_))));
        m.emit(A_AND(res, m.munch(c.get(_r_))));
        return res;
      }
    });
    
    em.add(new MunchRule<IRExp, Temp>(PLUS(CONST(_i_), _r_)) {
      @Override
      protected Temp trigger(Muncher m, Matched c) {
        Temp temp = new Temp();
        m.emit(A_MOV(temp, m.munch(c.get(_r_))));
        m.emit(A_ADD(c.get(_i_), temp));
        return temp;
      }
    });
    
    em.add(new MunchRule<IRExp, Temp>(MINUS(_l_, CONST(_i_))) {
      @Override
      protected Temp trigger(Muncher m, Matched match) {
        Temp temp = new Temp();
        m.emit(A_MOV(temp, m.munch(match.get(_l_))));
        m.emit(A_SUB(match.get(_i_), temp));
        return temp;
      }
    });
    
    em.add(new MunchRule<IRExp, Temp>(MINUS(CONST(_i_), _r_)) {
      @Override
      protected Temp trigger(Muncher m, Matched match) {
        Temp temp = new Temp();
        m.emit(A_MOV(temp, match.get(_i_)));
        m.emit(A_SUB(temp, m.munch(match.get(_r_))));
        return temp;
      }
    });
    
    em.add(new MunchRule<IRExp, Temp>(MUL(CONST(_i_), _r_)) {
      @Override
      protected Temp trigger(Muncher m, Matched match) {
        Temp temp = new Temp();
        m.emit(A_MOV(temp, m.munch(match.get(_r_))));
        m.emit(A_IMUL(match.get(_i_), temp));
        return temp;
      }
    });
    
    em.add(new MunchRule<IRExp, Temp>(PLUS(MEM(PLUS(_l_, CONST(_i_))), _r_)) {
      @Override
      protected Temp trigger(Muncher m, Matched c) {
        Temp temp = new Temp();
        m.emit(A_MOV(temp, m.munch(c.get(_r_))));
        m.emit(A_ADD(c.get(_i_), m.munch(c.get(_l_)), temp));
        return temp;
      }
    });
    
    em.add(new MunchRule<IRExp, Temp>(MEM(PLUS(_l_, CONST(_i_)))) {
      @Override
      protected Temp trigger(Muncher m, Matched c) {
        Temp temp = new Temp();
        m.emit(A_MOV_FROM_MEM(c.get(_i_), m.munch(c.get(_l_)), temp));
        return temp;
      }
    });
    
    em.add(new MunchRule<IRExp, Temp>(MEM(MINUS(_l_, CONST(_i_)))) {
      @Override
      protected Temp trigger(Muncher m, Matched c) {
        Temp temp = new Temp();
        m.emit(A_MOV_FROM_MEM(-1 * c.get(_i_), m.munch(c.get(_l_)), temp));
        return temp;
      }
    });
  }

  // /////// Helper methods to generate X86 assembly instructions
  // //////////////////////////////////////

  private static Instr A_ADD(Temp dst, Temp src) {
    return new A_OPER("addq    `s0, `d0", list(dst), list(src, dst));
  }

  private static Instr A_ADD(int c, Temp dst) {
    return new A_OPER("addq    $" + c + ", `d0", list(dst), noTemps);
  }
  
  private static Instr A_ADD(int offset, Temp ptr, Temp dst) {
    return new A_OPER("addq    " + offset + "(`s0), `d0", list(dst), list(ptr));
  }
  
  private static Instr A_AND(Temp dst, Temp src) {
    return new A_OPER("andq    `s0, `d0", list(dst), list(src));
  }
  
  private static Instr A_CALL(Label fun, int nargs) {
    List<Temp> args = List.empty();
    for (int i = 0; i < Math.min(arguments.size(), nargs); ++i) {
      args.add(arguments.get(i));
    }
    return new A_OPER("call    " + fun, callerSave.append(arguments),
        special.append(args));
  }

  private static Instr A_CJUMP(RelOp relOp, Label thn, Label els) {
    String opCode;
    switch (relOp) {
      case EQ:
        opCode = "je ";
        break;
      case NE:
        opCode = "jne";
        break;
      case GE:
        opCode = "jge";
        break;
      case LT:
        opCode = "jl ";
        break;
      case LE:
        opCode = "jle";
        break;
      case GT:
        opCode = "jg";
        break;
      case ULT:
        opCode = "jb";
        break;
      case UGT:
        opCode = "ja";
        break;
      case ULE:
        opCode = "jbe";
        break;
      case UGE:
        opCode = "jae";
        break;
      default:
        throw new Error("Missing case?");
    }
    return new A_OPER(opCode + "     `j0", noTemps, noTemps, list(thn, els));
  }

  private static Instr A_CMP(Temp l, Temp r) {
    return new A_OPER("cmpq    `s1, `s0", noTemps, list(l, r));
  }
  
  private static Instr A_CMP(Temp l, int r) {
    return new A_OPER("cmpq    $" + r + ", `s0", noTemps, list(l));
  }

  private static Instr A_CMP_FROM_MEM(Temp ptr, Temp dst) {
    return new A_OPER("cmpq    (`s1), `s0", noTemps, list(dst, ptr));
  }
  
  private static Instr A_CMP_FROM_MEM(int offset, Temp ptr, Temp dst) {
    return new A_OPER("cmpq    " + offset + "(`s1), `s0", noTemps, list(dst, ptr));
  }
  
  private static Instr A_IMUL(Temp dst, Temp src) {
    return new A_OPER("imulq   `s0, `d0", list(dst), list(src, dst));
  }
  
  private static Instr A_IMUL(int c, Temp dst) {
    return new A_OPER("imulq    $" + c + ", `d0", list(dst), noTemps);
  }

  private static Instr A_IDIV(Temp dst, Temp src) {
    return new A_OPER("movq    `d0, %rax\n" + "   cqto\n" + "   idivq   `s0\n"
        + "   movq    %rax, `d0", list(dst, RAX, RDX), list(src, dst));
  }

  private static Instr A_JMP(Label target) {
    return new A_OPER("jmp     `j0", noTemps, noTemps, List.list(target));
  }

  private static Instr A_LABEL(Label name) {
    return new A_LABEL(name + ":", name);
  }

  private static Instr A_MOV(Temp t, int value) {
    String instruction = value == 0 ? "xorq    `d0, `d0"
                                    : "movq    $" + value + ", `d0";
    return new A_OPER(instruction, list(t), noTemps);
  }

  private static Instr A_MOV(Temp d, Temp s) {
    return new A_MOVE("movq    `s0, `d0", d, s);
  }
  
  private static Instr A_CMOV(RelOp relOp, Temp d, Temp s) {
    String opCode;
    switch (relOp) {
      case EQ:
        opCode = "cmove ";
        break;
      case NE:
        opCode = "cmovne";
        break;
      case GE:
        opCode = "cmovge";
        break;
      case LT:
        opCode = "cmovl";
        break;
      case LE:
        opCode = "cmovle";
        break;
      case GT:
        opCode = "cmovg";
        break;
      case ULT:
        opCode = "cmovb";
        break;
      case UGT:
        opCode = "cmova";
        break;
      case ULE:
        opCode = "cmovbe";
        break;
      case UGE:
        opCode = "cmovae";
        break;
      default:
        throw new Error("Missing case?");
    }

    return new A_OPER(opCode + "    `s0, `d0", list(d), list(s, d));
  }

  private static Instr A_MOV_TO_MEM(Temp ptr, Temp s) {
    return new A_OPER("movq    `s1, (`s0)", noTemps, list(ptr, s));
  }

  private static Instr A_MOV_TO_MEM(int offset, Temp ptr, Temp src) {
    return new A_OPER("movq    `s1, " + offset + "(`s0)", noTemps, list(ptr, src));
  }
  
  private static Instr A_MOV_FROM_MEM(Temp d, Temp ptr) {
    return new A_OPER("movq    (`s0), `d0", list(d), list(ptr));
  }
  
  private static Instr A_MOV_FROM_MEM(int offset, Temp ptr, Temp dst) {
    return new A_OPER("movq    " + offset + "(`s0), `d0", list(dst), list(ptr));
  }

  private static Instr A_SUB(Temp dst, Temp src) {
    return new A_OPER("subq    `s0, `d0", list(dst), list(src, dst));
  }

  private static Instr A_SUB(int c, Temp dst) {
    return new A_OPER("subq    $" + c + ", `d0", list(dst), noTemps);
  }
  
  public static void dumpRules() {
    System.out.println("StmMunchers: " + sm);
    System.out.println("ExpMunchers: " + em);
  }
}
