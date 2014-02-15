package typechecker.implementation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import util.DefaultIndentable;
import util.ImpTable;
import util.ImpTable.DuplicateException;
import util.IndentingWriter;
import ast.Type;

public class SymbolTable extends DefaultIndentable {

  private final ImpTable<Type> globalVariables;
  private final ImpTable<FunctionEntry> functions;
  private FunctionEntry currentFunction;
  
  public SymbolTable() {
    globalVariables = new ImpTable<Type>();
    functions = new ImpTable<FunctionEntry>();
    currentFunction = null;
  }
  
  /**
   * Enters the scope of a function
   * @param functionName name of a function
   */
  public void enterScope(String functionName) {
    FunctionEntry functionEntry = functions.lookup(functionName);
    assert functionEntry != null;
    currentFunction = functionEntry;
  }
  
  /**
   * Return to global scope
   */
  public void exitScope() {
    currentFunction = null;
  }
  
  /**
   * Must call {@link #enterScope(String)} before calling this function
   * @return signature of current function scope
   */
  public FunctionSignature getCurrentFunctionSignature() {
    return currentFunction.functionSignature;
  }
  
  /**
   * Look up variable name in current scope
   * @param name name of variable
   * @return type of that variable
   */
  public Type lookupVariable(String name) {
    boolean inFunctionScope = currentFunction != null && currentFunction.variables.containsKey(name);
    return inFunctionScope ? currentFunction.variables.lookup(name) : globalVariables.lookup(name);
  }
  
  /**
   * 
   * @param name name of the function
   * @return return signature of that function
   */
  public FunctionSignature lookupFunction(String name) {
    FunctionEntry functionEntry = functions.lookup(name);
    return functionEntry == null ? null : functionEntry.functionSignature;
  }
  
  /**
   * Insert variable information into current scope
   * @param name name of a variable
   * @param type type of a variable
   * @throws DuplicateException if variable with the same name already exists
   */
  public void insertVariable(String name, Type type) throws DuplicateException {
    if (currentFunction == null) {
      globalVariables.put(name, type);
    } else {
      currentFunction.variables.put(name, type);
    }
  }
  
  /**
   * 
   * @param name name of a function
   * @param functionSignature signature of a function
   * @throws DuplicateException if function with the same name already exists
   */
  public void insertFunction(String name, FunctionSignature functionSignature) throws DuplicateException {
    functions.put(name, new FunctionEntry(functionSignature));
  }
  
  /**
   * Change the type of a variable under current scope
   * @param name name of a variable
   * @param type type of a variable
   */
  public void setVariableType(String name, Type type) {
    if (currentFunction == null) {
      globalVariables.set(name, type);
    } else {
      currentFunction.variables.set(name, type);
    }
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
      
      // print signature
      out.print("'signature' = (");
      Iterator<Type> itr = functionEntry.getValue().functionSignature.getParameterTypes().iterator();
      while (itr.hasNext()) {
      	out.print(itr.next());
      	if (itr.hasNext()) {
      		out.print(", ");
      	}
      }
      out.print(") --> ");
      out.println(functionEntry.getValue().functionSignature.returnType);
      
      // print variables
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
  static class FunctionEntry {
    
    private final FunctionSignature functionSignature;
    private final ImpTable<Type> variables; // both local variables and parameters
    
    private FunctionEntry(FunctionSignature functionSignature) {
      this.functionSignature = functionSignature;
      variables = new ImpTable<Type>();
    }
  }
  
  /**
   * Class represents a function signature (return type + list of parameter types)
   *
   */
  static class FunctionSignature {
    private Type returnType;
    private List<Type> parameterTypes;  // order matters
    
    FunctionSignature(Type returnType, List<Type> parameterTypes) {
      this.returnType = returnType;
      this.parameterTypes = parameterTypes;
    }
    
    Type getReturnType() {
      return returnType;
    }
    
    List<Type> getParameterTypes() {
      return new ArrayList<Type>(parameterTypes);
    }
    
    void setReturnType(Type returnType) {
      this.returnType = returnType;
    }
    
    void addParameterType(Type parameterType) {
      parameterTypes.add(parameterType);
    }
    
    void setParameterType(int index, Type parameterType) {
      parameterTypes.set(index, parameterType);
    }
  }
}
