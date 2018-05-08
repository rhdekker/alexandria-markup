package nl.knaw.huc.di.tag.tagml.importer2;

import nl.knaw.huc.di.tag.tagml.grammar.TAGMLLexer;

/*
 * TAGML Pull Parser
 *
 * @author: Ronald Haentjens Dekker
 * @date: 08-05-2018
 *
 * Specific instance of the pull parser
 * with TAGML expectations and rewrite rules.
 *
 */
public class TAGMLPullParser extends  PullParser {

    TAGMLPullParser() {
        super(new AndNode(new StrictTypeNode(TAGMLLexer.DEFAULT_BeginOpenMarkup), new StrictTypeNode(TAGMLLexer.IMO_NameOpenMarkup), new StrictTypeNode(TAGMLLexer.IMO_EndOpenMarkup), new CharacterClassNode(), new StrictTypeNode(TAGMLLexer.DEFAULT_BeginCloseMarkup), new StrictTypeNode(TAGMLLexer.IMC_NameCloseMarkup), new StrictTypeNode(TAGMLLexer.IMC_EndCloseMarkup), new TerminalNode()));
    }

}
