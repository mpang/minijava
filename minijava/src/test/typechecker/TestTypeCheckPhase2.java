package test.typechecker;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import typechecker.implementation.TypeChecker;
import util.SampleCode;
import util.TranscriptWriter;


/**
 * This is an "Internal" test class. It tests functionality that is somewhat
 * dependent on the internal workings of the particular implementation.
 * <p>
 * As such the tests should be considered "optional" in that a student
 * is not required to pass these tests exactly. (And in fact these may
 * be difficult, since the test output may even change depending on
 * the order of entries in a hashtable.
 * <p>
 * They are however useful for testing phase 1 of the model implementation.
 * The test and its transcript are included in the project drop, in case you want 
 * to use it for your own implementation. You may change the tests in this class 
 * if you like.
 * 
 * @author kdvolder
 */
public class TestTypeCheckPhase2 {
	
	final static boolean verifyTanscript = false;
  // test output is highly dependent on implementation details
  // (such as, for example, sorting order of entries in a table).
  // It may be useful to set this to true once you have created
  // your implementation and verified the output of this test is
  // what you expect.
	
	private static TranscriptWriter transcript;
	
	private static int testNumber = 0;
	
	@BeforeClass public static void openTranscript() throws IOException {
		if (verifyTanscript)
			transcript = new TranscriptWriter(new File("TestTypeCheckPhase2.log"));
		else {
			File tempLog = new File("TestTypeCheckPhase2-log.tmp");
			if (tempLog.exists())
				tempLog.delete();
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
	
	@Test public void testSampleCode() throws Exception {
		File[] sampleFiles = SampleCode.sampleJavaFiles();
		for (int i = 0; i < sampleFiles.length; i++) {
		  TypeChecker.parseAndCheck(sampleFiles[i]);
		}
	}
}
