package translate.implementation;

import static ir.tree.IR.*;
import static translate.Translator.L_MAIN;
import ir.frame.Access;
import ir.frame.Frame;
import ir.temp.Label;
import ir.temp.Temp;
import ir.tree.BINOP.Op;
import ir.tree.CJUMP.RelOp;
import ir.tree.IRExp;
import ir.tree.IRStm;
import ir.tree.TEMP;

import java.util.ArrayDeque;
import java.util.Deque;

import translate.Fragments;
import translate.ProcFragment;
import translate.Translator;
import typechecker.implementation.ClassEntry;
import util.FunTable;
import util.ImpTable;
import util.List;
import visitor.Visitor;
import ast.*;

/**
 * This visitor builds up a collection of IRTree code fragments for the body
 * of methods in a minijava program.
 * <p>
 * Methods that visit statements and expression return a TRExp, other methods 
 * just return null, but they may add Fragments to the collection by means
 * of a side effect.
 * 
 * @author kdvolder
 */
public class TranslateVisitor implements Visitor<TRExp> {

	/**
	 * We build up a list of Fragment (pieces of stuff to be converted into
	 * assembly) here.
	 */
	private Fragments frags;

	/**
	 * We use this factory to create Frame's, without making our code dependent
	 * on the target architecture.
	 */
	private Frame frameFactory;
	private Deque<Frame> frames; // stack of frames
	private Deque<FunTable<Access>> envs; // stack of envs to preserve scoping

	private ImpTable<ClassEntry> table; 
	private ClassEntry currentClass;

	public TranslateVisitor(ImpTable<ClassEntry> table, Frame frameFactory) {
		frags = new Fragments(frameFactory);
		frames = new ArrayDeque<Frame>();
		envs = new ArrayDeque<FunTable<Access>>();
		this.frameFactory = frameFactory;
    this.table = table;
    
	}

	/////// Helpers //////////////////////////////////////////////

	/**
	 * Create a frame with a given number of formals.
	 */
	private Frame newFrame(Label name, int formals) {
		return frameFactory.newFrame(name, formals);
	}

	private void putEnv(String name, Access access) {
	  envs.push(envs.pop().insert(name, access));
	}

	////// Visitor ///////////////////////////////////////////////

	@Override
	public <T extends AST> TRExp visit(NodeList<T> ns) {
		IRStm result = NOP;
		for (int i = 0; i < ns.size(); i++) {
			AST nextStm = ns.elementAt(i);
			result = SEQ(result, nextStm.accept(this).unNx());
		}
		return new Nx(result);
	}

	

	@Override
	public TRExp visit(Program n) {
	  n.mainClass.accept(this);
	  for (ClassDecl clazz : n.classes) {
	    clazz.accept(this);
	  }
	  return new Nx(NOP);
	}

	@Override
	public TRExp visit(BooleanType n) {
	  return new Nx(NOP);
	}

	@Override
	public TRExp visit(IntegerType n) {
	  return new Nx(NOP);
	}

	@Override
	public TRExp visit(Print n) {
		return new Ex(CALL(Translator.L_PRINT, n.exp.accept(this).unEx()));
	}

	@Override
	public TRExp visit(Assign n) {
	  return new Nx(MOVE(new IdentifierExp(n.name).accept(this).unEx(), n.value.accept(this).unEx()));
	}

  @Override
  public TRExp visit(LessThan n) {
    TRExp l = n.e1.accept(this);
    TRExp r = n.e2.accept(this);
    TEMP v = TEMP(new Temp());
    return new Ex(ESEQ(SEQ(MOVE(v, FALSE),
                           CMOVE(RelOp.LT, l.unEx(), r.unEx(), v, TRUE)),
                       v));
  }
  
	//////////////////////////////////////////////////////////////

	private TRExp numericOp(Op op, Expression e1, Expression e2) {
		return new Ex(BINOP(op, e1.accept(this).unEx(), e2.accept(this).unEx()));
	}

	@Override
	public TRExp visit(Plus n) {
		return numericOp(Op.PLUS, n.e1, n.e2);
	}

	@Override
	public TRExp visit(Minus n) {
		return numericOp(Op.MINUS, n.e1, n.e2);
	}

	@Override
	public TRExp visit(Times n) {
		return numericOp(Op.MUL, n.e1, n.e2);
	}
	
	@Override
  public TRExp visit(And n) {
    return numericOp(Op.AND, n.e1, n.e2);
  }
	
	@Override
  public TRExp visit(Not n) {
    return numericOp(Op.MINUS, new IntegerLiteral(1), n.e);
  }

	//////////////////////////////////////////////////////////////////

	@Override
	public TRExp visit(IntegerLiteral n) {
		return new Ex(CONST(n.value));
	}

	@Override
	public TRExp visit(IdentifierExp n) {
	  Frame frame = frames.peek();
	  Access var = envs.peek().lookup(n.name);
	  
	  if (var == null) {
	    int offset = currentClass.getOffsetOfField(n.name);
	    return new Ex(MEM(PLUS(new This().accept(this).unEx(), offset * frame.wordSize())));
	  }
	  
    return new Ex(var.exp(frame.FP()));
	}

	/**
	 * After the visitor successfully traversed the program, 
	 * retrieve the built-up list of Fragments with this method.
	 */
	public Fragments getResult() {
		return frags;
	}

  @Override
  public TRExp visit(MainClass n) {
  	Frame mainFrame = newFrame(L_MAIN, 1);
    frames.push(mainFrame);
    envs.push(FunTable.<Access>theEmpty());
    currentClass = table.lookup(n.className);
    
    frags.add(new ProcFragment(mainFrame, mainFrame.procEntryExit1(n.statement.accept(this).unNx())));
    envs.pop();
    frames.pop();
    currentClass = null;
    return new Nx(NOP);
  }

  @Override
  public TRExp visit(ClassDecl n) {
    currentClass = table.lookup(n.name);
    for (MethodDecl method : n.methods) {
      method.accept(this);
    }
    currentClass = null;
    return new Nx(NOP);
  }

  @Override
  public TRExp visit(MethodDecl n) {
    // need one extra argument for the receiver object
    Frame frame = newFrame(Label.get(currentClass.className + "$" + n.name), n.formals.size() + 1);
    envs.push(FunTable.<Access>theEmpty());
    frames.push(frame); 

    // params
    for (int i = 0; i < n.formals.size(); i++) {
      // first position is reserved for receiver object
      putEnv(n.formals.elementAt(i).name, frame.getFormal(i + 1));
    }
    
    // locals
    for (VarDecl local : n.vars) {
      putEnv(local.name, frame.allocLocal(false));
    }
  
    // body
    IRExp exp = n.statements.size() > 0 ? ESEQ(n.statements.accept(this).unNx(),
                                               n.returnExp.accept(this).unEx())
                                        : n.returnExp.accept(this).unEx();
    
    frags.add(new ProcFragment(frame, frame.procEntryExit1(MOVE(frame.RV(), exp))));
    frames.pop();
    envs.pop();
    
    return new Nx(NOP);
  }

  @Override
  public TRExp visit(VarDecl n) {
    return new Nx(NOP);
  }

  @Override
  public TRExp visit(IntArrayType n) {
    return new Nx(NOP);
  }

  @Override
  public TRExp visit(ObjectType n) {
    return new Nx(NOP);
  }

  @Override
  public TRExp visit(Block n) {
    return n.statements.accept(this);
  }

  @Override
  public TRExp visit(If n) {
    return new IfThenElse(n.tst.accept(this), n.thn.accept(this), n.els.accept(this));
  }

  @Override
  public TRExp visit(While n) {
    Label test = Label.gen();
    Label body = Label.gen();
    Label done = Label.gen();
    return new Nx(SEQ(LABEL(test),
                      n.tst.accept(this).unCx(body, done),
                      LABEL(body),
                      n.body.accept(this).unNx(),
                      JUMP(test),
                      LABEL(done)));
  }

  @Override
  public TRExp visit(ArrayAssign n) {
    return new Nx(MOVE(MEM(PLUS(new IdentifierExp(n.name).accept(this).unEx(),
                                MUL(n.index.accept(this).unEx(), frames.peek().wordSize()))),
                       n.value.accept(this).unEx()));
  }

  @Override
  public TRExp visit(BooleanLiteral n) {
    return new Ex(n.value ? TRUE : FALSE);
  }

  @Override
  public TRExp visit(ArrayLength n) {
    return new Ex(MEM(MINUS(n.array.accept(this).unEx(), frames.peek().wordSize())));
  }

  @Override
  public TRExp visit(ArrayLookup n) {
    return new Ex(MEM(PLUS(n.array.accept(this).unEx(),
                           MUL(n.index.accept(this).unEx(), frames.peek().wordSize()))));
  }

  @Override
  public TRExp visit(Call n) {
    List<IRExp> args = List.list(n.receiver.accept(this).unEx());
    for (Expression arg : n.rands) {
      args.add(arg.accept(this).unEx());
    }
    return new Ex(CALL(Label.get(n.receiver.getType().toString() + "$" + n.name), args));
  }

  @Override
  public TRExp visit(NewArray n) {
    return new Ex(CALL(Translator.L_NEW_ARRAY, n.size.accept(this).unEx()));
  }

  @Override
  public TRExp visit(NewObject n) {
    ClassEntry clazz = table.lookup(n.typeName);
    int numBytes = 0;
    while (clazz != null) {
      numBytes += clazz.getNumOfFields() * frames.peek().wordSize();
      clazz = clazz.getSuperClass();
    }
    return new Ex(CALL(Translator.L_NEW_OBJECT, CONST(numBytes)));
  }

  @Override
  public TRExp visit(This n) {
    Access var = frames.peek().getFormal(0);
    return new Ex(var.exp(frames.peek().FP()));
  }
}
