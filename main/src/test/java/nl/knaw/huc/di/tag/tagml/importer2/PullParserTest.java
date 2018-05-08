package nl.knaw.huc.di.tag.tagml.importer2;

import nl.knaw.huc.di.tag.tagml.grammar.TAGMLLexer;
import org.junit.Test;

public class PullParserTest {

    // The first test passes without exception
    // should test the pull parser events, after they are thrown
    @Test
    public void testTAGML_Text() throws ExpectationError {
        ExpectationTreeNode expectations = new AndNode(new CharacterClassNode(), new TerminalNode());
        String tagML = "The rain in Spain falls mainly on the plain.";
        PullParser parser = new PullParser(expectations);
        parser.importTAGML(tagML);

    }

    /* this one fails. due to incorrect expectations..
     * we need to use an or node
     * to combine everything
     * the rule for an open tag is already complex
     */

    @Test
    public void testTAGML_MarkupNonOverlapping() throws ExpectationError {
        ExpectationTreeNode openMarker = new AndNode(new StrictTypeNode(TAGMLLexer.DEFAULT_BeginOpenMarkup), new StrictTypeNode(TAGMLLexer.IMO_NameOpenMarkup), new StrictTypeNode(TAGMLLexer.IMO_EndOpenMarkup), new CharacterClassNode(), new StrictTypeNode(TAGMLLexer.DEFAULT_BeginCloseMarkup), new StrictTypeNode(TAGMLLexer.IMC_NameCloseMarkup), new StrictTypeNode(TAGMLLexer.IMC_EndCloseMarkup), new TerminalNode());
        //ExpectationTreeNode expectations = new AndNode(new CharacterClassNode(), new TerminalNode());
        String tagML = "[line>The rain in Spain falls mainly on the plain.<line]";
        PullParser parser = new PullParser(openMarker);
        parser.importTAGML(tagML);
    }
}