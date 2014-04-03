package typechecker.implementation;

import java.util.HashSet;
import java.util.Set;

import typechecker.ErrorReport;
import typechecker.implementation.MethodEntry.MethodSignature;
import util.ImpTable;
import visitor.Visitor;
import ast.*;

/**
 * This class implements Phase 2 of the Type Checker. This phase
 * assumes that we have already constructed the program's symbol table in
 * Phase1.
 * <p>
 * Phase 2 checks for the use of undefined identifiers and type errors.
 * <p>
 * Visitors may return a Type as a result. Generally, only visiting 
 * an expression or a type actually returns a type.
 * <p>
 * Visiting other nodes just returns null.
 * 
 * @author kdvolder
 */
public class TypeCheckVisitor implements Visitor<Type> {

	/**
	 * The place to send error messages to.
	 */
	private ErrorReport errors;

	/**
	 * The symbol table from Phase 1. 
	 */
	private ImpTable<ClassEntry> symbolTable;
	private ClassEntry currentClass;
	private MethodEntry currentMethod;


	public TypeCheckVisitor(ImpTable<ClassEntry> symbolTable, ErrorReport errors) {
		this.symbolTable = symbolTable;
		this.errors = errors;
	}

	//// Helpers /////////////////////

	/**
	 * Check whether the type of a particular expression is as expected.
	 */
	private void check(Expression exp, Type expected) {
		check(exp, expected, exp.accept(this));
	}

	/**
	 * Check whether two types in an expression are the same
	 */
	private void check(Expression exp, Type expected, Type actual) {
		if (!assignableFrom(expected, actual)) {
		  errors.typeError(exp, expected, actual);
		}
	}	

	private boolean assignableFrom(Type expected, Type actual) {
	  // both are non-object types
	  if (!(expected instanceof ObjectType) && !(actual instanceof ObjectType)) {
	    return expected.equals(actual);
	  }
	  
	  // both are object types
	  if ((expected instanceof ObjectType) && (actual instanceof ObjectType)) {
  	  // check subtyping
  	  ObjectType expectedObjectType = (ObjectType) expected;
  	  ObjectType actualObjectType = (ObjectType) actual;
  	  Set<String> actualObjectTypes = new HashSet<String>();
  	  ClassEntry clazz = symbolTable.lookup(actualObjectType.name);
  	  while (clazz != null) {
  	    actualObjectTypes.add(clazz.className);
  	    clazz = clazz.getSuperClass();
  	  }
  	  
  	  return actualObjectTypes.contains(expectedObjectType.name);
	  }
	  
	  // one is object and the other is not
	  return false;
	}
	
	///////// Visitor implementation //////////////////////////////////////

	@Override
	public <T extends AST> Type visit(NodeList<T> ns) {
		for (int i = 0; i < ns.size(); i++) {
			ns.elementAt(i).accept(this);
		}
		return null;
	}

	@Override
	public Type visit(Program n) {
	  n.mainClass.accept(this);
	  n.classes.accept(this);
	  return null;
	}

	@Override
	public Type visit(BooleanType n) {
		return n;
	}

	@Override
	public Type visit(IntegerType n) {
		return n;
	}

	@Override
	public Type visit(Print n) {
		check(n.exp, new IntegerType());
		return null;
	}

	@Override
	public Type visit(Assign n) {
	  check(n.value, new IdentifierExp(n.name).accept(this), n.value.accept(this));
	  return null;
	}

	@Override
	public Type visit(LessThan n) {
		check(n.e1, new IntegerType());
		check(n.e2, new IntegerType());
		n.setType(new BooleanType());
		return n.getType();
	}

	@Override
	public Type visit(Plus n) {
		check(n.e1, new IntegerType());
		check(n.e2, new IntegerType());
		n.setType(new IntegerType());
		return n.getType();
	}

	@Override
	public Type visit(Minus n) {
		check(n.e1, new IntegerType());
		check(n.e2, new IntegerType());
		n.setType(new IntegerType());
		return n.getType();
	}

	@Override
	public Type visit(Times n) {
		check(n.e1, new IntegerType());
		check(n.e2, new IntegerType());
		n.setType(new IntegerType());
		return n.getType();
	}

	@Override
	public Type visit(IntegerLiteral n) {
		n.setType(new IntegerType());
		return n.getType();
	}

	@Override
	public Type visit(IdentifierExp n) {
	  Type type = currentMethod.lookupVariable(n.name);
	  if (type == null) {
	    errors.undefinedId(n.name);
	  }
	  
	  n.setType(type);
	  return n.getType();
	}

	@Override
	public Type visit(Not n) {
		check(n.e, new BooleanType());
		n.setType(new BooleanType());
		return n.getType(); 
	}

  @Override
  public Type visit(MainClass n) {
    n.statement.accept(this);
    return null;
  }

  @Override
  public Type visit(ClassDecl n) {
    currentClass = symbolTable.lookup(n.name);
    n.methods.accept(this);
    currentClass = null;
    return null;
  }

  @Override
  public Type visit(MethodDecl n) {
    currentMethod = currentClass.lookupMethod(n.name);
    check(n.returnExp, n.returnType);
    n.statements.accept(this);
    currentMethod = null;
    return null;
  }

  @Override
  public Type visit(VarDecl n) {
    return null;
  }

  @Override
  public Type visit(IntArrayType n) {
    return n;
  }

  @Override
  public Type visit(ObjectType n) {
    return n;
  }

  @Override
  public Type visit(Block n) {
    n.statements.accept(this);
    return null;
  }

  @Override
  public Type visit(If n) {
    check(n.tst, new BooleanType());
    n.thn.accept(this);
    n.els.accept(this);
    return null;
  }

  @Override
  public Type visit(While n) {
    check(n.tst, new BooleanType());
    n.body.accept(this);
    return null;
  }

  @Override
  public Type visit(ArrayAssign n) {
    check(new IdentifierExp(n.name), new IntArrayType());
    check(n.index, new IntegerType());
    check(n.value, new IntegerType());
    return null;
  }

  @Override
  public Type visit(BooleanLiteral n) {
    n.setType(new BooleanType());
    return n.getType();
  }

  @Override
  public Type visit(And n) {
    check(n.e1, new BooleanType());
    check(n.e2, new BooleanType());
    n.setType(new BooleanType());
    return n.getType();
  }

  @Override
  public Type visit(ArrayLength n) {
    check(n.array, new IntArrayType());
    n.setType(new IntegerType());
    return n.getType();
  }

  @Override
  public Type visit(ArrayLookup n) {
    check(n.array, new IntArrayType());
    check(n.index, new IntegerType());
    n.setType(new IntegerType());
    return n.getType();
  }

  @Override
  public Type visit(Call n) {
    // check if receiver is an object
    Type receiverType = n.receiver.accept(this);
    if (!(receiverType instanceof ObjectType)) {
      errors.typeError(n.receiver, new ObjectType("object"), receiverType);
    }
    
    // check whether the class is defined
    ObjectType objectType = (ObjectType) receiverType;
    if (!symbolTable.containsKey(objectType.name)) {
      errors.undefinedId(objectType.name);
    }
    
    // check whether the method is defined
    if (!symbolTable.lookup(objectType.name).containsMethod(n.name)) {
      errors.undefinedId(n.name);
    }
    
    MethodSignature methodSignature = symbolTable.lookup(objectType.name)
                                                 .lookupMethod(n.name)
                                                 .getMethodSignature();
    // arity check
    if (methodSignature.getParameterTypes().size() != n.rands.size()) {
      errors.arityMismatch(n.name, methodSignature.getParameterTypes().size(), n.rands.size());
    }
    
    // type check arguments
    for (int i = 0; i < n.rands.size(); i++) {
      check(n.rands.elementAt(i), methodSignature.getParameterTypes().get(i));
    }
    
    n.setType(methodSignature.getReturnType());
    return n.getType();
  }

  @Override
  public Type visit(NewArray n) {
    check(n.size, new IntegerType());
    n.setType(new IntArrayType());
    return n.getType();
  }

  @Override
  public Type visit(NewObject n) {
    if (!symbolTable.containsKey(n.typeName)) {
      errors.undefinedId(n.typeName);
    }
    
    n.setType(new ObjectType(n.typeName));
    return n.getType();
  }

  @Override
  public Type visit(This n) {
    n.setType(new ObjectType(currentClass.className));
    return n.getType();
  }

  @Override
  public Type visit(InstanceOf n) {
    // check identifier is of object type
    Expression id = new IdentifierExp(n.identifier);
    Type idType = id.accept(this);
    if (!(idType instanceof ObjectType)) {
      errors.typeError(id, new ObjectType("object"), idType);
    }
    
    // check class is defined
    if (!symbolTable.containsKey(n.className)) {
      errors.undefinedId(n.className);
    }
    
    n.setType(new BooleanType());
    return n.getType();
  }
}
