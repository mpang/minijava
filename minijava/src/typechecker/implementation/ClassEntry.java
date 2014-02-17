package typechecker.implementation;

import java.util.Map.Entry;

import util.DefaultIndentable;
import util.ImpTable;
import util.ImpTable.DuplicateException;
import util.IndentingWriter;
import ast.Type;

public class ClassEntry extends DefaultIndentable {

  private final ImpTable<Type> fields;
  private final ImpTable<MethodEntry> methods;
  private ClassEntry superClass;
  
  ClassEntry(ImpTable<Type> fields, ImpTable<MethodEntry> methods) {
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
    return fields.lookup(fieldName);
  }
  
  MethodEntry lookupMethod(String methodName) {
    return methods.lookup(methodName);
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
