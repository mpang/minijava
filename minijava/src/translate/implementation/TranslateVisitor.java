package translate.implementation;

import static ir.tree.IR.*;
import static translate.Translator.L_ERROR;
import static translate.Translator.L_MAIN;
import static translate.Translator.L_NEW_ARRAY;
import static translate.Translator.L_NEW_OBJECT;
import static translate.Translator.L_PRINT;
import ir.frame.Access;
import ir.frame.Frame;
import ir.temp.Label;
import ir.temp.Temp;
import ir.tree.BINOP.Op;
import ir.tree.CJUMP.RelOp;
import ir.tree.IRData;
import ir.tree.IRExp;
import ir.tree.IRStm;
import ir.tree.NAME;
import ir.tree.TEMP;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import translate.DataFragment;
import translate.Fragment;
import translate.Fragments;
import translate.ProcFragment;
import typechecker.implementation.ClassEntry;
import util.FunTable;
import util.ImpTable;
import util.List;
import util.Utils;
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
	
	private static final String OBJECT_CLASS = "Object";

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
		return new Ex(CALL(L_PRINT, n.exp.accept(this).unEx()));
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
	    return new Ex(MEM(PLUS(new This().accept(this).unEx(), (offset + 1) * frame.wordSize())));
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
    currentClass = table.lookup(n.className);
    
    frags.add(new DataFragment(mainFrame, DATA(Label.get(OBJECT_CLASS), List.list(CONST(0)))));
    frags.add(new ProcFragment(mainFrame, mainFrame.procEntryExit1(n.statement.accept(this).unNx())));
    
    currentClass = null;
    return new Nx(NOP);
  }

  @Override
  public TRExp visit(ClassDecl n) {
    currentClass = table.lookup(n.name);
    // add superclass label
    List<IRExp> methods = List.list(NAME(Label.get(n.superName.isEmpty() ? OBJECT_CLASS : n.superName)));
    
    // building superclass virtual method table
    if (!n.superName.isEmpty()) {
      for (Fragment fragment : frags) {
        if (fragment instanceof DataFragment) {
          IRData data = ((DataFragment) fragment).getBody();
          
          if (data.getLabel().toString().equals(Label.get(n.superName).toString())) {
            Iterator<IRExp> iterator = data.iterator();
            iterator.next();  // ignore superclass's superclass label
            while (iterator.hasNext()) {
              methods = methods.append(List.list(iterator.next()));
            }
          }
        }
      }
    }
    
    // add this class's methods
    for (MethodDecl method : n.methods) {
      method.accept(this);
      IRExp methodExp = NAME(Label.get(n.name + "_" + method.name));
      boolean isOverriden = false;
      
      if (!n.superName.isEmpty()) {
        // try to find overriden methods, if any
        // start from tail to ignore superclass's superclass label
        for (IRExp exp : methods.tail()) {
          String superMethodName = ((NAME) exp).label.toString().split("_")[Utils.macOS() ? 2 : 1];
          if (superMethodName.equals(method.name)) {
            methods = methods.replace(exp, methodExp);
            isOverriden = true;
            break;
          }
        }
      }
      
      if (!isOverriden) {
        methods = methods.append(List.list(methodExp));
      }
    }
    
    frags.add(new DataFragment(frames.peek(), DATA(Label.get(n.name), methods)));
    currentClass = null;
    return new Nx(NOP);
  }

  @Override
  public TRExp visit(MethodDecl n) {
    // need one extra argument for the receiver object
    Frame frame = newFrame(Label.get(currentClass.className + "_" + n.name), n.formals.size() + 1);
    envs.push(FunTable.<Access>theEmpty());
    frames.push(frame); 

    // params
    for (int i = 0; i < n.formals.size(); i++) {
      // first position is reserved for receiver object
      putEnv(n.formals.elementAt(i).name, frame.getFormal(i + 1));
    }
    
    IRStm inits = NOP;
    // locals
    for (VarDecl local : n.vars) {
      Access var = frame.allocLocal(false);
      putEnv(local.name, var);
      // initialize local variables to 0
      inits = SEQ(inits, MOVE(var.exp(frame.FP()), CONST(0)));
    }
  
    // body
    IRExp exp = ESEQ(SEQ(inits, n.statements.accept(this).unNx()),
                     n.returnExp.accept(this).unEx());
    
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
    IdentifierExp array = new IdentifierExp(n.name);
    return new Nx(new IfThenElse(new LessThan(n.index, new ArrayLength(array)).accept(this),
                                 new Nx(MOVE(MEM(PLUS(array.accept(this).unEx(),
                                                      MUL(n.index.accept(this).unEx(),
                                                          frames.peek().wordSize()))),
                                             n.value.accept(this).unEx())),
                                 new Ex(CALL(L_ERROR, INDEX_OUT_OF_BOUND))).unNx());
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
    return new Ex(new IfThenElse(new LessThan(n.index, new ArrayLength(n.array)).accept(this),
                                 new Ex(MEM(PLUS(n.array.accept(this).unEx(),
                                                 MUL(n.index.accept(this).unEx(),
                                                     frames.peek().wordSize())))),
                                 new Ex(CALL(L_ERROR, INDEX_OUT_OF_BOUND))).unEx());
  }

  @Override
  public TRExp visit(Call n) {
    List<IRExp> args = List.list(n.receiver.accept(this).unEx());
    for (Expression arg : n.rands) {
      args.add(arg.accept(this).unEx());
    }
    
    // + 1 to ignore the superclass label
    int methodOffset = table.lookup(n.receiver.getType().toString()).getOffsetOfMethod(n.name) + 1;
    // there should be an uniform way of dealing with this and super in terms of method address
    // but this will do for now
    IRExp vmt = (n.receiver instanceof Super) ? MEM(n.receiver.accept(this).unEx())
                                              : n.receiver.accept(this).unEx();
    return new Ex(new IfThenElse(new Ex(n.receiver.accept(this).unEx()),
                                 new Ex(CALL(MEM(PLUS(MEM(vmt),
                                                      methodOffset * frames.peek().wordSize())),
                                             args)),
                                 new Ex(CALL(L_ERROR, NULL_OBJECT_REFERENCE))).unEx());
  }

  @Override
  public TRExp visit(NewArray n) {
    return new Ex(CALL(L_NEW_ARRAY, n.size.accept(this).unEx()));
  }

  @Override
  public TRExp visit(NewObject n) {
    ClassEntry clazz = table.lookup(n.typeName);
    int numBytes = frames.peek().wordSize();
    while (clazz != null) {
      numBytes += clazz.getNumOfFields() * frames.peek().wordSize();
      clazz = clazz.getSuperClass();
    }
    
    Temp temp = new Temp();
    return new Ex(ESEQ(SEQ(MOVE(temp, CALL(L_NEW_OBJECT, CONST(numBytes))),
                           MOVE(MEM(TEMP(temp)), NAME(Label.get(n.typeName)))),
                       TEMP(temp)));
  }

  @Override
  public TRExp visit(This n) {
    Frame frame = frames.peek();
    return new Ex(frame.getFormal(0).exp(frame.FP()));
  }

  @Override
  public TRExp visit(InstanceOf n) {
    IdentifierExp id = new IdentifierExp(n.identifier);
    Label t = Label.gen();
    Label f = Label.gen();
    Label body = Label.gen();
    Label test = Label.gen();
    Label join = Label.gen();
    TEMP classLabel = TEMP(new Temp());
    TEMP result = TEMP(new Temp());
    
    return new Ex(ESEQ(SEQ(MOVE(classLabel, MEM(id.accept(this).unEx())),
                           LABEL(test),
                           CJUMP(RelOp.EQ, classLabel, NAME(Label.get(n.className)), t, body),
                           LABEL(body),
                           MOVE(classLabel, MEM(classLabel)),
                           CJUMP(RelOp.EQ, classLabel, CONST(0), f, test),
                           LABEL(t),
                           MOVE(result, TRUE),
                           JUMP(join),
                           LABEL(f),
                           MOVE(result, FALSE),
                           LABEL(join)),
                       result));
  }

  @Override
  public TRExp visit(TypeCoercion n) {
    return new Ex(new IfThenElse(new InstanceOf(n.id, n.type).accept(this),
                                 new IdentifierExp(n.id).accept(this),
                                 new Ex(CALL(L_ERROR, INCOMPATIBLE_TYPE))).unEx());
  }

  @Override
  public TRExp visit(Super n) {
    return new This().accept(this);
  }
}
