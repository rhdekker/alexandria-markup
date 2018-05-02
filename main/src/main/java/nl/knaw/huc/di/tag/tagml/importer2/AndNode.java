package nl.knaw.huc.di.tag.tagml.importer2;

/*
 * author: Ronald Haentjens Dekker
 * date: 02-05-2018
 */
public class AndNode extends ExpectationTreeNode {
    AndNode() {
        //TODO: introduce leaf nodes
        super(1234567);
    }

    public AndNode(CharacterClassNode characterClassNode, TerminalNode terminalNode) {
        super(1234567);
        this.children.add(characterClassNode);
        this.children.add(terminalNode);
    }

    // delegate the get type to the first child!
    @Override
    public int getType() {
        return getFirstChildNode().getType();
    }

    private ExpectationTreeNode getFirstChildNode() {
        if (size() == 0) {
            throw new RuntimeException("Children of an AndNode should not be empty!");
        }
        return children.get(0);
    }

    public ExpectationTreeNode getRightNode() {
        if (size() != 2) {
            throw new RuntimeException("We cannot yet handle more than two children!");
        }
        return children.get(1);
    }
}
