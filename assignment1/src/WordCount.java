import java.io.File;

public class WordCount {
  public static void main(String[] args) {
    long totalSize = 0L;
    for (String fileName : args) {
      File file = new File(fileName);
      
      if (!file.exists()) {
        System.out.println("Error opening file: " + fileName);
        continue;
      }
      
      long fileSize = file.length();
      totalSize += fileSize;
      System.out.printf("%10d %s\n", fileSize, fileName);
    }
    
    System.out.printf("%10d %s\n", totalSize, "total");
  }
}
