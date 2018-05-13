package nl.knaw.huc.di.tag.tagml.importer3;

import nl.knaw.huc.di.tag.tagml.importer2.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class MultitreeParserTest {

    // The first test passes without exception
    // should test the pull parser events, after they are thrown
    @Test
    public void testTAGML_Text() throws ExpectationError {
        String tagML = "The rain in Spain falls mainly on the plain.";
        MultitreeParser parser = new MultitreeParser(null);
        List<Tree<MarkupNode>> multitree = parser.importTAGML(tagML);
        // 1 expect one tree
        assertEquals(1, multitree.size());
        Tree<MarkupNode> node1 = multitree.get(0);
        assertEquals("The rain in Spain falls mainly on the plain.", node1.getContent().getText());

    }

    @Test
    public void testTAGML_MarkupNonOverlapping() throws ExpectationError {
        String tagML = "[line>The rain in Spain falls mainly on the plain.<line]";
        MultitreeParser parser = new MultitreeParser(null);
        List<Tree<MarkupNode>> multitree = parser.importTAGML(tagML);
        // 1 expect one tree
        assertEquals(1, multitree.size());
        Tree<MarkupNode> node1 = multitree.get(0);
        assertEquals("line", node1.getContent().getTag());
        assertEquals("The rain in Spain falls mainly on the plain.", node1.getContent().getText());
    }



    // Now we need to add a test for overlap.
    @Test
    public void testOverlap() throws ExpectationError {
        String tagML = "[a>J'onn J'onzz [b>likes<a] Oreos<b]";
        MultitreeParser parser = new MultitreeParser(null);
        List<Tree<MarkupNode>> multitree = parser.importTAGML(tagML);
        // 2 expect one tree
        assertEquals(2, multitree.size());
        Tree node1 = multitree.get(0);
    }


        //        store.runInTransaction(() -> {
//            DocumentWrapper document = parseTAGML(tagML);
//            assertThat(document).hasMarkupWithTag("a").withTextNodesWithText("J'onn J'onzz ", "likes");
//            assertThat(document).hasMarkupWithTag("b").withTextNodesWithText("likes", " Oreos");
//        });
//    }



}