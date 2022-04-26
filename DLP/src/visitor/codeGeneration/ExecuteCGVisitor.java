package visitor.codeGeneration;

import ast.Definition;
import ast.Statement;
import ast.definitions.FunctionDefinition;
import ast.program.Program;
import ast.statements.*;
import visitor.codeGeneration.cg.CodeGenerator;

/**
 * Program:
 *
 * execute[[Program: program -> definition+]] =
 *
 * <call main>
 * <halt>
 *
 * for (var def : definition*) {
 * 	execute[[definition]]
 * }
 * Function definition:
 *
 * execute[[FunctionDefinition: definition -> type ID statement*]] =
 * <ID:>
 * var varDef = statement*.stream()
 * 	.filter(stmt -> stmt instanceof Definition)
 * 	.map(def -> ((VariableDefinition)def))
 * 	.toList()
 *
 * int bytesLocals = varDef.isEmpty() ? 0 :
 * 	-varDef.get(varDef.size()-1).offset;
 * <enter > bytesLocals
 *
 * int bytesParams = type.parameters.stream()
 * 	.mapToInt(param -> param.type.numberOfBytes())
 * 	.sum();
 * int bytesReturn=type.returnType.numberOfBytes();
 * for (var stmt : statement*) {
 * 	execute[[stmt]](bytesLocals, bytesParams, bytesReturn)
 * }
 * if (type.returnType instanceOf Void)
 * 	<ret > bytesReturn <, > bytesLocals <, > bytesParam
 * Read:
 *
 * execute[[Read: statement -> expression]] =
 *
 * <in> expression.type.suffix
 * Write
 *
 * execute[[Write: statement -> expression]] =
 *
 * <out> expression.type.suffix
 * Assignment
 *
 * execute[[Assignent: statement -> expression1 expression2]] =
 *
 * address[[expression1]]
 * value[[expression2]]
 * <store> expression1.type.suffix
 */
public class ExecuteCGVisitor extends AbstractCGVisitor<FunctionDefinition, Void> {

	private final CodeGenerator cg;

	private final ValueCGVisitor vcg;
	private final AddressCGVisitor acg;

	public ExecuteCGVisitor(String filename) {
		cg = new CodeGenerator(filename);
		acg = new AddressCGVisitor(cg);
		vcg = new ValueCGVisitor(cg);
		vcg.setAcg(acg);
		acg.setVcg(vcg);
	}

	@Override
	public Void visit(Program e, FunctionDefinition param) {
		cg.program();
		for (Definition def : e.getDefinitions()) {
			def.accept(this, null);
		}
		// close file
		cg.close();
		return null;
	}

	@Override
	public Void visit(FunctionDefinition e, FunctionDefinition param) {
		cg.printLine(e.getLine());
		cg.createFunctionFrame(e);
		for (Statement stmt : e.getBody()) {
			stmt.accept(this, e);
		}
		cg.createReturnSequence(e);
		return null;
	}

	@Override
	public Void visit(WriteStatement e, FunctionDefinition param) {
		cg.printLine(e.getLine());
		cg.createComment(1,"write");
		e.getExpression().accept(vcg, null);
		cg.write(e.getExpression().getType());
		return null;
	}

	@Override
	public Void visit(ReadStatement e, FunctionDefinition param) {
		cg.printLine(e.getLine());
		cg.createComment(1,"read");
		e.getExpression().accept(acg, null);
		cg.read(e.getExpression().getType());
		return null;
	}

	@Override
	public Void visit(AssignmentStatement e, FunctionDefinition param) {
		cg.printLine(e.getLine());
		e.getLeftExpression().accept(acg, null);
		e.getRightExpression().accept(vcg, null);
		cg.assign(e.getLeftExpression().getType());
		return null;
	}

	@Override
	public Void visit(WhileStatement e, FunctionDefinition param) {
		String condLabel = cg.nextLabel();
		String exitLabel = cg.nextLabel();
		cg.createComment(0, "while");
		cg.pushLabel(condLabel);
		e.getExpression().accept(vcg, null);
		cg.jnz(exitLabel);

		e.getBody().forEach(stmt -> stmt.accept(this, param));
		cg.jmp(condLabel);
		cg.pushLabel(exitLabel);
		return null;
	}

	@Override
	public Void visit(IfStatement e, FunctionDefinition param) {
		String elseCondLabel = cg.nextLabel();
		String exitLabel = cg.nextLabel();

		e.getExpression().accept(vcg, null);
		cg.jz(exitLabel);
		e.getIfBody().forEach(stmt -> stmt.accept(this, param));
		cg.jmp(exitLabel);
		cg.pushLabel(elseCondLabel);
		e.getElseBody().forEach(stmt -> stmt.accept(this, param));
		cg.pushLabel(exitLabel);
		return null;
	}
}
