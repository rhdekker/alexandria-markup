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

    public DocumentWrapper importTAGML(final String input) throws TAGMLSyntaxError {
        CharStream antlrInputStream = CharStreams.fromString(input);
        return importTAGML(antlrInputStream);
    }

    public DocumentWrapper importTAGML(InputStream input) throws TAGMLSyntaxError {
        try {
            CharStream antlrInputStream = CharStreams.fromStream(input);
            return importTAGML(antlrInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }

    private DocumentWrapper importTAGML(CharStream antlrInputStream) throws TAGMLSyntaxError {
        TAGMLLexer lexer = new TAGMLLexer(antlrInputStream);
        ErrorListener errorListener = new ErrorListener();
        lexer.addErrorListener(errorListener);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        List<Token> tokens = tokenStream.getTokens();
        System.out.println(tokens);
        return null;
    }
}