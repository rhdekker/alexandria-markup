package nl.knaw.huc.di.tag.tagml.importer2;

import org.antlr.v4.runtime.Token;

/*
 * @author: Ronald Haentjens Dekker
 */
public class StrictTypeNode extends ExpectationTreeNode {
    private final int type;

    public StrictTypeNode(int expectedType) {
        this.type = expectedType;
    }

    // check whether the next token t is equal to expectation
    // if not throw error!
    public ExpectationTreeNode evaluateToken(Token t) throws ExpectationError {
        if (t.getType() != getType()) {
            throw new ExpectationError(this, t);
        }
        return this;
    }

    public int getType() {
        return type;
    }

}
