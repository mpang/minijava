package ast;

import visitor.Visitor;

public class ExpressionList extends AST {
  
  public final NodeList<Expression> expressions;
  
  public ExpressionList(NodeList<Expression> expressions) {
    this.expressions = expressions;
  }

  @Override
  public <R> R accept(Visitor<R> v) {
    return null;
  }
}
