package translate.implementation;

import static ir.tree.IR.*;
import static translate.Translator.L_MAIN;
import ir.frame.Access;
import ir.frame.Frame;
import ir.temp.Label;
import ir.temp.Temp;
import ir.tree.BINOP.Op;
import ir.tree.CJUMP.RelOp;
import ir.tree.IR;
import ir.tree.IRExp;
import ir.tree.IRStm;
import ir.tree.TEMP;
import translate.Fragments;
import translate.ProcFragment;
import translate.Translator;
import typechecker.implementation.SymbolTable;
import util.FunTable;
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
	private Frame frame;
	private FunTable<Access> currentEnv;

	public TranslateVisitor(SymbolTable table, Frame frameFactory) {
		this.frags = new Fragments(frameFactory);
		this.frameFactory = frameFactory;
	}

	/////// Helpers //////////////////////////////////////////////

	/**
	 * Create a frame with a given number of formals.
	 */
	private Frame newFrame(Label name, int formals) {
		return frameFactory.newFrame(name, formals);
	}

	private void putEnv(String name, Access access) {
		currentEnv = currentEnv.insert(name, access);
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
		frame = newFrame(L_MAIN, 0);
		currentEnv = FunTable.theEmpty();
		TRExp statements = n.statements.accept(this);
		TRExp print = n.print.accept(this);
		IRStm body = IR.SEQ(
				statements.unNx(),
				print.unNx());
//		body = frame.procEntryExit1(body);
		frags.add(new ProcFragment(frame, body));
		return null;
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
	public TRExp visit(UnknownType n) {
		throw new Error("Not implemented");
	}

	@Override
	public TRExp visit(Print n) {
		TRExp arg = n.exp.accept(this);
		return new Ex(IR.CALL(Translator.L_PRINT, arg.unEx()));
	}

	@Override
	public TRExp visit(Assign n) {
		Access var = frame.allocLocal(false);
		putEnv(n.name, var);
		TRExp val = n.value.accept(this);
		return new Nx(IR.MOVE(var.exp(frame.FP()), val.unEx()));
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
		Access var = currentEnv.lookup(n.name);
		return new Ex(var.exp(frame.FP()));
	}

	@Override
	public TRExp visit(Not n) {
		final TRExp negated = n.e.accept(this);
		return new Ex(IR.BINOP(Op.MINUS, IR.CONST(1), negated.unEx()));
//		return new Cx() {
//			@Override
//			IRStm unCx(Label ifTrue, Label ifFalse) {
//				return negated.unCx(ifFalse, ifTrue);
//			}
//		};
	}

	/**
	 * After the visitor successfully traversed the program, 
	 * retrieve the built-up list of Fragments with this method.
	 */
	public Fragments getResult() {
		return frags;
	}

	@Override
	public TRExp visit(Conditional n) {
		TRExp c = n.e1.accept(this);
		TRExp t = n.e2.accept(this);
		TRExp f = n.e3.accept(this);

		TEMP v = TEMP(new Temp());
		Label trueLabel = Label.gen();
		Label falseLabel = Label.gen();
		Label jointLabel = Label.gen(); // to prevent from executing false label after executing true label
		
		return new Ex(ESEQ(SEQ(c.unCx(trueLabel, falseLabel),
		                       SEQ(LABEL(trueLabel),
                               MOVE(v, t.unEx()),
                               IR.JUMP(jointLabel)),
                           SEQ(LABEL(falseLabel),
                               MOVE(v, f.unEx())),
                           LABEL(jointLabel)),
		                   v));
	}

  @Override
  public TRExp visit(FunctionDeclaration n) {
    Frame oldFrame = frame;
    frame = newFrame(Label.get(n.name), n.parameters.parameters.size());
    IRExp exp = null;
    
    if (n.statements.size() > 0) {
      IRStm stm = NOP;
      for (int i = 0; i < n.statements.size(); i++) {
        stm = SEQ(stm, n.statements.elementAt(i).accept(this).unNx());
      }
      
      exp = ESEQ(stm, n.returnExpression.accept(this).unEx());
    }
    else {
      exp = n.returnExpression.accept(this).unEx();
    }
    
    frags.add(new ProcFragment(frame, frame.procEntryExit1(MOVE(frame.RV(), exp))));
    frame = oldFrame;
    return new Ex(exp);
  }

  @Override
  public TRExp visit(FunctionCallExp n) {
    List<IRExp> args = List.empty();
    for (int i = 0; i < n.arguments.expressions.size(); i++) {
      args.add(n.arguments.expressions.elementAt(i).accept(this).unEx());
    }
    return new Ex(CALL(Label.get(n.name), args));
  }

  @Override
  public TRExp visit(FormalList n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(ExpressionList n) {
    throw new Error("Not implemented");
  }

  @Override
  public TRExp visit(ParameterDeclaration n) {
    throw new Error("Not implemented");
  }
}
