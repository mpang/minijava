package typechecker.implementation;

import java.util.ArrayList;

import typechecker.ErrorReport;
import typechecker.implementation.SymbolTable.FunctionSignature;
import util.ImpTable.DuplicateException;
import visitor.Visitor;
import ast.AST;
import ast.Assign;
import ast.BooleanType;
import ast.Conditional;
import ast.ExpressionList;
import ast.FormalList;
import ast.FunctionCallExp;
import ast.FunctionDeclaration;
import ast.IdentifierExp;
import ast.IntegerLiteral;
import ast.IntegerType;
import ast.LessThan;
import ast.Minus;
import ast.NodeList;
import ast.Not;
import ast.ParameterDeclaration;
import ast.Plus;
import ast.Print;
import ast.Program;
import ast.Times;
import ast.Type;
import ast.UnknownType;

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
		n.statements.accept(this);
		n.print.accept(this); // process all the "normal" classes.
		return symbolTable;
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
		n.value.accept(this);
		addVariable(symbolTable, n.name, new UnknownType());
		return null;
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
	public SymbolTable visit(Conditional n) {
		n.e1.accept(this);
		n.e2.accept(this);
		n.e3.accept(this);
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
	public SymbolTable visit(UnknownType n) {
		return null;
	}

  @Override
  public SymbolTable visit(ParameterDeclaration n) {
    addVariable(symbolTable, n.name, new UnknownType());
    return null;
  }

  @Override
  public SymbolTable visit(FunctionDeclaration n) {
    addFunction(symbolTable, n.name, new FunctionSignature(new UnknownType(), new ArrayList<Type>()));
    symbolTable.enterScope(n.name);
    n.parameters.accept(this);
    n.statements.accept(this);
    n.returnExpression.accept(this);
    symbolTable.exitScope();
    return null;
  }

  @Override
  public SymbolTable visit(FunctionCallExp n) {
    // TODO: allow forward declaration of function
    if (symbolTable.lookupFunction(n.name) == null) {
      errors.undefinedId(n.name);
    }
    n.arguments.accept(this);
    return null;
  }

  @Override
  public SymbolTable visit(FormalList n) {
    FunctionSignature currentFunctionSignature = symbolTable.getCurrentFunctionSignature();
    for (int i = 0; i < n.parameters.size(); i++) {
      n.parameters.elementAt(i).accept(this);
      currentFunctionSignature.addParameterType(new UnknownType());
    }
    return null;
  }

  @Override
  public SymbolTable visit(ExpressionList n) {
    n.expressions.accept(this);
    return null;
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