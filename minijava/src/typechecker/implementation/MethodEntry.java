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

public class MethodEntry extends DefaultIndentable {

  private final MethodSignature methodSignature;
  private final ImpTable<Type> variables; // both local variables and parameters
  private final ClassEntry enclosedClass;
  
  MethodEntry(MethodSignature methodSignature, ClassEntry enclosedClass) {
    this.methodSignature = methodSignature;
    this.enclosedClass = enclosedClass;
    variables = new ImpTable<Type>();
  }
  
  MethodSignature getMethodSignature() {
    return methodSignature;
  }
  
  void insertVariable(String variableName, Type variableType) throws DuplicateException {
    variables.put(variableName, variableType);
  }
  
  Type lookupVariable(String variableName) {
    return variables.containsKey(variableName) ? variables.lookup(variableName)
                                               : enclosedClass.lookupField(variableName);
  }
  
  /**
   * Class represents a method signature (return type + list of parameter types)
   *
   */
  static class MethodSignature {
    
    private Type returnType;
    private List<Type> parameterTypes;  // order matters
    
    MethodSignature(Type returnType, List<Type> parameterTypes) {
      this.returnType = returnType;
      this.parameterTypes = parameterTypes;
    }
    
    Type getReturnType() {
      return returnType;
    }
    
    List<Type> getParameterTypes() {
      return new ArrayList<Type>(parameterTypes);
    }
    
    void addParameterType(Type parameterType) {
      parameterTypes.add(parameterType);
    }
  }

  @Override
  public void dump(IndentingWriter out) {
    out.println("Method Table {");
    out.indent();
    
    out.print("'signature' = (");
    Iterator<Type> paramTypeIterator = methodSignature.parameterTypes.iterator();
    while (paramTypeIterator.hasNext()) {
      Type paramType = paramTypeIterator.next();
      out.print(paramType);
      if (paramTypeIterator.hasNext()) {
        out.print(", ");
      }
    }
    out.println(") --> " + methodSignature.returnType);
    
    for (Entry<String, Type> variableEntry : variables) {
      out.println(variableEntry.getKey() + " = " + variableEntry.getValue());
    }
    
    out.outdent();
    out.println("}");
  }
}
