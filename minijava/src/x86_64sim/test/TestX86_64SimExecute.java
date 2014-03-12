package x86_64sim.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import util.SampleCode;
import x86_64sim.Program;
import x86_64sim.Sim;
import x86_64sim.State;
import x86_64sim.parser.SimParser;

public class TestX86_64SimExecute {
	protected State accept(String input) throws Exception {
		System.out.print("\nparsing string:\n"+ input);
		Program p = SimParser.parse(input);
		//		System.out.println("Program:");
		//		System.out.println(p.dump());
		State s = new State(p);
		System.out.println("Running:");
		s.beVerbose = true;
		s.run();
		System.out.println("Static: " + p.countInstructions() + " instructions generated");
		System.out.println("Dynamic: " + s.instructionsExecuted + " instructions executed");
		return s;
	}

	protected void run(String input) {
		String result = Sim.ulate(input, true).result;
		System.out.println("Resulting output:");
		System.out.print(result);
	}

	protected State accept(File file) throws Exception {
		Program p = null;
		State s = null;
		String outname = null;
		String outtmpname = null;
		
		try {
			System.out.println("parsing file: "+file);
			p = SimParser.parse(file);
			//		System.out.println("Program:");
			//		System.out.println(p.dump());
			s = new State(p);
			Matcher m = Pattern.compile("\\.s+$").matcher(file.getPath());
			outname = m.replaceAll(".out");
			outtmpname = m.replaceAll(".out.tmp");
			File out = new File(outtmpname);
			s.beVerbose = false;
			s.run(out);
		} catch (Error e) {
			throw e;
		}
		
		System.out.println("Static: " + p.countInstructions() + " instructions generated");
		System.out.println("Dynamic: " + s.instructionsExecuted + " instructions executed");
		System.out.println("Running diff ...");
		Process proc = Runtime.getRuntime().exec("diff -c " + outname + " " + outtmpname);
		proc.waitFor();
		
		if (proc.exitValue() != 0) {
		  throw new Error("Error: Acutal output: " + outtmpname + "different than expected: " + outname);
		}
		
		if (outtmpname != null) {
			File out = new File(outtmpname);
			if (!out.delete()) 
				System.out.println("Can't remove temporary output file " + outtmpname);
		}
		return s;
	}

	@Test
	public void testBasic() throws Exception {
		accept("main:\n" +
				"ret\n");
	}

	@Test
	public void testRet() throws Exception {
		accept("ret\n");
	}

	@Test
	public void testRunBasic() throws Exception {
		run("main:\n" +
				"ret\n");
	}

	@Test
	public void testRunQuad() throws Exception {
		run(".data\n" +
				"_a:\n" +
				".quad    23\n" +
				".text\n" +
				"main:\n" +
				"leaq    _a(%rip), %rax\n" +
				"movq    (%rax), %rax\n" +
				"ret\n");
	}

	@Test
	public void testRunRet() throws Exception {
		run("ret\n");
	}

	@Test (expected = Error.class)
	public void testRunBogus() throws Exception {
		accept("movq	$1, %rdi\n" +
				"movq	(%rdi), %rdi\n" +
				"ret\n");
	}

	@Test
	public void testMemoryW() throws Exception {
		String program = "movq $4096, %rbx\n" +
				"movq $11, %rax\n" +
				"movq %rax, (%rbx)\n" +
				"movq $13, %rax\n" +
				"movq %rax, 8(%rbx)\n" +
				"movq $15, %rax\n" +
				"movq %rax, 16(%rbx)\n" + 
				"ret\n";
		State s = accept(program);
		assertTrue(s.ram.read(4096L) == 11);
		assertTrue(s.ram.read(4096L+8L) == 13);
		assertTrue(s.ram.read(4096L+16L) == 15);
		for (long a = 4096L+24; a < s.ram.maxheap; a += 8) {
			assertTrue(s.ram.read(a) == 0L);
		}
	}

	@Test
	public void testMemoryRW() throws Exception {
		String program = "movq $4096, %rbx\n" +
				"movq $11, %rax\n" +
				"movq %rax, (%rbx)\n" +
				"movq $13, %rax\n" +
				"movq %rax, 8(%rbx)\n" +
				"movq $15, %rax\n" +
				"movq %rax, 16(%rbx)\n" + 
				"movq 8(%rbx), %rax\n" +
				"movq %rax, 32(%rbx)\n" +
				"movq 0(%rbx), %rax\n" +
				"movq %rax, 40(%rbx)\n" +
				"movq 16(%rbx), %rax\n" +
				"movq %rax, 48(%rbx)\n" +
				"ret\n";
		State s = accept(program);
		assertTrue(s.ram.read(4096L+0L) == 11);
		assertTrue(s.ram.read(4096L+8L) == 13);
		assertTrue(s.ram.read(4096L+16L) == 15);
		assertTrue(s.ram.read(4096L+24L) == 0);
		assertTrue(s.ram.read(4096L+32L) == 13);
		assertTrue(s.ram.read(4096L+40L) == 11);
		assertTrue(s.ram.read(4096L+48L) == 15);
		for (long a = 4096L+56; a < s.ram.maxheap; a += 8) {
			assertTrue(s.ram.read(a) == 0L);
		}
	}

	@Test 
	public void testExecuteSampleSCode() throws Exception {
		File[] files = SampleCode.sampleSFiles();
		for (File file : files) {
			try {
				accept(file);
			} catch (Error e) {
				System.out.println(e.getMessage());
			}
		}
	}

	@Test 
	public void testExecuteSampleSSCode() throws Exception {
		File[] files = SampleCode.sampleSSFiles();
		for (File file : files) {
			try {
				accept(file);
			} catch (Error e) {
				System.out.println(e.getMessage());
			}
		}
	}
}
