package ast;

import visitor.Visitor;

public class InstanceOf extends Expression {
  
  public final String identifier;
  public final String className;
  
  public InstanceOf(String identifier, String className) {
    super();
    this.identifier = identifier;
    this.className = className;
  }

  @Override
  public <R> R accept(Visitor<R> v) {
    return v.visit(this);
  }

}
