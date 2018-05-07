package nl.knaw.huc.di.tag.tagml.importer2;

import org.antlr.v4.runtime.Token;

import java.util.List;

/*
 * author: Ronald Haentjens Dekker
 * date: 02-05-2018
 */
public class AndNode extends ExpectationTreeNode {
    AndNode() {
        //TODO: introduce leaf nodes
        super(1234567);
    }

    public AndNode(CharacterClassNode characterClassNode, TerminalNode terminalNode) {
        super(1234567);
        this.children.add(characterClassNode);
        this.children.add(terminalNode);
    }

    public AndNode(ExpectationTreeNode stringExpectationNode, StrictTypeNode characterClassNode, StrictTypeNode stringExpectationNode1) {
        super(123457);
        this.children.add(stringExpectationNode);
        this.children.add(characterClassNode);
        this.children.add(stringExpectationNode1);
    }

    public AndNode(List<ExpectationTreeNode> everyThingExceptTheFirstOne) {
        super(124433);
        this.children.addAll(everyThingExceptTheFirstOne);
    }

    @Override
    public ExpectationTreeNode evaluateToken(Token t) throws ExpectationError {
        // delegate the evaluation to the first child!
        return getFirstChildNode().evaluateToken(t);
    }

    private ExpectationTreeNode getFirstChildNode() {
        if (size() == 0) {
            throw new RuntimeException("Children of an AndNode should not be empty!");
        }
        return children.get(0);
    }

    public ExpectationTreeNode getRightNode() {
        if (size() != 2) {
            throw new RuntimeException("We cannot yet handle more than two children!");
        }
        return children.get(1);
    }

    @Override
    public String toString() {
        return "AndNode: "+this.children.toString();
    }
}
