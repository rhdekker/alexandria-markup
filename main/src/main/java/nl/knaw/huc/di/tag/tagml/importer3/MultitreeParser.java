package nl.knaw.huc.di.tag.tagml.importer3;

import nl.knaw.huc.di.tag.tagml.TAGMLSyntaxError;
import nl.knaw.huc.di.tag.tagml.grammar.TAGMLLexer;
import nl.knaw.huc.di.tag.tagml.importer2.*;
import nl.knaw.huygens.alexandria.ErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/*
 * TAGML Multitree Parser
 *
 * Input: TAGMLLexer TokenStream
 * Imports based on detecting one or more tree structures
 *
 * @author: Ronald Haentjens Dekker
 * 13-05-2018
 */

public class MultitreeParser {

    private ExpectationTreeNode expectations;

    public MultitreeParser(ExpectationTreeNode expectations) {
        this.expectations = expectations;
    }

    public List<Tree> importTAGML(final String input) throws TAGMLSyntaxError, ExpectationError {
        CharStream antlrInputStream = CharStreams.fromString(input);
        return importTAGML(antlrInputStream);
    }

    public List<Tree> importTAGML(InputStream input) throws TAGMLSyntaxError, ExpectationError {
        try {
            CharStream antlrInputStream = CharStreams.fromStream(input);
            return importTAGML(antlrInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }

    private List<Tree> importTAGML(CharStream antlrInputStream) throws TAGMLSyntaxError, ExpectationError {
        TAGMLLexer lexer = new TAGMLLexer(antlrInputStream);
        ErrorListener errorListener = new ErrorListener();
        lexer.addErrorListener(errorListener);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        List<Token> tokens = tokenStream.getTokens();

        Tree firstTree = new Tree();

        for (Token t: tokens) {
            if (t.getType() == TAGMLLexer.DEFAULT_Text) {
                firstTree.setText(t.getText());
            }
            else if (t.getType() == -1) {
                break;
            }
            else {
                throw new RuntimeException("Unsupported token exception: "+t.toString());
            }
        }

        List<Tree> multitree = new ArrayList<>();
        multitree.add(firstTree);
        return multitree;
    }


}