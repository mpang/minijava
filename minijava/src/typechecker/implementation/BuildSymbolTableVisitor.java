package typechecker.implementation;

import java.util.ArrayList;
import java.util.List;

import typechecker.ErrorReport;
import typechecker.implementation.MethodEntry.MethodSignature;
import util.ImpTable;
import util.ImpTable.DuplicateException;
import visitor.DefaultVisitor;
import ast.AST;
import ast.ClassDecl;
import ast.MainClass;
import ast.MethodDecl;
import ast.NodeList;
import ast.Program;
import ast.Type;
import ast.VarDecl;
import ast.VarDecl.Kind;

/**
 * This visitor implements Phase 1 of the TypeChecker. It constructs the symboltable.
 * 
 * @author norm
 */
public class BuildSymbolTableVisitor extends DefaultVisitor<ImpTable<ClassEntry>> {
	
	private final ImpTable<ClassEntry> symbolTable = new ImpTable<ClassEntry>();
	private ClassEntry currentClass;
	private MethodEntry currentMethod;
	private final ErrorReport errors;
	
	public BuildSymbolTableVisitor(ErrorReport errors) {
		this.errors = errors;
	}

	/////////////////// Phase 1 ///////////////////////////////////////////////////////
	// In our implementation, Phase 1 builds up a single symbol table containing all the
	// identifiers defined in an Expression program. 
	//
	// We also check for duplicate identifier definitions 

	@Override
	public ImpTable<ClassEntry> visit(Program n) {
	  n.mainClass.accept(this);
	  n.classes.accept(this);
	  return symbolTable;
	}
	
	@Override
	public <T extends AST> ImpTable<ClassEntry> visit(NodeList<T> ns) {
		for (int i = 0; i < ns.size(); i++) {
			ns.elementAt(i).accept(this);
		}
		return null;
	}
	
  @Override
  public ImpTable<ClassEntry> visit(MainClass n) {
    addClass(n.className, new ClassEntry(n.className, new ImpTable<Type>(), new ImpTable<MethodEntry>()));
    return null;
  }

  @Override
  public ImpTable<ClassEntry> visit(ClassDecl n) {
    currentClass = new ClassEntry(n.name, new ImpTable<Type>(), new ImpTable<MethodEntry>());
    
    if (!n.superName.isEmpty()) {
      if (!symbolTable.containsKey(n.superName)) {
        errors.undefinedId(n.superName);
      }
      currentClass.setSuperClass(symbolTable.lookup(n.superName));
    }

    n.vars.accept(this);
    n.methods.accept(this);
    
    addClass(n.name, currentClass);
    currentClass = null;
    return null;
  }

  @Override
  public ImpTable<ClassEntry> visit(MethodDecl n) {
    List<Type> paramTypes = new ArrayList<Type>();
    for (VarDecl paramDecl : n.formals) {
      paramTypes.add(paramDecl.type);
    }
    
    currentMethod = new MethodEntry(new MethodSignature(n.returnType, paramTypes), currentClass);
    
    n.formals.accept(this);
    n.vars.accept(this);
    
    addMethod(n.name, currentMethod);
    currentMethod = null;
    return null;
  }

  @Override
  public ImpTable<ClassEntry> visit(VarDecl n) {
    addVariable(n);
    return null;
  }

  ///////////////////// HELPERS //////////////////////////////////////
  
  private void addClass(String name, ClassEntry entry) {
    try {
      symbolTable.put(name, entry);
    } catch (DuplicateException e) {
      errors.duplicateDefinition(name);
    }
  }
  
  private void addMethod(String name, MethodEntry entry) {
    try {
      currentClass.insertMethod(name, entry);
    } catch (DuplicateException e) {
      errors.duplicateDefinition(name);
    }
  }
  
  private void addVariable(VarDecl var) {
    try {
      if (var.kind == Kind.FIELD) {
        currentClass.insertField(var.name, var.type);
      } else {
        currentMethod.insertVariable(var.name, var.type);
      }
    } catch (DuplicateException e) {
      errors.duplicateDefinition(var.name);
    }
  }
}