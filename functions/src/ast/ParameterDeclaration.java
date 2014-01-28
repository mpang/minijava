package ast;

import visitor.Visitor;

public class ParameterDeclaration extends AST {

  public final Type type;
  public final String name;
  
  public ParameterDeclaration(Type type, String name) {
    this.type = type;
    this.name = name;
  }
  
  @Override
  public <R> R accept(Visitor<R> v) {
    return v.visit(this);
  }
}
