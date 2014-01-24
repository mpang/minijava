package ast;

import visitor.Visitor;

public class FormalList extends AST {

  public final NodeList<AST> parameters;
  
  public FormalList(NodeList<AST> parameters) {
    this.parameters = parameters;
  }
  
  @Override
  public <R> R accept(Visitor<R> v) {
    return v.visit(this);
  }
}
