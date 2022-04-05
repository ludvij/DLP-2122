package ast.types;

import ast.Type;
import ast.types.error.ErrorType;
import visitor.Visitor;

public class CharType extends AbstractType {

    private static CharType instance;

    private CharType() {
        super(0, 0);
    }

    public static CharType get() {
        if (instance == null) {
            instance = new CharType();
        }
        return instance;
    }

    @Override
    public Type arithmetic(Type other, int line, int column) {
        if (this != other) {
            new ErrorType("Expected: char, Given: " + other,
                line, column);
        }
        return get();
    }

    @Override
    public Type comparison(Type other, int line, int column) {
        if (this != other) {
            new ErrorType("Expected: char, Given: " + other,
                line, column);
        }
        return IntType.get();
    }

    @Override
    public boolean isBuiltin() {
        return true;
    }

    @Override
    public <TP, TR> TR accept(Visitor<TP, TR> v, TP param) {
        return v.visit(this, param);
    }

    @Override
    public String toString() {
        return "char";
    }

    @Override
    public int numberOfBytes() {
        return 1;
    }
}
