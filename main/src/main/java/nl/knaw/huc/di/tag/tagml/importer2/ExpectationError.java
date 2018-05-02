package nl.knaw.huc.di.tag.tagml.importer2;

import nl.knaw.huc.di.tag.tagml.grammar.TAGMLLexer;
import org.antlr.v4.runtime.Token;

/*
  * ExpectationError exception
  *
  * @author: Ronald Haentjens Dekker
  * date: 02-05-2018
 */
public class ExpectationError extends Throwable {
    private final ExpectationTreeNode expectations;
    private final Token token;

    public ExpectationError(ExpectationTreeNode expectations, Token token) {
        this.expectations = expectations;
        this.token = token;
    }

    @Override
    public String getMessage() {
        return "Expected: "+TAGMLLexer.VOCABULARY.getDisplayName(expectations.getType())+" : but got: "+TAGMLLexer.VOCABULARY.getDisplayName(token.getType());
    }
}
