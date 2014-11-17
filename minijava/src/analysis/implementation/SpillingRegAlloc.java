package analysis.implementation;

import ir.temp.Color;
import ir.temp.Temp;

import java.util.Map.Entry;

import util.IndentingWriter;

import codegen.AssemProc;

import analysis.RegAlloc;


/**
 * A slightly better implementation of Register allocation. It properly
 * deals with spilled temps by rewriting the procedure body and trying again.
 */
public class SpillingRegAlloc extends RegAlloc {
	// Records the result of each iteration: for debugging.
	private String trace = "";

	public SpillingRegAlloc(AssemProc proc) {
		SimpleRegAlloc simple = new SimpleRegAlloc(proc);
		int lastSpilled = Integer.MAX_VALUE;
		int iteration = 1;
		while (!simple.getSpilled().isEmpty()) {
			// If we have more spills this time than last time, we aren't making good progress
			if (simple.getSpilled().size() >= lastSpilled && iteration > 4) {
				System.out.println(trace);
				System.out.println(simple);
				throw new Error("More spilled registers this time (" + simple.getSpilled().size() + ") than last time (" + lastSpilled + ")");
			}
			// We'll paint the spilled Temp's with their spill color.
			// This permanently marks them as spilled so when we 
			// rewrite the code we can deal with them specially.
			// (We'll simply add a special munching rule that matches spilled
			// TEMP nodes to our IR Munching rules.
			lastSpilled = simple.getSpilled().size();
			for (Temp spilled : simple.getSpilled()) {
				spilled.paint(simple.getColorMap().get(spilled));
			}
			trace += "Register allocation iteration " + iteration + "\n" + simple.toString();
			iteration++;
			proc.rewrite();
			simple = new SimpleRegAlloc(proc);
		}

		// The last allocation should be good, with no spills!
		// Paint the Temps in permanent paint now.
		for (Entry<Temp, Color> entry : simple.getColorMap().entrySet()) {
			entry.getKey().paint(entry.getValue());
		}
		trace += "Register allocation iteration " + iteration + "\n" + simple.toString();
	}

	@Override
	public void dump(IndentingWriter out) {
		out.println(trace);

	}

	public String getTrace() {
		return trace;
	}
}
