package visitor;

import java.io.PrintWriter;

import util.IndentingWriter;
import ast.*;



/**
 * This is an adaptation of the PrettyPrintVisitor from the textbook
 * online material, but updated to work with the "modernized" 
 * Visitor and our own versions of the AST classes.
 * <p>
 * This version is also cleaned up to actually produce *properly* indented
 * output.
 * 
 * @author kdvolder
 */
public class PrettyPrintVisitor implements Visitor<Void> {

	/**
	 * Where to send out.print output.
	 */
	private IndentingWriter out;
	
	public PrettyPrintVisitor(PrintWriter out) {
		this.out = new IndentingWriter(out);
	}
	
	///////////// Visitor methods /////////////////////////////////////////

	@Override
	public Void visit(Program n) {
	  throw new Error("Not implemented");
	}

	@Override
	public Void visit(BooleanType n) {
		out.print("boolean");
		return null;
	}

	@Override
	public Void visit(IntegerType n) {
		out.print("int");
		return null;
	}

	@Override
	public Void visit(Print n) {
		out.print("print ");
		n.exp.accept(this);
		out.println();
		return null;
	}

	@Override
	public Void visit(Assign n) {
		out.print(n.name + " = ");
		n.value.accept(this);
		out.println(";");
		return null;
	}

	@Override
	public Void visit(LessThan n) {
		out.print("(");
		n.e1.accept(this);
		out.print(" < ");
		n.e2.accept(this);
		out.print(")");
		return null;
	}

	@Override
	public Void visit(Plus n) {
		out.print("(");
		n.e1.accept(this);
		out.print(" + ");
		n.e2.accept(this);
		out.print(")");
		return null;
	}

	@Override
	public Void visit(Minus n) {
		out.print("(");
		n.e1.accept(this);
		out.print(" - ");
		n.e2.accept(this);
		out.print(")");
		return null;
	}

	@Override
	public Void visit(Times n) {
		out.print("(");
		n.e1.accept(this);
		out.print(" * ");
		n.e2.accept(this);
		out.print(")");
		return null;
	}

	@Override
	public Void visit(IntegerLiteral n) {
		out.print(""+n.value);
		return null;
	}

	@Override
	public Void visit(IdentifierExp n) {
		out.print(n.name);
		return null;
	}

	@Override
	public Void visit(Not n) {
		out.print("!");
		n.e.accept(this);
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
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(ClassDecl n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(MethodDecl n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(VarDecl n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(IntArrayType n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(ObjectType n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(Block n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(If n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(While n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(ArrayAssign n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(BooleanLiteral n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(And n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(ArrayLength n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(ArrayLookup n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(Call n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(NewArray n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(NewObject n) {
    throw new Error("Not implemented");
  }

  @Override
  public Void visit(This n) {
    throw new Error("Not implemented");
  }
}
