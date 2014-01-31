package test.typechecker;

import org.junit.Test;

import parser.Parser;
import parser.jcc.ParseException;
import typechecker.implementation.TypeCheckerImplementation;
import ast.Program;

public class BuildSymbolTableTest {
  
  private final String program1 = "int fib (int n) {" +
                                  "r = n < 2 ? n : fib(n-1) + fib(n-2);" +
                                  "return r;" +
                                  "}" +
                                  "i0 = 6;" +
                                  "i1 = fib(i0);" +
                                  "print i1";
  
  private final String program2 = "i0 = 3;" +
                                  "i0 = 1 < 2;" +
                                  "print i0";
  
  private final String program3 = "i0 = 3;"
                                + "int x (int n) {"
                                + "n = 2;"
                                + "return n;"
                                + "}"
                                + "print i0";
  
  private final String program4 = "i0 = 3;"
                                + "int x () {"
                                + "i0 = 2 < 1;"
                                + "return i0;"
                                + "}"
                                + "print x()";
  
  private final String program5 = "i1 = i0;"
                                + "i0 = 3;"
                                + "print i1";

  @Test
  public void test() throws ParseException {
    Program program = Parser.parse(program1);
    TypeCheckerImplementation tci = new TypeCheckerImplementation(program);
    System.out.println("Symbol Table for Program1:");
    System.out.println(tci.buildTable().toString());
    
    program = Parser.parse(program2);
    tci = new TypeCheckerImplementation(program);
    System.out.println("Symbol Table for Program2:");
    System.out.println(tci.buildTable().toString());
    
    program = Parser.parse(program3);
    tci = new TypeCheckerImplementation(program);
    System.out.println("Symbol Table for Program3:");
    System.out.println(tci.buildTable().toString());
    
    program = Parser.parse(program4);
    tci = new TypeCheckerImplementation(program);
    System.out.println("Symbol Table for Program4:");
    System.out.println(tci.buildTable().toString());
    
    program = Parser.parse(program5);
    tci = new TypeCheckerImplementation(program);
    System.out.println("Symbol Table for Program5:");
    System.out.println(tci.buildTable().toString());
  }
}
