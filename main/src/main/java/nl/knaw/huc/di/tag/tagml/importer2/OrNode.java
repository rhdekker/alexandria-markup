package nl.knaw.huc.di.tag.tagml.importer2;

import org.antlr.v4.runtime.Token;

/*
 * Or node
 *
 * @author: Ronald Haentjens Dekker
 *
 * Restriction:
 * The children of this node have to be of type stricttypenode.
 *
 */
public class OrNode extends ExpectationTreeNode {

    @Override
    public ExpectationTreeNode evaluateToken(Token token) throws ExpectationError {
        if (size()==0) {
            throw new UnsupportedOperationException("No children!");
        }
        // this thing has children..
        // if one of the children has the correct type we return yes
        // or rather than yes... we return the node
        // there could of course be multiple so we go from the left to the right
        for (ExpectationTreeNode node : children) {
            // guard against child nodes that are not of StrictTypeNode class
            if (!(node instanceof StrictTypeNode)) {
                throw new RuntimeException("Child node of Or node not of type StrictTypeNode, but of type: "+node.getClass());
            }
            StrictTypeNode stn = (StrictTypeNode) node;
            if (stn.getType()==token.getType()) {
                return node;
            }
        }
        return null;
    }
}
