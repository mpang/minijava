package translate.implementation;

import static ir.tree.IR.CMOVE;
import static ir.tree.IR.ESEQ;
import static ir.tree.IR.FALSE;
import static ir.tree.IR.MOVE;
import static ir.tree.IR.SEQ;
import static ir.tree.IR.TEMP;
import static ir.tree.IR.TRUE;
import ir.frame.Access;
import ir.frame.Frame;
import ir.temp.Label;
import ir.temp.Temp;
import ir.tree.BINOP.Op;
import ir.tree.CJUMP.RelOp;
import ir.tree.IR;
import ir.tree.IRStm;
import ir.tree.TEMP;

import java.util.ArrayDeque;
import java.util.Deque;

import translate.Fragments;
import translate.Translator;
import typechecker.implementation.SymbolTable;
import util.FunTable;
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

	public TranslateVisitor(SymbolTable table, Frame frameFactory) {
		this.frags = new Fragments(frameFactory);
		this.frameFactory = frameFactory;
		frames = new ArrayDeque<Frame>();
		envs = new ArrayDeque<FunTable<Access>>();
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
		IRStm result = IR.NOP;
		for (int i = 0; i < ns.size(); i++) {
			AST nextStm = ns.elementAt(i);
			result = IR.SEQ(result, nextStm.accept(this).unNx());
		}
		return new Nx(result);
	}

	@Override
	public TRExp visit(Program n) {
	  throw new Error("Not implemented");
	}

	@Override
	public TRExp visit(BooleanType n) {
		throw new Error("Not implemented");
	}

	@Override
	public TRExp visit(IntegerType n) {
		throw new Error("Not implemented");
	}

	@Override
	public TRExp visit(Print n) {
		TRExp arg = n.exp.accept(this);
		return new Ex(IR.CALL(Translator.L_PRINT, arg.unEx()));
	}

	@Override
	public TRExp visit(Assign n) {
	  throw new Error("Not implemented");
	}

  @Override
  public TRExp visit(LessThan n) {
    TRExp l = n.e1.accept(this);
    TRExp r = n.e2.accept(this);

    TEMP v = TEMP(new Temp());
    return new Ex(ESEQ( SEQ( 
        MOVE(v, FALSE),
        CMOVE(RelOp.LT, l.unEx(), r.unEx(), v, TRUE)),
        v));
  }
  
	//////////////////////////////////////////////////////////////

	private TRExp numericOp(Op op, Expression e1, Expression e2) {
		TRExp l = e1.accept(this);
		TRExp r = e2.accept(this);
		return new Ex(IR.BINOP(op, l.unEx(), r.unEx()));
	}

	@Override
	public TRExp visit(Plus n) {
		return numericOp(Op.PLUS,n.e1,n.e2);
	}

	@Override
	public TRExp visit(Minus n) {
		return numericOp(Op.MINUS,n.e1,n.e2);
	}

	@Override
	public TRExp visit(Times n) {
		return numericOp(Op.MUL,n.e1,n.e2);
	}

	//////////////////////////////////////////////////////////////////

	@Override
	public TRExp visit(IntegerLiteral n) {
		return new Ex(IR.CONST(n.value));
	}

	@Override
	public TRExp visit(IdentifierExp n) {
	  Frame frame = frames.peek();
	  Access var = envs.peek().lookup(n.name);
	  if (frame.equals(frames.peekLast()) || 
		(var == null && !frame.equals(frames.peekLast()))) {
		// Global variable lookup
	    return new Ex(IR.MEM(IR.NAME(Label.get(n.name))));
	  } else {
		// Local variable lookup
	    return new Ex(var.exp(frame.FP()));
      }
	}

	@Override
	public TRExp visit(Not n) {
		final TRExp negated = n.e.accept(this);
		return new Ex(IR.BINOP(Op.MINUS, IR.CONST(1), negated.unEx()));
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
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(ClassDecl n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(MethodDecl n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(VarDecl n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(IntArrayType n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(ObjectType n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(Block n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(If n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(While n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(ArrayAssign n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(BooleanLiteral n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(And n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(ArrayLength n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(ArrayLookup n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(Call n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(NewArray n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(NewObject n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(This n) {
    throw new Error("Not implemented");
  }
}
