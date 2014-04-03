package ast;

import visitor.Visitor;

public class TypeCoercion extends Expression {
  
  public final String type;
  public final String id;
  
  public TypeCoercion(String type, String id) {
    this.type = type;
    this.id = id;
  }

  @Override
  public <R> R accept(Visitor<R> v) {
    return v.visit(this);
  }

}
