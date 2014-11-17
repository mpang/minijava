package ir.tree;

import ir.interp.Word;
import ir.interp.X86_64SimFrame;
import ir.temp.Color;
import ir.temp.Temp;
import ir.visitor.Visitor;

import org.junit.Assert;

import util.IndentingWriter;
import util.List;


public class TEMP extends IRExp {
	public final Temp temp;
	public TEMP(Temp t) {
		Assert.assertNotNull(t);
		temp=t;
	}
	@Override
	public void dump(IndentingWriter out) {
//		out.print("TEMP ");
		out.print(temp);
		if (temp.getColor()!=null) { 
			out.print(":");
			out.print(temp.getColor());
		}
	}
	@Override
	public IRExp build(List<IRExp> kids) {
		return this;
	}
	@Override
	public List<IRExp> kids() {
		return List.empty();
	}
	@Override
	public Word interp(X86_64SimFrame env) {
		//We only get here if we are reading the TEMP.
		//A TMP in the dst of a MOVE is treated explicitly by MOVE
		return env.getTemp(temp);
	}
	public Color getColor() {
		return temp.getColor();
	}
	
	@Override
	public void set(Word value, X86_64SimFrame env) {
		env.setTemp(temp, value);
	}
	
	@Override
	public boolean mentions(Temp t) {
		return temp.equals(t);
	}
	
	@Override 
	public List<Temp> use() {
		return List.list(temp);
	}

	@Override
	public <R> R accept(Visitor<R> v) {
		return v.visit(this);
	}
}

