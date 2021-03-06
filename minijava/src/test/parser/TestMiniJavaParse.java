package test.parser;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import parser.Parser;
import util.SampleCode;
import util.TranscriptWriter;
import ast.AST;

/**
 * The tests in this class correspond more or less to the work in Chapter 4.
 * <p>
 * We simply run the same tests as the Test3Parse tests (done this by inheriting all
 * the tests from Test3Parse via the extends clause). But we modify the accept method
 * to also check the output of pretty printing the parse tree against a (supposedly correct)
 * test transcript.
 * <p>
 * See also {@link test.TranscriptWriter} for more 
 * information on the Transcript.
 * 
 * @author kdvolder
 */
public class TestMiniJavaParse {

	/**
	 * If verifyTranscript is true then the output of the test run will be
	 * compared against the "TestParse4.log" file. Errors will be thrown if
	 * the actual output doesn't match the logfile contents.
	 * <p>
	 * If verifyTranscript is false, the output will not be compared to the
	 * logfile.
	 * <p>
	 * This is useful if you are debugging individual tests, because running only
	 * some but not all tests will produce transcript verification errors 
	 * since skipping tests will cause some output to go "missing".
	 * <p>
	 * Note: in "noverify" mode, a test transcript is written to the file
	 * "Test4Parse-log.tmp". This logfile is rewritten on
	 * each testrun.
	 */
	final static boolean verifyTanscript = true;
	
	private static TranscriptWriter transcript;
	
	private static int testNumber = 0;
	
	@BeforeClass public static void openTranscript() throws IOException {
		if (verifyTanscript) {
			transcript = new TranscriptWriter(new File("Test4Parse.log"));
		}
		else {
			File tempLog = new File("Test4Parse-log.tmp");
			if (tempLog.exists()) {
				tempLog.delete();
			}
			transcript = new TranscriptWriter(tempLog);
		}
	}
	
	@AfterClass public static void closeTranscript() throws IOException {
		transcript.close();
	}
	
	@Before public void markBegin() throws IOException {
		transcript.mark("### BEG # TEST "+ ++testNumber+" ###");
	}
	
	@After public void markEnd() throws IOException {
		transcript.mark("### END # TEST "+ testNumber+" ###");
	}
	
	@Test 
  public void testParseSampleCode() throws Exception {
    File[] files = SampleCode.sampleJavaFiles();
    for (File file : files) {
      accept(file);
    }
  }
	
	protected void accept(File input) throws Exception {
		transcript.println("---------------");
		transcript.println("File: " + input);
		AST result = Parser.parse(input);
		verifyAST(result);
	}
	
	private void verifyAST(AST result) throws Exception {
		transcript.println("vvvvvvvvvvvvvvv");
		String pretty = Parser.unparse(result);
		transcript.println(pretty); // this is automatically verified against transcript log if it exists.
		//As an extra safety against possible human error in verifying that the
		//captured transcript output is actually correct, we also double check that the
		//the pretty printed output can be parsed again and results in the same parse tree.
		AST parsedAgain = Parser.parse(pretty);
		Assert.assertEquals(pretty, Parser.unparse(parsedAgain));
	}
}
