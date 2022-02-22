package ast.expressions;

import ast.AbstractASTNode;
import ast.Expression;

public class CharLiteral extends AbstractASTNode implements Expression {

    private final char value;

    public CharLiteral(char value, int column, int line) {
        super(column, line);
        this.value = value;
    }

    public char getValue() {
        return value;
    }
}