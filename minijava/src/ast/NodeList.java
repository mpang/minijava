package ast;

import java.util.Iterator;
import java.util.List;

import visitor.Visitor;


public class NodeList<T extends AST> extends AST implements Iterable<T> {
	
	private List<T> nodes;

	public NodeList(List<T> nodes) {
		this.nodes = nodes;
	}

	public int size() {
		return nodes.size();
	}

	public T elementAt(int i) {
		return nodes.get(i);
	}

	@Override
	public <R> R accept(Visitor<R> v) {
		return v.visit(this);
	}

  @Override
  public Iterator<T> iterator() {
    return nodes.iterator();
  }

}
