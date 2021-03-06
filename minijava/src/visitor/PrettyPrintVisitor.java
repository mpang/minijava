package visitor;

import java.io.PrintWriter;
import java.util.Iterator;

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
	  n.mainClass.accept(this);
	  n.classes.accept(this);
	  return null;
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
		out.print("System.out.println(");
		n.exp.accept(this);
		out.println(");");
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
		out.print("" + n.value);
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
    out.println("class " + n.className + " {");
    out.indent();
    out.println("public static void main(String[] " + n.argName + ") {");
    out.indent();
    n.statement.accept(this);
    out.outdent();
    out.println("}");
    out.outdent();
    out.println("}");
    return null;
  }

  @Override
  public Void visit(ClassDecl n) {
    out.print("class " + n.name);
    if (!n.superName.isEmpty()) {
      out.print(" extends " + n.superName);
    }
    out.println(" {");
    out.indent();
    n.vars.accept(this);
    n.methods.accept(this);
    out.outdent();
    out.println("}");
    return null;
  }

  @Override
  public Void visit(MethodDecl n) {
    out.print("public ");
    n.returnType.accept(this);
    out.print(" " + n.name + "(");
    // formals
    Iterator<VarDecl> formalIterator = n.formals.iterator();
    while (formalIterator.hasNext()) {
      VarDecl formal = formalIterator.next();
      formal.accept(this);
      if (formalIterator.hasNext()) {
        out.print(", ");
      }
    }
    out.println(") {");
    
    // local variables and body
    out.indent();
    n.vars.accept(this);
    n.statements.accept(this);
    out.print("return ");
    n.returnExp.accept(this);
    out.println(";");
    out.outdent();
    out.println("}");
    return null;
  }

  @Override
  public Void visit(VarDecl n) {
    n.type.accept(this);
    out.print(" " + n.name);
    if (n.kind != VarDecl.Kind.FORMAL) {
      out.println(";");
    }
    return null;
  }

  @Override
  public Void visit(IntArrayType n) {
    out.print("int[]");
    return null;
  }

  @Override
  public Void visit(ObjectType n) {
    out.print(n.name);
    return null;
  }

  @Override
  public Void visit(Block n) {
    out.println("{");
    out.indent();
    n.statements.accept(this);
    out.outdent();
    out.println("}");
    return null;
  }

  @Override
  public Void visit(If n) {
    out.print("if (");
    n.tst.accept(this);
    out.print(") ");
    n.thn.accept(this);
    out.print("else ");
    n.els.accept(this);
    out.println();
    return null;
  }

  @Override
  public Void visit(While n) {
    out.print("while (");
    n.tst.accept(this);
    out.print(") ");
    n.body.accept(this);
    out.println();
    return null;
  }

  @Override
  public Void visit(ArrayAssign n) {
    out.print(n.name + "[");
    n.index.accept(this);
    out.print("] = ");
    n.value.accept(this);
    out.println(";");
    return null;
  }

  @Override
  public Void visit(BooleanLiteral n) {
    out.print("" + n.value);
    return null;
  }

  @Override
  public Void visit(And n) {
    n.e1.accept(this);
    out.print(" && ");
    n.e2.accept(this);
    return null;
  }

  @Override
  public Void visit(ArrayLength n) {
    n.array.accept(this);
    out.print(".length");
    return null;
  }

  @Override
  public Void visit(ArrayLookup n) {
    n.array.accept(this);
    out.print("[");
    n.index.accept(this);
    out.print("]");
    return null;
  }

  @Override
  public Void visit(Call n) {
    n.receiver.accept(this);
    out.print("." + n.name + "(");
    Iterator<Expression> iterator = n.rands.iterator();
    while (iterator.hasNext()) {
      Expression argument = iterator.next();
      argument.accept(this);
      if (iterator.hasNext()) {
        out.print(", ");
      }
    }
    out.print(")");
    return null;
  }

  @Override
  public Void visit(NewArray n) {
    out.print("new int[");
    n.size.accept(this);
    out.print("]");
    return null;
  }

  @Override
  public Void visit(NewObject n) {
    out.print("new " + n.typeName);
    out.print("()");
    return null;
  }

  @Override
  public Void visit(This n) {
    out.print("this");
    return null;
  }

  @Override
  public Void visit(InstanceOf n) {
    out.print(n.identifier + " instanceof " + n.className);
    return null;
  }

  @Override
  public Void visit(TypeCoercion n) {
    out.print("(" + n.type + ")" + n.id);
    return null;
  }

  @Override
  public Void visit(Super n) {
    out.print("super");
    return null;
  }
}
