package ast.types;

import ast.AbstractASTNode;
import ast.Type;
import ast.definitions.VariableDefinition;

import java.util.List;

public class FunctionType extends AbstractASTNode implements Type {

    private Type type;
    private List<VariableDefinition> parameters;

    public FunctionType(Type type, List<VariableDefinition> parameters, int column, int line) {
        super(column, line);
        this.type = type;
        this.parameters = parameters;
    }

    public Type getType() { return type; }
    public List<VariableDefinition> getParameters() { return parameters; }
}