package util;
import org.junit.runner.JUnitCore;

public class RunTest {

	public RunTest() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws ClassNotFoundException {
		JUnitCore core= new JUnitCore();
		core.addListener(new TestListener());
		core.run(Class.forName(args[0]));
	}	
}
