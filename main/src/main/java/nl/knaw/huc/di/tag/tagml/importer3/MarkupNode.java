package nl.knaw.huc.di.tag.tagml.importer3;

/*
 * @author: Ronald Haentjens Dekker
 * date: 13-05-2018
 *
 * This class contains the content of a node
 * The content can be used in multiple trees
 * There are no separate text nodes; this might be a problem in case of overlap; need array
 * and mixed content. Maybe need to add a leaf text for cases for there follows text after a node
 * Maybe split in MarkupNode and textNode
 *
 */
public class MarkupNode {
    private String text="";
    private String tag="";

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
