package ast;

import visitor.Visitor;

public class FormalList extends AST {

  public final NodeList<ParameterDeclaration> parameters;
  
  public FormalList(NodeList<ParameterDeclaration> parameters) {
    this.parameters = parameters;
  }
  
  @Override
  public <R> R accept(Visitor<R> v) {
    return v.visit(this);
  }
}
