package driver;

import ir.frame.Frame;
import ir.frame.x86_64.X86_64Frame;

import java.io.File;


import translate.Fragments;
import translate.TranslatorLabels;
import util.Utils;
import x86_64sim.Sim;

import codegen.CodeGenerator;

public class DSimFinal {

	public static final Frame architecture = X86_64Frame.factory;
	
	/**
	 * Given a source file, compile it and write the parse tree to System.out.
	 * 
	 * @param program  program to compile.
	 */
	public static void compile(File program) throws Exception {
		Compiler compiler = new Compiler();

		String assemFileName = Utils.changeSuffix(program, "s");
		File assemFile = new File(assemFileName);

		System.out.println("compiling: "+program);
		compiler.compile(program, assemFile);
		System.out.println(">>>>>  "+assemFile);
		String sProgram = Utils.getContents(assemFile);
		String output = Sim.ulate(sProgram, false).result;
		System.out.println("Program output:\n" + output);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String program = args[0];
		try {
			compile(new File(program));
		} catch (Exception e) {
			System.out.println("Compilation problem");
			e.printStackTrace();
		}
	}

}
