package ast;

import visitor.Visitor;

public class FunctionCallExp extends Expression {
  
  public final IdentifierExp name;
  public final ExpressionList arguments;
  
  public FunctionCallExp(IdentifierExp name, ExpressionList arguments) {
    super();
    this.name = name;
    this.arguments = arguments;
  }

  @Override
  public <R> R accept(Visitor<R> v) {
    return null;
  }
}
