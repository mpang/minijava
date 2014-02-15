package typechecker.implementation;

import typechecker.ErrorReport;
import typechecker.implementation.SymbolTable.FunctionSignature;
import util.ImpTable.DuplicateException;
import visitor.Visitor;
import ast.*;

/**
 * This visitor implements Phase 1 of the TypeChecker. It constructs the symboltable.
 * 
 * @author norm
 */
public class BuildSymbolTableVisitor implements Visitor<SymbolTable> {
	
	private final SymbolTable symbolTable = new SymbolTable();
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
	public SymbolTable visit(Program n) {
	  throw new Error("Not implemented");
	}
	
	@Override
	public <T extends AST> SymbolTable visit(NodeList<T> ns) {
		for (int i = 0; i < ns.size(); i++) {
			ns.elementAt(i).accept(this);
		}
		return null;
	}

	@Override
	public SymbolTable visit(Assign n) {
	  throw new Error("Not implemented");
	}
	

	@Override
	public SymbolTable visit(IdentifierExp n) {
		if (symbolTable.lookupVariable(n.name) == null) {
			errors.undefinedId(n.name);
		}
		return null;
	}
	
	@Override
	public SymbolTable visit(BooleanType n) {
		return null;
	}

	@Override
	public SymbolTable visit(IntegerType n) {
		return null;
	}

	@Override
	public SymbolTable visit(Print n) {
		n.exp.accept(this);
		return null;
	}

	@Override
	public SymbolTable visit(LessThan n) {
		n.e1.accept(this);
		n.e2.accept(this);
		return null;
	}

	@Override
	public SymbolTable visit(Plus n) {
		n.e1.accept(this);
		n.e2.accept(this);
		return null;
	}

	@Override
	public SymbolTable visit(Minus n) {
		n.e1.accept(this);
		n.e2.accept(this);
		return null;
	}

	@Override
	public SymbolTable visit(Times n) {
		n.e1.accept(this);
		n.e2.accept(this);
		return null;
	}

	@Override
	public SymbolTable visit(IntegerLiteral n) {
		return null;
	}

	@Override
	public SymbolTable visit(Not not) {
		not.e.accept(this);
		return null;
	}
	
  @Override
  public SymbolTable visit(MainClass n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(ClassDecl n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(MethodDecl n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(VarDecl n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(IntArrayType n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(ObjectType n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(Block n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(If n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(While n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(ArrayAssign n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(BooleanLiteral n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(And n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(ArrayLength n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(ArrayLookup n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(Call n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(NewArray n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(NewObject n) {
    throw new Error("Not implemented");
  }

  @Override
  public SymbolTable visit(This n) {
    throw new Error("Not implemented");
  }

	///////////////////// Helpers ///////////////////////////////////////////////
	
	private void addVariable(SymbolTable table, String name, Type type) {
	  try {
      table.insertVariable(name, type);
    } catch (DuplicateException e) {
      errors.duplicateDefinition(name);
    }
	}
	
	private void addFunction(SymbolTable table, String name, FunctionSignature functionSignature) {
	  try {
      table.insertFunction(name, functionSignature);
    } catch (DuplicateException e) {
      errors.duplicateDefinition(name);
    }
	}
}