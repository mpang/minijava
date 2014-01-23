package ast;

import visitor.Visitor;

public class FunctionCallExp extends Expression {
  
  public final String functionName;
  public final NodeList<Expression> arguments;
  
  public FunctionCallExp(String functionName, NodeList<Expression> arguments) {
    super();
    this.functionName = functionName;
    this.arguments = arguments;
  }

  @Override
  public <R> R accept(Visitor<R> v) {
    return null;
  }
}
