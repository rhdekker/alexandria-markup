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
public class ExpectationTreeNode {
//    private final ExpectationTreeNode parent;
    private final int type;
    final List<ExpectationTreeNode> children;

    ExpectationTreeNode(int type) {
//        this.parent = null;
        this.type = type;
        this.children = new ArrayList<>();
    }

//    private ExpectationTreeNode(ExpectationTreeNode parent, int type) {
//        this.parent = parent;
//        this.type = type;
//    }

    public int getType() {
        return type;
    }

    int size() {
        return children.size();
    }

    // check whether the next token t is equal to expectation
    // if not throw error!
    public ExpectationTreeNode evaluateToken(Token t) throws ExpectationError {
        if (t.getType() != getType()) {
            throw new ExpectationError(this, t);
        }
        return this;
    }

}
