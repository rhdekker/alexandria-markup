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
public class Tree<T> {
//    private final Tree parent;
    private T content;
    private final List<Tree> children;

    Tree() {
//        this.parent = null;
        this.children = new ArrayList<>();
    }

    Tree(T content) {
//        this.parent = null;
        this.children = new ArrayList<>();
        this.content = content;
    }

    int size() {
        return children.size();
    }

    public T getContent() {
        return content;
    }
}
