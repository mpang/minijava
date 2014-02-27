package typechecker.implementation;

import java.util.Map.Entry;

import util.DefaultIndentable;
import util.ImpTable;
import util.ImpTable.DuplicateException;
import util.IndentingWriter;
import ast.Type;

public class ClassEntry extends DefaultIndentable {

  public final String className;
  private final ImpTable<Type> fields;
  private final ImpTable<MethodEntry> methods;
  private ClassEntry superClass;
  
  ClassEntry(String className, ImpTable<Type> fields, ImpTable<MethodEntry> methods) {
    this.className = className;
    this.fields = fields;
    this.methods = methods;
  }
  
  void setSuperClass(ClassEntry superClass) {
    this.superClass = superClass;
  }
  
  ClassEntry getSuperClass() {
    return superClass;
  }
  
  void insertField(String fieldName, Type fieldType) throws DuplicateException {
    fields.put(fieldName, fieldType);
  }
  
  void insertMethod(String methodName, MethodEntry methodEntry) throws DuplicateException {
    methods.put(methodName, methodEntry);
  }
  
  Type lookupField(String fieldName) {
    if (fields.containsKey(fieldName)) {
      return fields.lookup(fieldName);
    }
    
    return superClass != null ? superClass.lookupField(fieldName) : null;
  }
  
  MethodEntry lookupMethod(String methodName) {
    if (methods.containsKey(methodName)) {
      return methods.lookup(methodName);
    }
    
    return superClass != null ? superClass.lookupMethod(methodName) : null;
  }
  
  boolean containsMethod(String methodName) {
    return methods.containsKey(methodName) || (superClass != null && superClass.containsMethod(methodName));
  }
  
  public int getNumOfFields() {
    return fields.size();
  }

  @Override
  public void dump(IndentingWriter out) {
    out.println("Class Table {");
    out.indent();
    
    for (Entry<String, Type> fieldEntry : fields) {
      out.println(fieldEntry.getKey() + " = " + fieldEntry.getValue());
    }
    
    for (Entry<String, MethodEntry> methodEntry : methods) {
      out.print(methodEntry.getKey() + " = ");
      methodEntry.getValue().dump(out);
    }
    
    out.outdent();
    out.println("}");
  }
}
