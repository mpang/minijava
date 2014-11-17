package driver;

import ir.frame.Frame;
import ir.frame.x86_64.X86_64Frame;

import java.io.File;

import util.Utils;

public class DRun {

	public static final Frame architecture = X86_64Frame.factory;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String program = args[0];
		Compiler ec = new Compiler();
		Runner er = new Runner();
		String sProgram = Utils.changeSuffix(new File(program), "s");
		try {
			ec.compile(new File(program), new File(sProgram));
			System.out.println("Assembly code:");
			System.out.println(Utils.getContents(new File(sProgram)));
			System.out.println("Output:");
			er.run(new File(sProgram));
		} catch (Exception e) {
			System.out.println("Compilation problem");
			e.printStackTrace();
		}
	}

}
