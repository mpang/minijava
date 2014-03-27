package test.translate;

import ir.frame.Frame;
import ir.frame.x86_64.X86_64Frame;
import ir.interp.Interp;
import ir.interp.InterpMode;

import java.io.File;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import translate.Fragments;
import translate.Translator;
import typechecker.TypeCheckerException;
import util.SampleCode;
import util.Utils;


/**
 * Test the minijava translation phase that takes a (type-checked) program and turns
 * the bodies of all the methods in the program into IRtrees.
 * <p>
 * This test suite uses the IR interpreter to simulate the execution of the
 * resulting IR. This gives us some confidence that our translation works correctly :-)
 * 
 * @author kdvolder
 */
public class TestTranslate {

	public static final Frame architecture = X86_64Frame.factory;

	/**
	 * To make it easy to run all of these tests with the either 
	 * linearized ir code, basic blocks or trace scheduled code
	 * We determine the simulation mode via this method.
	 * <p>
	 * Simply creating a subclass and overriding this method will create
	 * a test suite that runs all the same tests in a different simulation 
	 * mode.
	 * 
	 * @return
	 */
	protected InterpMode getSimulationMode() {
		// return null;
		return InterpMode.LINEARIZED_IR;
	}

	/**
	 * Print out all the generated IR?
	 * <p>
	 * If false, only the result of simulating the IR execution 
	 * will be printed.
	 */
	protected boolean dumpIR() {
		return false;
	}

	//////////////// Sample code //////////////////////////////////
	
	@Test
	public void testSampleCode() throws Exception {
		File[] files = SampleCode.sampleJavaFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (!optionalSample(f))
				test(f);
		}
	}
	@Test @Ignore // Don't run this unless you are implementing inheritance support!
	public void testOptionalSampleCode() throws Exception {
		File[] files = SampleCode.sampleJavaFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (optionalSample(f))
				test(f);
		}
	}
	
	protected Fragments test(File program) throws TypeCheckerException, Exception {
		System.out.println("Translating: "+program);
		String expected = Utils.getExpected(program);
		
		return test(expected, program);
	}	

	protected Fragments test(String expected, File program)
			throws TypeCheckerException, Exception {
		Fragments translated = Translator.translate(architecture, program);
		if (dumpIR()) {
			System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV");
			System.out.println(translated);
			System.out.println();
		}
			
		if (getSimulationMode()!=null) {
			System.out.println("Simulating IR code:");
			Interp interp = new Interp(translated, getSimulationMode());
			String result = interp.run();
			System.out.println(result);
			Assert.assertEquals(expected, result);
		}
		System.out.println("=================================");
		return translated;
	}
	
	private boolean optionalSample(File f) {
		return f.getName().equals("treevisitor.java");
	}
	
	protected Fragments test(String expected, String program) throws Exception {
		System.out.println("Translating program: ");
		System.out.println(program);
		Fragments translated = Translator.translate(architecture, program);
		if (dumpIR()) {
			System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV");
			System.out.println(translated);
			System.out.println();
		}
		if (getSimulationMode()!=null) {
			System.out.println("Simulating IR code:");
			Interp interp = new Interp(translated, getSimulationMode());
			String result = interp.run();
			System.out.print(result);
			Assert.assertEquals(expected, result);
		}
		System.out.println("=================================");
		return translated;
	}

}
