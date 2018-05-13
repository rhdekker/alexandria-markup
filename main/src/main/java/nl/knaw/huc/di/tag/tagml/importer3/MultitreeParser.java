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
import java.util.*;

/*
 * TAGML Multitree Parser
 *
 * Input: TAGMLLexer TokenStream
 * Imports based on detecting one or more tree structures
 * One node can appear in multiple trees
 *
 * @author: Ronald Haentjens Dekker
 * 13-05-2018
 */

public class MultitreeParser {

    private ExpectationTreeNode expectations;

    public MultitreeParser(ExpectationTreeNode expectations) {
        this.expectations = expectations;
    }

    public List<Tree<MarkupNode>> importTAGML(final String input) throws TAGMLSyntaxError, ExpectationError {
        CharStream antlrInputStream = CharStreams.fromString(input);
        return importTAGML(antlrInputStream);
    }

    public List<Tree<MarkupNode>> importTAGML(InputStream input) throws TAGMLSyntaxError, ExpectationError {
        try {
            CharStream antlrInputStream = CharStreams.fromStream(input);
            return importTAGML(antlrInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }

    private List<Tree<MarkupNode>> importTAGML(CharStream antlrInputStream) throws TAGMLSyntaxError, ExpectationError {
        TAGMLLexer lexer = new TAGMLLexer(antlrInputStream);
        ErrorListener errorListener = new ErrorListener();
        lexer.addErrorListener(errorListener);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();
        List<Token> tokens = tokenStream.getTokens();

        // the parser is bottom up and starts to chain together subtrees when closing tags
        // We houden alle bomen bij als een list van stacks van open subtrees
        List<Stack<MarkupNode>> openNodes = new ArrayList<>();
        // create the first stack
        Stack<MarkupNode> openMarkupNodesStack = new Stack<>();
        openNodes.add(openMarkupNodesStack);


        // We houden alle gebouwde bomen bij..
        List<Tree<MarkupNode>> closedTrees = new ArrayList<>();



        for (Token t: tokens) {
            // a couple of tokens we ignore
            if (t.getType() == TAGMLLexer.DEFAULT_BeginOpenMarkup) {
                continue;
            } else if (t.getType() == TAGMLLexer.IMO_EndOpenMarkup) {
                continue;
            } else if (t.getType() == TAGMLLexer.DEFAULT_BeginCloseMarkup) {
                continue;
            } else if (t.getType() == TAGMLLexer.IMC_EndCloseMarkup) {
                continue;
            }


            // a couple of tokens we store the temporary result
            if (t.getType() == TAGMLLexer.DEFAULT_Text) {
                for (Stack<MarkupNode> openNodesStack : openNodes) {
                    // the first node is not always necessary.. it is optional for files without root node
                    if (openNodesStack.isEmpty()) {
                        MarkupNode rootNode = new MarkupNode();
                        openNodesStack.add(rootNode);
                        //NOTE: also add tree!
                        //Cause normally this done at close tag time!
                        Tree<MarkupNode> markupNodeTree = new Tree<>(rootNode);
                        closedTrees.add(markupNodeTree);
                        //TODO: oh boy this will trigger a node not closed exception!
                    }

                    openNodesStack.peek().setText(t.getText());
                }
            }
            else if (t.getType() == TAGMLLexer.IMO_NameOpenMarkup) {
                MarkupNode node = new MarkupNode();
                node.setTag(t.getText());
                //TODO: nu wordt het alleen aan één boom toegevoegd!
                openMarkupNodesStack.add(node);
            }
            else if (t.getType() == TAGMLLexer.IMC_NameCloseMarkup) {
                String tagToLookFor = t.getText();
                System.out.println(tagToLookFor);
                System.out.println(openMarkupNodesStack.peek().getTag());
                if (!openMarkupNodesStack.peek().getTag().equals(tagToLookFor)) {
                    // nu moeten we gaan zoeken
                    MarkupNode node = searchTheStackForTag(openMarkupNodesStack, tagToLookFor);
                    // committen is ook best lastig!
                    // Ik weet niet precies hoeveel trees we moeten maken
                    Tree<MarkupNode> newTree = new Tree<MarkupNode>();
                } else {
                    MarkupNode last = openMarkupNodesStack.pop();
                    Tree<MarkupNode> markupNodeTree = new Tree<>(last);
                    closedTrees.add(markupNodeTree);


//                if (!openTreeNodes.contains(tagToLookFor)) {
//                    throw new RuntimeException( "Heh!");
//                }
                }
            }
            else if (t.getType() == -1) {
                break;
            }
            else {
                throw new RuntimeException("Unsupported token exception: "+t.toString());
            }
        }

        return closedTrees;
    }

    private MarkupNode searchTheStackForTag(Stack<MarkupNode> openTreeNodesStack, String tagToLookFor) {
        MarkupNode found = null;
        for (int i=openTreeNodesStack.size()-1; i >= 0;i--) {
            MarkupNode markupNode = openTreeNodesStack.get(i);
            if (markupNode.getTag().equals(tagToLookFor)) {
                System.out.println("Found it!");
                found = markupNode;
                break;
                // throw new RuntimeException("Warning overlap!");
            }
        }
        if (found == null) {
            throw new RuntimeException("Weird not found!");
        }
        return found;
    }


}