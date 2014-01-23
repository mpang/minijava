package ast;

import visitor.Visitor;

public class FunctionDeclaration extends Statement {
  
  public final Type returnType;
  public final String name;
  public final FormalList parameters;
  public final NodeList<Statement> statements;
  public final Expression returnExpression;

  public FunctionDeclaration(Type returnType,
                             String name,
                             FormalList parameters,
                             NodeList<Statement> statements,
                             Expression returnExpression) {
    
    super();
    this.returnType = returnType;
    this.name = name;
    this.parameters = parameters;
    this.statements = statements;
    this.returnExpression = returnExpression;
  }
  
  @Override
  public <R> R accept(Visitor<R> v) {
    return null;
  }
}
