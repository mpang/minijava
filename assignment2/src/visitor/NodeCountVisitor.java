package visitor;

import ast.AST;
import ast.Assign;
import ast.BooleanType;
import ast.Conditional;
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

public class NodeCountVisitor implements Visitor<Integer> {

  @Override
  public <T extends AST> Integer visit(NodeList<T> ns) {
    int count = 0;
    for (int i = 0; i < ns.size(); i++) {
      count += ns.elementAt(i).accept(this);
    }
    return count;
  }

  @Override
  public Integer visit(Program n) {
    return 1 + n.print.accept(this) + n.statements.accept(this);
  }

  @Override
  public Integer visit(Print n) {
    return 1 + n.exp.accept(this);
  }

  @Override
  public Integer visit(Assign n) {
    return 1 + n.name.accept(this) + n.value.accept(this);
  }

  @Override
  public Integer visit(LessThan n) {
    return 1 + n.e1.accept(this) + n.e2.accept(this);
  }

  @Override
  public Integer visit(Conditional n) {
    return 1 + n.e1.accept(this) + n.e2.accept(this) + n.e3.accept(this);
  }

  @Override
  public Integer visit(Plus n) {
    return 1 + n.e1.accept(this) + n.e2.accept(this);
  }

  @Override
  public Integer visit(Minus n) {
    return 1 + n.e1.accept(this) + n.e2.accept(this);
  }

  @Override
  public Integer visit(Times n) {
    return 1 + n.e1.accept(this) + n.e2.accept(this);
  }

  @Override
  public Integer visit(IntegerLiteral n) {
    return 1;
  }

  @Override
  public Integer visit(IdentifierExp n) {
    return 1;
  }

  @Override
  public Integer visit(Not not) {
    return 1 + not.e.accept(this);
  }

  @Override
  public Integer visit(IntegerType n) {
    return 1;
  }

  @Override
  public Integer visit(BooleanType n) {
    return 1;
  }

  @Override
  public Integer visit(UnknownType n) {
    return 1;
  }
}
