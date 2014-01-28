package test.parser;

import static parser.jcc.JCCFunctionsParserConstants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import ast.Program;

import parser.Parser;
import parser.jcc.JCCFunctionsParser;

/**
 * The tests in this class are for testing the new Functions syntax added for Project Phase 1.
 * Common testing functions are borrowed from Test2LexInternal.java and Test3Parse.java 
 * 
 * Covers matching of tokens and validity of parsing programs containing new Functions syntax
 */
public class TestFunctionLexAndParse {

	// This function was taken from Test2LexInternal.java
	private JCCFunctionsParser parserOn(String inputString) {
		return new JCCFunctionsParser(new StringReader(inputString));
	}
	
	// This function was taken from Test2LexInternal.java
	void testTokens(String input, int[] tokenKinds) {
		JCCFunctionsParser parser = parserOn(input);
		for (int i = 0; i < tokenKinds.length; i++) {
			Assert.assertEquals(tokenKinds[i], parser.getNextToken().kind);
		}
		Assert.assertEquals(EOF, parser.getNextToken().kind);
	}
	
	// This function was taken from Test3Parse.java
	void accept(String input) throws Exception {
		System.out.println("Parsing program/string:");
		System.out.println("------------------------------");
		System.out.println(input);
		System.out.println("------------------------------");
		Program p = Parser.parse(input);
		System.out.println("Parse tree:");
		System.out.println(p.dump());
		System.out.println();
	}
	
	@Test
	public void TestTokenIntFunction() {
		testTokens(
			"int foo () {" +
			"	return 0;" +
			"}",
			new int[] {
				INT, IDENTIFIER, LPAREN, RPAREN, LBRACE,
				RETURN, INTEGER_LITERAL, SEMICOLON, 
				RBRACE
			}
		);
	}
	
	@Test
	public void TestTokenBooleanFunction() {
		testTokens(
			"boolean bar () {" +
			"	return false;" +
			"}",
			new int[] {
				BOOLEAN, IDENTIFIER, LPAREN, RPAREN, LBRACE,
				RETURN, IDENTIFIER, SEMICOLON, 
				RBRACE
			}
		);
	}
	
	@Test 
	public void TestTokenFibFunction() {
		testTokens(	
			"int fib (int n) {" +
			"	r = n < 2 ? n : fib(n-1) + fib(n-2);" +
			"	return r;" +
			"}" +
			"i0 = 6;" +
			"i1 = fib(i0);" +
			"print i1",
			new int[] { 
				INT, IDENTIFIER, LPAREN, INT, IDENTIFIER, RPAREN, LBRACE, 								// int fib ( int n ) {
				IDENTIFIER, ASSIGN, IDENTIFIER, SMALLER, INTEGER_LITERAL, QUESTION, IDENTIFIER, COLON, 	//   r = n < 2 ? n :
				IDENTIFIER, LPAREN, IDENTIFIER, MINUS, INTEGER_LITERAL, RPAREN, PLUS,					//	 fib ( n - 1 ) + 
				IDENTIFIER, LPAREN, IDENTIFIER, MINUS, INTEGER_LITERAL, RPAREN, SEMICOLON,				//   fib ( n - 2 ) ; 
				RETURN, IDENTIFIER, SEMICOLON, RBRACE,													//   return r ; }
				IDENTIFIER, ASSIGN, INTEGER_LITERAL, SEMICOLON,											// i0 = 6 ;
				IDENTIFIER, ASSIGN, IDENTIFIER, LPAREN, IDENTIFIER, RPAREN, SEMICOLON,					// i1 = fib ( i0 ) ;
				PRINT, IDENTIFIER																		// print r
			}
		);
	}
	
	@Test 
	public void TestParseBasicFunction() throws Exception {
		accept( "int foo ( ) {\n" +
				"  return 0;\n" +
				"}\n" +
				"print foo()");
		
		accept( "boolean bar (int n) {\n" +
				"  return (n < 0 ? true : false);\n" +
				"}\n" +
				"print bar(9)");
		
		accept( "int qux (int q, int u, int x) {\n" +
				"  return q + u + x;\n" +
				"}\n" +
				"print qux(q,u,x)");
	}
	
	@Test
	public void TestParseFunctionCall() throws Exception {
		accept( "i0 = foo();\n" +
				"print i0 + foo()");
		
		accept( "i1 = bar(i0);\n" +
				"print bar(i1) ? 1 : 0");
		
		accept( "print qux(1,2,3)");
	}
	
	@Test
	public void TestParseFibFunction() throws Exception {
		accept(	"int fib (int n) {\n" +
				"  r = n < 2 ? n : fib(n-1) + fib(n-2);\n" +
				"  return r;\n" +
				"}\n" +
				"i0 = 6;\n" +
				"i1 = fib(i0);\n" +
				"print i1");
	}
}
