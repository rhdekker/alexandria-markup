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

    @Test
    public void testTAGML_MarkupNonOverlapping() throws ExpectationError {
        ExpectationTreeNode openMarker = new AndNode(new StrictTypeNode(TAGMLLexer.DEFAULT_BeginOpenMarkup), new StrictTypeNode(TAGMLLexer.IMO_NameOpenMarkup), new StrictTypeNode(TAGMLLexer.IMO_EndOpenMarkup), new CharacterClassNode(), new StrictTypeNode(TAGMLLexer.DEFAULT_BeginCloseMarkup), new StrictTypeNode(TAGMLLexer.IMC_NameCloseMarkup), new StrictTypeNode(TAGMLLexer.IMC_EndCloseMarkup), new TerminalNode());
        //ExpectationTreeNode expectations = new AndNode(new CharacterClassNode(), new TerminalNode());
        String tagML = "[line>The rain in Spain falls mainly on the plain.<line]";
        PullParser parser = new PullParser(openMarker);
        parser.importTAGML(tagML);
    }

    // Now we need to add a test for overlap.
    @Test
    public void testOverlap() throws ExpectationError {
        String tagML = "[a>J'onn J'onzz [b>likes<a] Oreos<b]";
        TAGMLPullParser parser = new TAGMLPullParser();
        parser.importTAGML(tagML);
    }


        //        store.runInTransaction(() -> {
//            DocumentWrapper document = parseTAGML(tagML);
//            assertThat(document).hasMarkupWithTag("a").withTextNodesWithText("J'onn J'onzz ", "likes");
//            assertThat(document).hasMarkupWithTag("b").withTextNodesWithText("likes", " Oreos");
//        });
//    }



}