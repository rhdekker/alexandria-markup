package nl.knaw.huc.di.tag.tagml.importer2;

import org.antlr.v4.runtime.Token;

/*
 * Author: Ronald Haentjens Dekker
 * date: 02-05-2018
 */
public class CharacterClassNode extends StrictTypeNode {
    // This node represents expectation of a text token, which contains characters
    // denoted in a character class regular expression

    public CharacterClassNode() {
        super(6);
    }

     // check whether the next token t is equal to expectation
     // if not throw error!
    public ExpectationTreeNode evaluateToken(Token t) throws ExpectationError {
        if (t.getType() != getType()) {
            throw new ExpectationError(this, t);
        }
        // TODO: check regular expression!
        return this;
    }

}
