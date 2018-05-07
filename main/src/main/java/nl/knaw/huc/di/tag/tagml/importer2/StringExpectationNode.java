package nl.knaw.huc.di.tag.tagml.importer2;

import org.antlr.v4.runtime.Token;

/*
 * @author: Ronald Haentjens Dekker
 *
 * This string should be matched in literal form
 */
class StringExpectationNode extends ExpectationTreeNode {
    StringExpectationNode(String expectedString) {
        super(-1);
    }

    @Override
    public ExpectationTreeNode evaluateToken(Token t) throws ExpectationError {
        System.out.println(t.getType());
        throw new RuntimeException("blalbab");
        //return null;
    }
}
