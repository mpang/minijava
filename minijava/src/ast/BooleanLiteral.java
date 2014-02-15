package ast;

import visitor.Visitor;

public class BooleanLiteral extends Expression {
	
	public final boolean value;

	public BooleanLiteral(boolean value) {
		super();
		this.value = value;
	}
	
	public BooleanLiteral(String image) {
	  this(Boolean.parseBoolean(image));
	}

	@Override
	public <R> R accept(Visitor<R> v) {
		return v.visit(this);
	}

}
