package ast.expressions;

import ast.AbstractASTNode;
import ast.Expression;
import semantic.Visitor;

public class ArithmeticOperation extends AbstractExpression {

    private String operator;
    private Expression expressionLeft;
    private Expression expressionRight;


    public ArithmeticOperation(String operator, Expression expressionLeft, Expression expressionRight, int column, int line) {
        super(column, line);
        this.expressionLeft = expressionLeft;
        this.expressionRight = expressionRight;
        this.operator = operator;
    }

    @Override
    public <TP, TR> TR accept(Visitor<TP, TR> v, TP param) {
        return v.visit(this, param);
    }

    public String getOperator() {
        return operator;
    }

    public Expression getExpressionLeft() {
        return expressionLeft;
    }

    public Expression getExpressionRight() {
        return expressionRight;
    }

    @Override
    public String toString() {
        return expressionLeft + " " + operator + " " + expressionRight;
    }
}
