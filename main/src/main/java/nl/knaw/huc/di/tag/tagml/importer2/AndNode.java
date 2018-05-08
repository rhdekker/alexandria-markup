package nl.knaw.huc.di.tag.tagml.importer2;

import org.antlr.v4.runtime.Token;

import java.util.Arrays;
import java.util.List;

/*
 * author: Ronald Haentjens Dekker
 * date: 02-05-2018
 */
public class AndNode extends ExpectationTreeNode {

    AndNode(ExpectationTreeNode... expectationTreeNodes) {
        this.children.addAll(Arrays.asList(expectationTreeNodes));
    }

    AndNode(List<ExpectationTreeNode> expectationTreeNodes) {
        this.children.addAll(expectationTreeNodes);
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

    ExpectationTreeNode getRightNode() {
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
