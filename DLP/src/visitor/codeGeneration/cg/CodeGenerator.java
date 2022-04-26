package visitor.codeGeneration.cg;


import ast.Definition;
import ast.Expression;
import ast.Type;
import ast.definitions.FunctionDefinition;
import ast.definitions.VariableDefinition;
import ast.expressions.*;
import ast.types.CharType;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeGenerator {

	private int lastLine = 0;

	private final Map<String, String> OperatorsMap = Stream.of(new String[][]{
			{"+", "add" },
			{"-", "sub" },
			{"*", "mul" },
			{"/", "div" },
			{"%", "mod" },
			{"||", "or" },
			{"&&", "and"},
			{"<", "lt"  },
			{">", "gt"  },
			{"<=", "lte"},
			{">=", "gte"},
			{"==", "eq" },
			{"!=", "ne" }
	}).collect(Collectors.toMap(x -> x[0], x -> x[1]));

	private final OutFileSingleton file = OutFileSingleton.getInstance();


	public CodeGenerator(String filename) {
		file.open(filename);
	}

	public void printLine(int line) {
		if (line > lastLine) {
			lastLine = line;
			file.write("\n#line\t"+lastLine+"\n");
		}
	}

	public void comparison(ComparisonOperation op) {
		String operation = OperatorsMap.get(op.getOperator());

		String res = "\t"+ operation + comparisonSuffix(op.getExpressionLeft().getType())+ "\n";
		file.write(res);
	}

	public void arithmetic(ArithmeticOperation op) {
		String operation = OperatorsMap.get(op.getOperator());
		String res = "\t"+operation + op.getType().getSuffix()+ "\n";
		file.write(res);
	}

	public void logical(LogicalOperation op) {

		String res = "\t"+OperatorsMap.get(op.getOperator())+ "\n";
		file.write(res);
	}

	public void pushAddress(VariableDefinition def) {
		String res = "";
		if (def.getScope() == 0) {
			res += "\t"+"pusha " + def.getOffset() + "\n";
		} else {
			res += "\t"+"push bp\n";
			res += "\t"+"pushi " + def.getOffset() + "\n";
			res += "\t"+"addi\n";
		}
		file.write(res);
	}

	// PRIVATE METHODS
	private String comparisonSuffix(Type type) {
		if (type instanceof CharType) return "i";
		else return type.getSuffix();
	}

	public void pushInt(IntLiteral e) {
		String res = "\t"+"pushi " + e.getValue() + "\n";
		file.write(res);
	}

	public void pushChar(CharLiteral e) {
		String res = "\t"+"pushb " + (int)e.getValue() + "\n";
		file.write(res);
	}

	public void pushDouble(DoubleLiteral e) {
		String res = "\t"+"pushf " + e.getValue() + "\n";
		file.write(res);
	}

	public void convert(Type caster, Type castee) {
		String res = castee.convert(caster);
		file.write(res);
	}

	public void unaryMinus(Expression expression) {
		String res = "\t"+"push " + expression.getType().getSuffix() + " 0\n";
		file.write(res);
	}

	public void promote(Type promoter, Type promotee) {
		String res = promotee.promote(promoter);
		file.write(res);
	}

	public void not() {
		String res = "\t"+"not\n";
		file.write(res);
	}

	public void assign(Type type) {
		String res = "\t"+"store" + type.getSuffix()+ "\n";
		file.write(res);
	}

	public void write(Type type) {
		String res = "\t"+"out" + type.getSuffix()+"\n";
		file.write(res);
	}

	public void read(Type type) {
		String res = "\t"+"in" + type.getSuffix()+ "\n";
		res +="\t"+ "store" + type.getSuffix()+"\n";
		file.write(res);
	}

	public void createFunctionFrame(FunctionDefinition e) {
		var varDef = e.getBody().stream()
				.filter(stmt -> stmt instanceof Definition)
				.map(def -> ((VariableDefinition)def))
				.toList();

		int bytesLocals = varDef.isEmpty() ? 0 : -varDef.get(varDef.size()-1).getOffset();

		int bytesParams = e.getType().getParameters().stream()
				.mapToInt(x -> x.getType().numberOfBytes())
				.sum();
		int bytesReturn = e.getType().getType().numberOfBytes();

		e.setBytesLocals(bytesLocals);
		e.setBytesParam(bytesParams);
		e.setBytesReturn(bytesReturn);

		String res = e.getName() + ":\n";
		commentFnFrame(e);
		res += "\t"+"enter " + bytesLocals + "\n";
		file.write(res);
	}

	private void commentFnFrame(FunctionDefinition e) {
		createComment(1,"Parameters");
		for (var i : e.getType().getParameters())
			createComment(1,i + "(offset " + i.getOffset()+")");
		createComment(1,"Local variables");
		for (var i : e.getBody()){
			if (i instanceof VariableDefinition vd){
				createComment(1,vd + "(offset " + vd.getOffset()+")");
			}
		}

	}

	public void createReturnSequence(FunctionDefinition e) {
		String res = "\t"+"ret " + e.getBytesReturn() + ", " + e.getBytesLocals() + ", " + e.getBytesParam() + "\n";
		file.write(res);
	}

	public void program() {
		createComment(0, "Invocation of main function");
		String res = "call main\nhalt\n";
		file.write(res);
	}

	public void close() {
		file.close();
	}

	public void createComment(int tabLevel, String... lines){
		String tabs = "";
		for (int i = 0; i < tabLevel; i++) tabs += "\t";
		String comment = tabs + "' *";
		String res = Stream.of(lines).map(x -> comment + x).collect(Collectors.joining("\n")) + "\n";
		file.write(res);
	}

	public void loadVariable(Definition def) {
		String res = "\t"+"load" + def.getType().getSuffix() + "\n";
		file.write(res);
	}
}
