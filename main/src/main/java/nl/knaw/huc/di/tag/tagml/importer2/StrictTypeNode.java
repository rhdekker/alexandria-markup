package nl.knaw.huc.di.tag.tagml.importer2;

import org.antlr.v4.runtime.Token;

/*
 * @author: Ronald Haentjens Dekker
 */
public class StrictTypeNode extends ExpectationTreeNode {
    public StrictTypeNode(int default_beginOpenMarkup) {
        super(default_beginOpenMarkup);
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
