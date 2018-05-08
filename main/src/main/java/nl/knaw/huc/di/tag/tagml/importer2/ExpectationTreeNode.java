package nl.knaw.huc.di.tag.tagml.importer2;

import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

/*
 * Class ExpectationTreeNode
 *
 * @author: Ronald Haentjens Dekker
 * date: 02-05-2018
 *
 * We express the expected Tokens as nodes of different type in a tree
 */
public abstract class ExpectationTreeNode {
//    private final ExpectationTreeNode parent;
    final List<ExpectationTreeNode> children;

    ExpectationTreeNode() {
//        this.parent = null;
        this.children = new ArrayList<>();
    }

    int size() {
        return children.size();
    }

    public abstract ExpectationTreeNode evaluateToken(Token t) throws ExpectationError;


}
