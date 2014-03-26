package analysis.implementation;

import ir.temp.Color;
import ir.temp.Temp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import util.IndentingWriter;
import util.List;
import analysis.FlowGraph;
import analysis.InterferenceGraph;
import analysis.util.graph.Node;
import codegen.assem.A_MOVE;

public class InterferenceGraphImplementation<N> extends InterferenceGraph {

	private LivenessImplementation<N> liveness;
	private List<Move> moves;
	private FlowGraph<N> fg;

	public InterferenceGraphImplementation(FlowGraph<N> fg) {
	  this.fg = fg;
		liveness = new LivenessImplementation<N>(fg);
		
		// create nodes first
		for (Node<N> node : fg.nodes()) {
			for (Temp def : fg.def(node)) {
				nodeFor(def);
			}
			
			for (Temp use : fg.use(node)) {
				nodeFor(use);
			}
		}
		
		Set<Move> movesSet = new LinkedHashSet<Move>();
		for (Node<N> node : fg.nodes()) {
		  // move instructions
		  if (isMove(node)) {
		    A_MOVE move = (A_MOVE) node.wrappee();
		    movesSet.add(new Move(nodeFor(move.dst), nodeFor(move.src)));
		    
		    for (Temp liveOut : liveness.liveOut(node)) {
		      if (!liveOut.equals(move.src) && !liveOut.equals(move.dst)) {
		        addUndirectedEdge(liveOut, move.dst);
		      }
		    }
		  }
		  // non-move instructions
		  else {
		    for (Temp def : fg.def(node)) {
		      for (Temp liveOut : liveness.liveOut(node)) {
		        if (!def.equals(liveOut)) {
		          addUndirectedEdge(def, liveOut);
		        }
		      }
		    }
		  }
		}
		
		moves = List.list(movesSet);
	}
	
	private boolean isMove(Node<N> node) {
	  return node.wrappee() instanceof A_MOVE;
	}
	
	private void addUndirectedEdge(Temp first, Temp second) {
	  Node<Temp> firstNode = nodeFor(first);
	  Node<Temp> secondNode = nodeFor(second);
	  addEdge(firstNode, secondNode);
	  addEdge(secondNode, firstNode);
	}
	
	@Override
	public List<Move> moves() {
		return moves;
	}
	
	@Override
	public void dump(IndentingWriter out) {
		out.println("Liveness info: ");
		out.println(liveness);
		
		out.println("Interference graph: ");
		super.dump(out);
		
		out.print("Moves");
		out.println(moves);
	}

	private Color colorOf(Temp t, Map<Temp, Color> xcolorMap) {
		Color c = null;
		if (xcolorMap != null) c = xcolorMap.get(t);
		if (c == null)
			c = t.getColor();
		return c;
	}

	private String colorStringOf(Color c, Map<String, String> colorMap) {
		String s = null;
		if (c != null) s = colorMap.get(c.toString());
		return s == null ? "red" : s;
	}

	@Override
	public String dotString(int K, Map<Temp, Color> xcolorMap) {
		Map<String, String> colorMap = new HashMap<String, String>();
		colorMap.put("%rax", "gray");
		colorMap.put("%rbx", "hotpink");
		colorMap.put("%rcx", "mediumpurple");
		colorMap.put("%rdx", "salmon");
		colorMap.put("%rdi", "beige");
		colorMap.put("%rsi", "brown");
		colorMap.put("%rsp", "orange");
		colorMap.put("%rbp", "gold");
		colorMap.put("%r8", "yellow");
		colorMap.put("%r9", "darkgreen");
		colorMap.put("%r10", "green");
		colorMap.put("%r11", "cyan");
		colorMap.put("%r12", "blueviolet");
		colorMap.put("%r13", "skyblue");
		colorMap.put("%r14", "magenta");
		colorMap.put("%r15", "turquoise");
		HashSet<String> missing = new HashSet<String>(colorMap.keySet());
		StringBuffer out = new StringBuffer();
		out.append("graph \"Interference graph\" {\n");
	    out.append("labelloc=\"t\";\n");
	    out.append("fontsize=" + (Math.max(30, Math.sqrt(nodes().size() + 1) * nodes().size() * 1.2)) + ";\n");
	    out.append("label=\"" + name + "\";\n");

		out.append("  graph [size=\"6.5, 9\", ratio=fill];\n");
		for (Node<Temp> n : nodes()) {
			if (n.succ().size() > 0) {
				out.append("  \"" + n + "\" [fontsize=" + (Math.max(30, Math.sqrt(nodes().size() + 1) * nodes().size())));
				if (colorOf(n.wrappee(), xcolorMap) == null) {
					out.append(", style=\"setlinewidth(" + Math.min(10, nodes().size()) + ")\", color=" + (n.outDegree() < K ? "green" : "red"));
				} else {
					Color color = colorOf(n.wrappee(), xcolorMap);
					out.append(", style=filled, color=" + colorStringOf(color, colorMap));
					for (Temp t : n.wrappee().elements()) {
						missing.remove(t.toString());
					}
				}
				out.append("]\n");
			}
		}
		for (String m : missing) {
			out.append("  \"" + m + "\" [fontsize=" + (Math.max(30, Math.sqrt(nodes().size() + 1) * nodes().size())));
			out.append(", style=filled, color=" + colorMap.get(m));
			out.append("]\n");
		}
		Set<Pair<Temp, Temp>> done = new HashSet<Pair<Temp, Temp>>();
		for (Node<Temp> n : nodes()) {
			for (Node<Temp> o : n.succ()) {
				Pair<Temp, Temp> p = new Pair<Temp, Temp>(o.wrappee(), n.wrappee());
				if (done.contains(p)) {
					// Do nothing
				} else {
					out.append("  \"" + n + "\" -- \"" + o + "\";\n");
					done.add(new Pair<Temp, Temp>(n.wrappee(), o.wrappee()));
				}
			}
		}

		for (Move m : moves) {
			Node<Temp> s = m.src;
			Node<Temp> d = m.dst;
			out.append("\"" + s + "\" -- \"" + d + "\" [style=\"dashed, setlinewidth(5)\", color=" + 
					"green" + "];\n");
		}	
		out.append("}\n");
		return out.toString();
	}
}
