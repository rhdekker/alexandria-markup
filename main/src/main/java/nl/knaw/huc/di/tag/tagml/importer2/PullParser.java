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

    private ExpectationTreeNode expectations;

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
            // evaluate token against expectation
            expectations.evaluateToken(t);
            // based on the current expectations and the actual we generate new expectations
            this.expectations = rewriteExpectations(expectations, t);
        }

        return null;
    }

    private ExpectationTreeNode rewriteExpectations(ExpectationTreeNode expectations, Token t) {
        if (expectations.getClass() == AndNode.class) {
            return rewriteAndExpectations((AndNode) expectations, t);
        } else if (expectations.getClass() == TerminalNode.class) {
            return rewriteTerminalExpectations((TerminalNode)expectations, t);
        }
            {
            throw new UnsupportedOperationException("Unknown expectation type "+expectations.getClass().getSimpleName()+" to rewrite!");
        }
    }

    private ExpectationTreeNode rewriteTerminalExpectations(TerminalNode expectations, Token t) {
        return null;
    }

    private ExpectationTreeNode rewriteAndExpectations(AndNode andExpectations, Token t) {
        if (andExpectations.children.size()!=2) {
            throw new RuntimeException("Implement AND rewrite rule for more than two children!");
        }
        // NOTE: In case of an And it is always the leftmost node that is matched
        // the left child node is matched, we can forget about it.
        // The new expectation is the right child node
        ExpectationTreeNode rightNode = andExpectations.getRightNode();
        return rightNode;
    }
}