package ast.expressions;

import ast.AbstractASTNode;
import ast.Expression;
import ast.Statement;

import java.util.List;

public class FunctionInvocation extends AbstractASTNode implements Statement, Expression {

    private String name;
    private List<Expression> parameters;

    public FunctionInvocation(String name, List<Expression> parameters, int column, int line) {
        super(column, line);
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() { return name; }
    public List<Expression> getParameters() { return parameters; }
}