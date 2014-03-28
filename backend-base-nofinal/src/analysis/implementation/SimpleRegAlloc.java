package analysis.implementation;

import ir.frame.Frame;
import ir.temp.Color;
import ir.temp.Temp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import util.IndentingWriter;
import util.List;
import analysis.FlowGraph;
import analysis.InterferenceGraph;
import analysis.InterferenceGraph.Move;
import analysis.RegAlloc;
import analysis.util.graph.Node;
import codegen.AssemProc;
import codegen.assem.Instr;

public class SimpleRegAlloc extends RegAlloc {

	private AssemProc proc;
	private String trace = "";
	private FlowGraph<Instr> fg;
	private InterferenceGraph ig;
	private Frame frame;

	private Map<Temp, Color> colorMap = new HashMap<Temp, Color>();
	private List<Temp> registers;
	private List<Color> colors;
	private List<Color> spillColors = List.empty();
	private int iteration;

	/**
	 * List of *actual* spills.
	 */
	private List<Temp> spilled = List.empty();
	private List<Temp> colorOrdering = List.empty();
	
	private final int K;
	private Set<Temp> precoloured = new HashSet<Temp>();
  private java.util.List<Temp> simplifyCandidates = new ArrayList<Temp>();
  private java.util.List<Temp> spillCandidates = new ArrayList<Temp>();
  private Map<Temp, Set<Temp>> moveRelatedRegisters = new HashMap<Temp, Set<Temp>>();
  
	@Override
	public void dump(IndentingWriter out) {
		out.println(trace);
		out.println("Coloring {");
		out.indent();
		for (Temp temp : colorMap.keySet()) {
			out.print(temp);
			out.print(" : ");
			out.println(colorMap.get(temp));
			out.indent();
			for (Node<Temp> interferes : ig.nodeFor(temp).succ()) {
				out.print(interferes);
				out.print(":");
				out.print(getColor(interferes));
				out.print(" ");
			}
			out.println();
			out.outdent();
		}
		out.outdent();
		out.println("}");
		out.print("Spilled");
		out.println(spilled);
	}

	public SimpleRegAlloc(AssemProc proc) {
		this(proc, 1);
	}

	public SimpleRegAlloc(AssemProc proc, int iteration) {
		this.proc = proc;
		this.iteration = iteration;
		this.trace += proc.toString();
		this.frame = proc.getFrame();
		this.registers = frame.registers();
		this.K = registers.size();

		this.colors = List.empty();
		for (Temp reg : registers) 
			colors.add(reg.getColor());

		build();
		this.trace += "\n" + "Flow graph:\n" + fg.toString();
		this.trace += ig.toString();

		process();

		build(); // must rebuild the graph, since simplify should destroy it.
		color(colorOrdering);
	}

	private void color(List<Temp> toColor) {
		if (toColor.isEmpty()) return;
		Temp t = toColor.head();
		boolean success;

		// Try to color using a register
		//success = tryToColor(t, colors);
		
		List<Color> moveRelatedColors = List.empty();
		List<Color> nonMoveRelatedColors = colors;
		
		if (moveRelatedRegisters.containsKey(t)) {
		  for (Temp temp : moveRelatedRegisters.get(t)) {
		    moveRelatedColors = List.cons(temp.getColor(), moveRelatedColors);
		  }
		  
		  for (Color color : moveRelatedColors) {
		    nonMoveRelatedColors = nonMoveRelatedColors.delete(color);
		  }
		}
		
		success = tryToColor(t, moveRelatedColors);
		
		if (!success) {
		  success = tryToColor(t, nonMoveRelatedColors);
		}
		
		if (!success) {
			// Try to spill using an existing spill slot.
			spilled.add(t);
			success = tryToColor(t, spillColors);
		}
    
		if (!success) {
			//Create a new spill slot and use that.
			Color color = new SpillColor(frame);
			spillColors = spillColors.append(List.list(color));
			setColor(t, color);
		}
		
		color(toColor.tail());
	}

	private boolean tryToColor(Temp t, List<Color> colors) {
		for (Color color : colors) {
			if (isColorOK(ig.nodeFor(t), color)) {
				setColor(t, color);
				return true;
			}
		}
		return false;
	}

	private boolean isColorOK(Node<Temp> node, Color color) {
		for (Node<Temp> interferes : node.succ()) 
			if (color.equals(getColor(interferes))) return false;
		return true;
	}

	/**
	 * Start by building the interference graph for the procedure body.
	 */
	private void build() {
		this.fg = FlowGraph.build(proc.getBody());
		this.ig = fg.getInterferenceGraph();
		this.ig.name = proc.getLabel().toString() + " round " + iteration;
	}

	/**
	 * Returns a List of Temp's (a stack really) which suggest the order
	 * in which nodes should be assigned colors.
	 * 
	 */
	private void process() {
	  prepareForAllocation();
	  
	  while (!simplifyCandidates.isEmpty() || !spillCandidates.isEmpty()) {
	    
	    if (!simplifyCandidates.isEmpty()) {
	      simplify();
	    } else {
	      selectSpill();
	    }
	  }
	}
	
	private void prepareForAllocation() {
	  for (Node<Temp> node : ig.nodes()) {
	    Temp temp = node.wrappee();
	    if (temp.getColor() != null) {
        precoloured.add(temp);
      } else if (node.outDegree() >= K) {
        spillCandidates.add(temp);
      } else {
        simplifyCandidates.add(temp);
      }
	  }
	  
	  for (Move move : ig.moves()) {
	    if (move.src.goesTo(move.dst)) {
	      continue;
	    }
	    
	    Temp src = move.src.wrappee();
	    Temp dst = move.dst.wrappee();
	    
	    if (!precoloured.contains(src) && precoloured.contains(dst)) {
	      if (!moveRelatedRegisters.containsKey(src)) {
	        moveRelatedRegisters.put(src, new HashSet<Temp>());
	      }
	      moveRelatedRegisters.get(src).add(dst);
	    }
	    
	    if (!precoloured.contains(dst) && precoloured.contains(src)) {
	      if (!moveRelatedRegisters.containsKey(dst)) {
	        moveRelatedRegisters.put(dst, new HashSet<Temp>());
	      }
	      moveRelatedRegisters.get(dst).add(src);
	    }
	  }
	}
	
	private void simplify() {
	  Temp head = simplifyCandidates.remove(0);
	  colorOrdering = List.cons(head, colorOrdering);
    ig.rmNode(ig.nodeFor(head));
    checkSpill();
	}
	
	private void checkSpill() {
    Iterator<Temp> iterator = spillCandidates.iterator();
    while (iterator.hasNext()) {
      Temp next = iterator.next();
      
      if (ig.nodeFor(next).outDegree() < K) {
        simplifyCandidates.add(next);
        iterator.remove();
      }
    }
  }
	
	private void selectSpill() {
	  Temp temp = spillCandidates.remove(0);
	  simplifyCandidates.add(temp);
  }
	
	private Color getColor(Node<Temp> node) {
		return getColor(node.wrappee());
	}

	private void setColor(Temp t, Color color) {
		Assert.assertNull(getColor(t));
		colorMap.put(t, color);
	}

	/**
	 * Gets the color of a Temp based on the "hypothetical" coloring we are
	 * exploring now.
	 */
	private Color getColor(Temp temp) {
		Color color = temp.getColor();
		if (color != null) // it is precolored!
			return color;
		color = colorMap.get(temp);
		return color;
	}

	public List<Temp> getSpilled() {
		return spilled;
	}

	public Map<Temp, Color> getColorMap() {
		return colorMap;
	}

	public String getTrace() {
		return this.toString();
	}
}
