package test;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * We use this class as a place to put some methods and constants to get at
 * the sample code in the sample directory.
 * 
 * @author kdvolder
 */
public class SampleCode {

	/**
	 * Points to a directory containing sample java code to parse.
	 */
	public final static File sample_dir = new File("sample");

	/**
	 * Filter for selecting exp files only.
	 */
	private static final FilenameFilter expFileFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".exp");
		}
	};

	/**
	 * Filter for selecting .s files only.
	 */
	private static final FilenameFilter sFileFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".s");
		}
	};

	/**
	 * Filter for selecting .ss files only.
	 */
	private static final FilenameFilter ssFileFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".ss");
		}
	};

	/**
	 * @return An array of sample exp files.
	 */
	public static File[] sampleFiles() {
		File[] files = sample_dir.listFiles(expFileFilter);
		// Should sort the array to ensure that we produce the files in the same order
		// independent of the order in which the OS produces them.
		Arrays.sort(files);
		return files;
	}

	/**
	 * @return An array of sample .s files.
	 */
	public static File[] sampleSFiles() {
		File[] files = sample_dir.listFiles(sFileFilter);
		// Should sort the array to ensure that we produce the files in the same order
		// independent of the order in which the OS produces them.
		Arrays.sort(files);
		return files;
	}
	/**
	 * @return An array of sample .s files.
	 */
	public static File[] sampleSSFiles() {
		File[] files = sample_dir.listFiles(ssFileFilter);
		// Should sort the array to ensure that we produce the files in the same order
		// independent of the order in which the OS produces them.
		Arrays.sort(files);
		return files;
	}

}
