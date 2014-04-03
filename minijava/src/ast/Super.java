package ast;

import visitor.Visitor;

public class Super extends Expression {

  @Override
  public <R> R accept(Visitor<R> v) {
    return v.visit(this);
  }

}
