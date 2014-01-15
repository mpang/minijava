package ast;

import java.io.PrintWriter;
import java.io.StringWriter;

import visitor.DepthCountVisitor;
import visitor.NodeCountVisitor;
import visitor.PrettyPrintVisitor;
import visitor.StructurePrintVisitor;
import visitor.Visitor;

public abstract class AST {
	
	public abstract <R> R accept(Visitor<R> v);
	
	@Override
	public String toString() {
		StringWriter out = new StringWriter();
		this.accept(new PrettyPrintVisitor(new PrintWriter(out)));
		return out.toString();
	}

	public String dump() {
		StringWriter out = new StringWriter();
		this.accept(new StructurePrintVisitor(new PrintWriter(out)));
		return out.toString();		
	}
	
	public int nodeCount() {
	  return this.accept(new NodeCountVisitor());
	}
	
	public int depthCount() {
	  return this.accept(new DepthCountVisitor());
	}
}
