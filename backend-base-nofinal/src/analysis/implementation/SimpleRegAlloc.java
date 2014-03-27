package analysis.implementation;

import ir.frame.Frame;
import ir.temp.Color;
import ir.temp.Temp;

import java.util.ArrayList;
import java.util.Arrays;
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
  private java.util.List<Move> coalesceCandidates = new ArrayList<Move>();
  private java.util.List<Temp> freezeCandidates = new ArrayList<Temp>();
  private java.util.List<Temp> spillCandidates = new ArrayList<Temp>();
  
  private Set<Move> coalescedMoves = new HashSet<Move>();
  private Set<Move> constrainedMoves = new HashSet<Move>();
  private Set<Move> frozenMoves = new HashSet<Move>();
  private Set<Move> activeMoves = new HashSet<Move>();
  /**
   * mapping between temp and its associated moves
   */
  private Map<Temp, Set<Move>> tempMoveMapping = new HashMap<Temp, Set<Move>>();
  /**
   * key: temps that are coalesced; value: temp that represent the coalesced one
   */
  private Map<Temp, Temp> alias = new HashMap<Temp, Temp>();

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
		success = tryToColor(t, colors);

		
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
	  
	  while (!simplifyCandidates.isEmpty() || !coalesceCandidates.isEmpty() ||
	         !freezeCandidates.isEmpty() || !spillCandidates.isEmpty()) {
	    
	    if (!simplifyCandidates.isEmpty()) {
	      simplify();
	    } else if (!coalesceCandidates.isEmpty()) {
	      coalesce();
	    } else if (!freezeCandidates.isEmpty()) {
	      freeze();
	    } else {
	      selectSpill();
	    }
	  }
	}
	
	private void prepareForAllocation() {
	  for (Move move : ig.moves()) {
      coalesceCandidates.add(move);
      for (Temp temp : Arrays.asList(move.src.wrappee(), move.dst.wrappee())) {
        if (!tempMoveMapping.containsKey(temp)) {
          tempMoveMapping.put(temp, new HashSet<Move>());
        }
        tempMoveMapping.get(temp).add(move);
      }
    }
	  
	  for (Node<Temp> node : ig.nodes()) {
	    Temp temp = node.wrappee();
	    if (temp.getColor() != null) {
        precoloured.add(temp);
      } else if (node.outDegree() >= K) {
        spillCandidates.add(temp);
      } else if (tempMoveMapping.containsKey(temp)) {
        freezeCandidates.add(temp);
      } else {
        simplifyCandidates.add(temp);
      }
	  }
	}
	
	private void simplify() {
	  Temp head = simplifyCandidates.remove(0);
	  colorOrdering = List.cons(head, colorOrdering);
    ig.rmNode(ig.nodeFor(head));
    checkSpill();
	}
	
	/**
	 * 
	 * @param temp
	 * @return moves related to a temp that are not coalesced, constrained, or frozen
	 */
	private Set<Move> nodeMoves(Temp temp) {
	  if (!tempMoveMapping.containsKey(temp)) {
	    return new HashSet<Move>();
	  }
	  
	  Set<Move> allMoves = new HashSet<Move>(tempMoveMapping.get(temp));
	  Set<Move> coalescedCandidatesCopy = new HashSet<Move>(coalesceCandidates);
	  coalescedCandidatesCopy.addAll(activeMoves);
	  allMoves.retainAll(coalescedCandidatesCopy);
	  return allMoves;
	}
	
	private void enableMoves(java.util.List<Temp> temps) {
	  for (Temp temp : temps) {
	    for (Move move : nodeMoves(temp)) {
	      if (activeMoves.contains(move)) {
	        activeMoves.remove(move);
	        coalesceCandidates.add(move);
	      }
	    }
	  }
	}
	
	private void checkSpill() {
    Iterator<Temp> iterator = spillCandidates.iterator();
    while (iterator.hasNext()) {
      Temp next = iterator.next();
      
      if (ig.nodeFor(next).outDegree() < K) {
        java.util.List<Temp> temps = new ArrayList<Temp>(Arrays.asList(next));
        for (Node<Temp> neighbours : ig.nodeFor(next).succ()) {
          temps.add(neighbours.wrappee());
        }
        enableMoves(temps);
        
        if (nodeMoves(next).isEmpty()) {
          simplifyCandidates.add(next);
        } else {
          freezeCandidates.add(next);
        }
        
        iterator.remove();
      }
    }
  }
	
	private void selectSpill() {
	  Temp temp = spillCandidates.remove(0);
	  simplifyCandidates.add(temp);
	  freezeMoves(temp);
  }
	
	private Temp getAlias(Temp temp) {
	  if (alias.containsKey(temp)) {
	    return getAlias(alias.get(temp));
	  }
	  
	  return temp;
	}
	
	private void addToCandidates(Temp temp) {
	  if (!precoloured.contains(temp) && nodeMoves(temp).isEmpty() && ig.nodeFor(temp).outDegree() < K) {
	    freezeCandidates.remove(temp);
	    simplifyCandidates.add(temp);
	  }
	}
	
	private boolean canCoalesceTemps(Temp a, Temp b) {
	  int count = 0;
	  Set<Node<Temp>> nodes = new HashSet<Node<Temp>>();
	  for (Node<Temp> node : ig.nodeFor(a).succ().append(ig.nodeFor(b).succ())) {
	    nodes.add(node);
	  }
	  
	  for (Node<Temp> node : nodes) {
	    if (node.outDegree() >= K) {
	      count++;
	    }
	  }
	  
	  return count < K;
	}
	
	private void coalesce(Temp a, Temp b) {
	  if (freezeCandidates.contains(b)) {
	    freezeCandidates.remove(b);
	  } else {
	    spillCandidates.remove(b);
	  }
	  
	  alias.put(b, a);
	  tempMoveMapping.get(a).addAll(tempMoveMapping.get(b));
	  enableMoves(Arrays.asList(b));
	  ig.merge(ig.nodeFor(a), ig.nodeFor(b));
	  checkSpill();
	  if (ig.nodeFor(a).outDegree() >= K && freezeCandidates.contains(a)) {
	    freezeCandidates.remove(a);
	    spillCandidates.add(a);
	  }
	}
	
	private void coalesce() {
	  Move move = coalesceCandidates.remove(0);
	  Temp src = getAlias(move.src.wrappee());
	  Temp dst = getAlias(move.dst.wrappee());
	  
	  if (src.equals(dst)) {
      coalescedMoves.add(move);
      addToCandidates(src);
    } else if ((precoloured.contains(src) && precoloured.contains(dst)) || ig.nodeFor(src).goesTo(ig.nodeFor(dst))) {
      constrainedMoves.add(move);
      addToCandidates(src);
      addToCandidates(dst);
    } else if (canCoalesceTemps(src, dst)) {
      coalescedMoves.add(move);
      
      if (precoloured.contains(dst)) {
        coalesce(dst, src);
        addToCandidates(dst);
      } else {
        coalesce(src, dst);
        addToCandidates(src);
      }
      
      //coalesce(src, dst);
      //addToCandidates(src);
    } else {
      activeMoves.add(move);
    }
	}
	
	private void freeze() {
	  Temp temp = freezeCandidates.remove(0);
	  simplifyCandidates.add(temp);
	  freezeMoves(temp);
	}
	
	private void freezeMoves(Temp temp) {
	  for (Move move : nodeMoves(temp)) {
      Temp src = getAlias(move.src.wrappee());
      Temp dst = getAlias(move.dst.wrappee());
      Temp anotherTemp = getAlias(temp).equals(src) ? dst : src;
      
      activeMoves.remove(move);
      frozenMoves.add(move);
      
      if (freezeCandidates.contains(anotherTemp) && nodeMoves(anotherTemp).isEmpty()) {
        freezeCandidates.remove(anotherTemp);
        simplifyCandidates.add(anotherTemp);
      }
    }
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
