package analysis;

import ir.temp.Color;
import ir.temp.Temp;

import java.util.Map;

import util.DefaultIndentable;
import util.IndentingWriter;
import util.List;
import analysis.util.graph.Graph;
import analysis.util.graph.Node;

abstract public class InterferenceGraph extends Graph<Temp> {
	
	public class Move extends DefaultIndentable {
		
		public Node<Temp> src;
		public Node<Temp> dst;
		public Move(Node<Temp> dst, Node<Temp> src) {
			this.dst = dst;
			this.src = src;
		}
		
		@Override
		public void dump(IndentingWriter out) {
			out.print(dst);
			out.print(" <= ");
			out.print(src);
		}
		
		@Override
		public boolean equals(Object obj) {
		  if (!(obj instanceof Move)) {
		    return false;
		  }
		  
		  Move anotherMove = (Move) obj;
		  return src.wrappee().equals(anotherMove.src.wrappee()) && dst.wrappee().equals(anotherMove.dst.wrappee());
		}
		
		@Override
		public int hashCode() {
		  int result = 17;
		  result = 31 * result + src.wrappee().hashCode();
		  result = 31 * result + dst.wrappee().hashCode();
		  return result;
		}
	}
	
	public String name = "Unknown";
	
	abstract public List<Move> moves();
	
	abstract public String dotString(int K, Map<Temp, Color> xcolorMap);
	
	abstract public boolean canProcess();
	
	abstract public Temp process();
	
	/**
	 * 
	 * @param k number of pre-coloured nodes (machine registers)
	 */
	abstract public void prepareForAllocation(int k);
	
	/**
	 * This default implementation will work, but you should 
	 * override it to provide a better implementation. 
	 * A good implementation should assign a higher spill cost
	 * to a Temp that is used frequently (and also may reduce
	 * spill cost if a temp interferes with lots of other temps,
	 * because spilling it will help avoid more spills.
	 */
	public double spillCost(Node<Temp> node) {
	  return 1;
	}
	
	@Override
	protected Node<Temp> makeNode(Temp content) {
		// Create nodes that print nicer.
		return new Node<Temp>(this, content) {
			@Override
			public String toString() {
				return wrappee().toString();
			}
		};
	}
}
