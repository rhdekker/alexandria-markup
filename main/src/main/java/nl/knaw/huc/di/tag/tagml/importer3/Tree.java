package nl.knaw.huc.di.tag.tagml.importer3;

import java.util.ArrayList;
import java.util.List;

/*
 * Class Tree
 *
 * @author: Ronald Haentjens Dekker
 * date: 13-05-2018
 *
 * We express the parse results as a tree
 */
public class Tree {
//    private final Tree parent;
    final List<Tree> children;
    String text="";

    Tree() {
//        this.parent = null;
        this.children = new ArrayList<>();
    }

    int size() {
        return children.size();
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
