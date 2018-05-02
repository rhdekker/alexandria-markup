package nl.knaw.huc.di.tag.tagml.importer2;

import org.antlr.v4.runtime.Token;

/*
 * Or node
 *
 * @author: Ronald Haentjens Dekker
 *
 */
public class OrNode extends ExpectationTreeNode {
    // just like the AndNode we do not a need a type here!
    OrNode(int type) {
        super(type);
    }

    // maybe evaluate token is a better name?
    public ExpectationTreeNode evaluateType(Token token) {
        if (size()==0) {
            throw new UnsupportedOperationException("No children!");
        }
        // this thing has children..
        // if one of the children has the correct type we return yes
        // or rather than yes... we return the node
        // there could of course be multiple so we go from the left to the right
        for (ExpectationTreeNode node : children) {
            if (node.getType() == token.getType()) {
                return node;
            }
        }
        return null;
    }
}
