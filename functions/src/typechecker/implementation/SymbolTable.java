package typechecker.implementation;

import java.util.Map.Entry;

import util.DefaultIndentable;
import util.ImpTable;
import util.ImpTable.DuplicateException;
import util.IndentingWriter;
import ast.Type;

public class SymbolTable extends DefaultIndentable {

  private final ImpTable<Type> globalVariables;
  private final ImpTable<FunctionEntry> functions;
  private ImpTable<Type> currentScope;
  
  public SymbolTable() {
    globalVariables = new ImpTable<Type>();
    functions = new ImpTable<FunctionEntry>();
    currentScope = globalVariables;
  }
  
  /**
   * Enters the scope of a function
   * @param functionName name of a function
   */
  public void enterScope(String functionName) {
    FunctionEntry functionEntry = functions.lookup(functionName);
    assert functionEntry != null;
    currentScope = functionEntry.variables;
  }
  
  /**
   * Return to global scope
   */
  public void exitScope() {
    currentScope = globalVariables;
  }
  
  /**
   * Look up variable name in current scope
   * @param name name of variable
   * @return type of that variable
   */
  public Type lookupVariable(String name) {
    return currentScope.lookup(name);
  }
  
  /**
   * 
   * @param name name of the function
   * @return return type of that function
   */
  public Type lookupFunction(String name) {
    return functions.lookup(name).returnType;
  }
  
  /**
   * Insert variable information into current scope
   * @param name name of a variable
   * @param type type of a variable
   * @throws DuplicateException if variable with the same name already exists
   */
  public void insertVariable(String name, Type type) throws DuplicateException {
    currentScope.put(name, type);
  }
  
  /**
   * 
   * @param name name of a function
   * @param type return type of a function
   * @throws DuplicateException if function with the same name already exists
   */
  public void insertFunction(String name, Type type) throws DuplicateException {
    functions.put(name, new FunctionEntry(type));
  }
  
  /**
   * Change the type of a variable under current scope
   * @param name name of a variable
   * @param type type of a variable
   */
  public void setVariableType(String name, Type type) {
    currentScope.set(name, type);
  }
  
  /**
   * Change the return type of a function
   * @param name
   * @param type
   */
  public void setFunctionType(String name, Type type) {
    functions.lookup(name).returnType = type;
  }
  
  @Override
  public void dump(IndentingWriter out) {
    out.println("Table {");
    out.indent();
    
    // print variables
    for (Entry<String, Type> variablEntry : globalVariables) {
      out.print(variablEntry.getKey() + " = ");
      out.println(variablEntry.getValue());
    }
    
    //print functions
    for (Entry<String, FunctionEntry> functionEntry : functions) {
      out.println(functionEntry.getKey() + " = Table {");
      out.indent();
      
      out.print("'return type' = ");
      out.println(functionEntry.getValue().returnType);
      for (Entry<String, Type> variablEntry : functionEntry.getValue().variables) {
        out.print(variablEntry.getKey() + " = ");
        out.println(variablEntry.getValue());
      }
      
      out.outdent();
      out.println("}");
    }
    
    out.outdent();
    out.println("}");
  }
  
  /**
   * Class represents a function entry in a symbol table
   *
   */
  private class FunctionEntry {
    
    private Type returnType;
    private final ImpTable<Type> variables; // local variables and parameters
    
    private FunctionEntry(Type returnType) {
      this.returnType = returnType;
      variables = new ImpTable<Type>();
    }
  }
}
