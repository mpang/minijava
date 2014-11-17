package util;

import org.junit.runner.notification.RunListener;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestListener extends RunListener {
	boolean failed;
	String description;
	
	public TestListener() {
		failed = false;
	}

	@Override
	public void testAssumptionFailure(Failure failure) {
		// super.testAssumptionFailure(failure);
		//System.out.println("Test assumption fail: " + shortName(failure.getDescription()));
		failed = true;
		description = shortName(failure.getDescription());
	}

	@Override
	public void testFailure(Failure failure) throws Exception {
		// super.testFailure(failure);
		// System.out.println("Test fail: " + shortName(failure.getDescription()));
		failed = true;
		description = shortName(failure.getDescription());
	}

	@Override
	public void testFinished(Description description) throws Exception {
		// super.testFinished(description);
		System.out.println("    Finished test: " + shortName(description) + ": " + (failed ? "fail " : "pass"));
//		System.setSecurityManager(null);
	}

	@Override
	public void testIgnored(Description description) throws Exception {
		// super.testIgnored(description);
		// System.out.println("Test ignored: " + shortName(description));
	}

	@Override
	public void testRunFinished(Result result) throws Exception {
		// super.testRunFinished(result);
		// System.out.println("Run finish: " + result);
	}

	@Override
	public void testRunStarted(Description description) throws Exception {
		// super.testRunStarted(description);
		// System.out.println("Run start: " + shortName(description));
	}

	@Override
	public void testStarted(Description d) throws Exception {
		// super.testStarted(d);
		System.out.println("    Running  test: " + shortName(d));
		failed = false;
		description = "";
//		System.setSecurityManager(new SecurityManager());
	}

	private String shortName(Description description) {
		String s = description.getDisplayName();
		int lparen = s.indexOf('(');
		if (lparen >= 0)
			return s.substring(0,  lparen);
		else
			return s;
	}
}
