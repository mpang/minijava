package visitor;

import java.io.PrintWriter;

import ast.AST;
import ast.Assign;
import ast.BooleanType;
import ast.Conditional;
import ast.ExpressionList;
import ast.FormalList;
import ast.FunctionCallExp;
import ast.FunctionDeclaration;
import ast.IdentifierExp;
import ast.IntegerLiteral;
import ast.IntegerType;
import ast.LessThan;
import ast.Minus;
import ast.NodeList;
import ast.Not;
import ast.Plus;
import ast.Print;
import ast.Program;
import ast.Times;
import ast.UnknownType;
import ast.ParameterDeclaration;
import util.IndentingWriter;



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
		n.statements.accept(this);
		n.print.accept(this);
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
	public Void visit(UnknownType n) {
		out.println("UnknownType");
		return null;
	}

	@Override
	public Void visit(Conditional n) {
		out.println("Conditional");
		out.indent();
		n.e1.accept(this);
		n.e2.accept(this);
		n.e3.accept(this);
		out.outdent();
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
		new IdentifierExp(n.name).accept(this);
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
		out.println("IntegerLiteral "+n.value);
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
  public Void visit(FunctionDeclaration n) {
    out.println("FunctionDeclaration");
    out.indent();
    n.returnType.accept(this);
    new IdentifierExp(n.name).accept(this);
    n.parameters.accept(this);
    n.statements.accept(this);
    n.returnExpression.accept(this);
    out.outdent();
    return null;
  }

  @Override
  public Void visit(FunctionCallExp n) {
    out.println("FunctionCallExp");
    out.indent();
    new IdentifierExp(n.name).accept(this);
    n.arguments.accept(this);
    out.outdent();
    return null;
  }

  @Override
  public Void visit(FormalList n) {
    out.println("FormalList");
    out.indent();
    n.parameters.accept(this);
    out.outdent();
    return null;
  }

  @Override
  public Void visit(ExpressionList n) {
    out.println("ExpressionList");
    out.indent();
    n.expressions.accept(this);
    out.outdent();
    return null;
  }

  @Override
  public Void visit(ParameterDeclaration n) {
    out.println("ParameterDeclaration");
    out.indent();
    n.type.accept(this);
    new IdentifierExp(n.name).accept(this);
    out.outdent();
    return null;
  }
}
