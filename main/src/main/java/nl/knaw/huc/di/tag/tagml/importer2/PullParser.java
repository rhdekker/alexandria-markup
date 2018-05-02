package nl.knaw.huc.di.tag.tagml.importer2;

import nl.knaw.huc.di.tag.tagml.TAGMLSyntaxError;
import nl.knaw.huc.di.tag.tagml.grammar.TAGMLLexer;
import nl.knaw.huygens.alexandria.ErrorListener;
import nl.knaw.huygens.alexandria.storage.wrappers.DocumentWrapper;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

/*
 * TAGML Pull Parser
 *
 * Input: TAGMLLexer TokenStream
 * Imports and validates using rewrite rules based on tree grammars
 *
 * @author: Ronald Haentjens Dekker
 * 02-05-2018
 */

public class PullParser {

    private final ExpectationTreeNode expectations;

    public PullParser(ExpectationTreeNode expectations) {
        this.expectations = expectations;
    }

    public DocumentWrapper importTAGML(final String input) throws TAGMLSyntaxError, ExpectationError {
        CharStream antlrInputStream = CharStreams.fromString(input);
        return importTAGML(antlrInputStream);
    }

    public DocumentWrapper importTAGML(InputStream input) throws TAGMLSyntaxError, ExpectationError {
        try {
            CharStream antlrInputStream = CharStreams.fromStream(input);
            return importTAGML(antlrInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }

    private DocumentWrapper importTAGML(CharStream antlrInputStream) throws TAGMLSyntaxError, ExpectationError {
        TAGMLLexer lexer = new TAGMLLexer(antlrInputStream);
        ErrorListener errorListener = new ErrorListener();
        lexer.addErrorListener(errorListener);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        List<Token> tokens = tokenStream.getTokens();

        for (Token t: tokens) {
            // check whether the next token t is equal to expectation
            // if not throw error!
            if (t.getType() != expectations.getType()) {
                throw new ExpectationError(expectations, t);
            }
            // based on the current expectations and the actual we generate new expectations
            // TODO
        }





//        System.out.println(tokens);
//        System.out.println(tokens.get(0).getType());
//       // TAGMLLexer.
//        System.out.println(tokens.get(0).getText());
//
//        // In the first example the token is 6, which is the default text in the TAGMLLexer.
//        //Now I need to setup the expectations as nodes in a tree
//

        return null;
    }
}