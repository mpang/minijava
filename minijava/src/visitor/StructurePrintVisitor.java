package visitor;

import java.io.PrintWriter;

import util.IndentingWriter;
import ast.*;

/**
 * This prints the structure of an AST, showing its hierarchical relationships.
 * <p>
 * This version is also cleaned up to actually produce *properly* indented
 * output.
 * 
 * @author norm
 */
public class StructurePrintVisitor implements Visitor<Void> {

	/**
	 * Where to send out.print output.
	 */
	private IndentingWriter out;

	public StructurePrintVisitor(PrintWriter out) {
		this.out = new IndentingWriter(out);
	}

	///////////// Visitor methods /////////////////////////////////////////

	@Override
	public Void visit(Program n) {
	  out.println("Program");
	  out.indent();
	  n.mainClass.accept(this);
	  n.classes.accept(this);
	  out.outdent();
	  return null;
	}

	@Override
	public Void visit(BooleanType n) {
		out.println("BooleanType");
		return null;
	}

	@Override
	public Void visit(IntegerType n) {
		out.println("IntegerType");
		return null;
	}

	@Override
	public Void visit(Print n) {
		out.println("Print");
		out.indent();
		n.exp.accept(this);
		out.outdent();
		return null;
	}

	@Override
	public Void visit(Assign n) {
	  out.println("Assign");
	  out.indent();
	  out.println(n.name);
	  n.value.accept(this);
	  out.outdent();
	  return null;
	}

	@Override
	public Void visit(LessThan n) {
		out.println("LessThan");
		out.indent();
		n.e1.accept(this);
		n.e2.accept(this);
		out.outdent();
		return null;
	}

	@Override
	public Void visit(Plus n) {
		out.println("Plus");
		out.indent();
		n.e1.accept(this);
		n.e2.accept(this);
		out.outdent();
		return null;
	}

	@Override
	public Void visit(Minus n) {
		out.println("Minus");
		out.indent();
		n.e1.accept(this);
		n.e2.accept(this);
		out.outdent();
		return null;
	}

	@Override
	public Void visit(Times n) {
		out.println("Times");
		out.indent();
		n.e1.accept(this);
		n.e2.accept(this);
		out.outdent();
		return null;
	}

	@Override
	public Void visit(IntegerLiteral n) {
		out.println("IntegerLiteral " + n.value);
		return null;
	}

	@Override
	public Void visit(IdentifierExp n) {
		out.println("IdentifierExp " + n.name);
		return null;
	}

	@Override
	public Void visit(Not n) {
		out.println("Not");
		out.indent();
		n.e.accept(this);
		out.outdent();
		return null;
	}

	@Override
	public <T extends AST> Void visit(NodeList<T> nodes) {
		for (int i = 0; i < nodes.size(); i++) {
			nodes.elementAt(i).accept(this);
		}
		return null;
	}

  @Override
  public Void visit(MainClass n) {
    out.println("MainClass");
    out.indent();
    out.println(n.className);
    out.println(n.argName);
    n.statement.accept(this);
    return null;
  }

  @Override
  public Void visit(ClassDecl n) {
    out.println("ClassDecl");
    out.indent();
    out.println(n.name);
    if (!n.superName.isEmpty()) {
      out.println(n.superName);
    }
    n.vars.accept(this);
    n.methods.accept(this);
    out.outdent();
    return null;
  }

  @Override
  public Void visit(MethodDecl n) {
    out.println("MethodDecl");
    out.indent();
    n.returnType.accept(this);
    out.println(n.name);
    out.println("Formals");
    out.indent();
    n.formals.accept(this);
    out.outdent();
    n.vars.accept(this);
    n.statements.accept(this);
    n.returnExp.accept(this);
    out.outdent();
    return null;
  }

  @Override
  public Void visit(VarDecl n) {
    out.println("VarDecl");
    out.indent();
    out.println(n.kind.toString());
    n.type.accept(this);
    out.println(n.name);
    out.outdent();
    return null;
  }

  @Override
  public Void visit(IntArrayType n) {
    out.println("IntArrayType");
    return null;
  }

  @Override
  public Void visit(ObjectType n) {
    out.println("ObjectType " + n.name);
    return null;
  }

  @Override
  public Void visit(Block n) {
    out.println("Block");
    out.indent();
    n.statements.accept(this);
    out.outdent();
    return null;
  }

  @Override
  public Void visit(If n) {
    out.println("If");
    out.indent();
    n.tst.accept(this);
    n.thn.accept(this);
    n.els.accept(this);
    out.outdent();
    return null;
  }

  @Override
  public Void visit(While n) {
    out.println("While");
    out.indent();
    n.tst.accept(this);
    n.body.accept(this);
    out.outdent();
    return null;
  }

  @Override
  public Void visit(ArrayAssign n) {
    out.println("ArrayAssign");
    out.indent();
    out.println(n.name);
    n.index.accept(this);
    n.value.accept(this);
    out.outdent();
    return null;
  }

  @Override
  public Void visit(BooleanLiteral n) {
    out.println("BooleanLiteral " + n.value);
    return null;
  }

  @Override
  public Void visit(And n) {
    out.println("And");
    out.indent();
    n.e1.accept(this);
    n.e2.accept(this);
    return null;
  }

  @Override
  public Void visit(ArrayLength n) {
    out.println("ArrayLength");
    out.indent();
    n.array.accept(this);
    out.outdent();
    return null;
  }

  @Override
  public Void visit(ArrayLookup n) {
    out.println("ArrayLookup");
    out.indent();
    n.array.accept(this);
    n.index.accept(this);
    out.outdent();
    return null;
  }

  @Override
  public Void visit(Call n) {
    out.println("Call");
    out.indent();
    n.receiver.accept(this);
    out.println(n.name);
    n.rands.accept(this);
    out.outdent();
    return null;
  }

  @Override
  public Void visit(NewArray n) {
    out.println("NewArray");
    out.indent();
    n.size.accept(this);
    out.outdent();
    return null;
  }

  @Override
  public Void visit(NewObject n) {
    out.println("NewObject " + n.typeName);
    return null;
  }

  @Override
  public Void visit(This n) {
    out.println("This");
    return null;
  }
}
