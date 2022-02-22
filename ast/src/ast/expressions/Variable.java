package ast.expressions;

import ast.AbstractASTNode;
import ast.Expression;

public class Variable extends AbstractASTNode implements Expression {

    private final String name;

    public Variable(String name, int column, int line) {
        super(column, line);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}