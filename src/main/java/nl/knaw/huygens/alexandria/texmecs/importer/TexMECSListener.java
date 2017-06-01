package nl.knaw.huygens.alexandria.texmecs.importer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.huygens.alexandria.lmnl.data_model.Annotation;
import nl.knaw.huygens.alexandria.lmnl.data_model.Document;
import nl.knaw.huygens.alexandria.lmnl.data_model.Limen;
import nl.knaw.huygens.alexandria.lmnl.data_model.TextNode;
import nl.knaw.huygens.alexandria.lmnl.data_model.Markup;
import nl.knaw.huygens.alexandria.lmnl.grammar.TexMECSParser.AttsContext;
import nl.knaw.huygens.alexandria.lmnl.grammar.TexMECSParser.EidContext;
import nl.knaw.huygens.alexandria.lmnl.grammar.TexMECSParser.EndTagContext;
import nl.knaw.huygens.alexandria.lmnl.grammar.TexMECSParser.EndTagSetContext;
import nl.knaw.huygens.alexandria.lmnl.grammar.TexMECSParser.GiContext;
import nl.knaw.huygens.alexandria.lmnl.grammar.TexMECSParser.ResumeTagContext;
import nl.knaw.huygens.alexandria.lmnl.grammar.TexMECSParser.SoleTagContext;
import nl.knaw.huygens.alexandria.lmnl.grammar.TexMECSParser.StartTagContext;
import nl.knaw.huygens.alexandria.lmnl.grammar.TexMECSParser.StartTagSetContext;
import nl.knaw.huygens.alexandria.lmnl.grammar.TexMECSParser.SuspendTagContext;
import nl.knaw.huygens.alexandria.lmnl.grammar.TexMECSParser.TextContext;
import nl.knaw.huygens.alexandria.lmnl.grammar.TexMECSParser.VirtualElementContext;
import nl.knaw.huygens.alexandria.lmnl.grammar.TexMECSParserBaseListener;

public class TexMECSListener extends TexMECSParserBaseListener {

  Logger LOG = LoggerFactory.getLogger(getClass());

  private Document document = new Document();
  private Limen limen = document.value();
  private Deque<Markup> openMarkup = new ArrayDeque<>();
  private Deque<Markup> suspendedMarkup = new ArrayDeque<>();
  private boolean insideTagSet = false; // TODO: use this?
  private HashMap<String, Markup> identifiedMarkups = new HashMap<>();

  public TexMECSListener() {
  }

  public Document getDocument() {
    return document;
  }

  @Override
  public void exitStartTagSet(StartTagSetContext ctx) {
    Markup markup = addMarkup(ctx.eid(), ctx.atts());
    openMarkup.add(markup);
    insideTagSet = true;
  }

  @Override
  public void exitStartTag(StartTagContext ctx) {
    Markup markup = addMarkup(ctx.eid(), ctx.atts());
    openMarkup.add(markup);
  }

  @Override
  public void exitText(TextContext ctx) {
    TextNode tn = new TextNode(ctx.getText());
    limen.addTextNode(tn);
    openMarkup.forEach(m -> linkTextToMarkup(tn, m));
  }

  @Override
  public void exitEndTag(EndTagContext ctx) {
    removeFromOpenMarkup(ctx.gi());
  }

  @Override
  public void exitEndTagSet(EndTagSetContext ctx) {
    insideTagSet = false;
    removeFromOpenMarkup(ctx.gi());
  }

  @Override
  public void exitSoleTag(SoleTagContext ctx) {
    TextNode tn = new TextNode("");
    limen.addTextNode(tn);

    openMarkup.forEach(m -> linkTextToMarkup(tn, m));
    Markup markup = addMarkup(ctx.eid(), ctx.atts());
    linkTextToMarkup(tn, markup);
  }

  private void linkTextToMarkup(TextNode tn, Markup markup) {
    limen.associateTextWithRange(tn, markup);
    markup.addTextNode(tn);
  }

  @Override
  public void exitSuspendTag(SuspendTagContext ctx) {
    Markup markup = removeFromOpenMarkup(ctx.gi());
    suspendedMarkup.add(markup);
  }

  @Override
  public void exitResumeTag(ResumeTagContext ctx) {
    Markup markup = removeFromSuspendedMarkup(ctx);
    openMarkup.add(markup);
  }

  @Override
  public void exitVirtualElement(VirtualElementContext ctx) {
    String extendedTag = ctx.eid().gi().getText() + "=" + ctx.idref().getText();
    if (identifiedMarkups.containsKey(extendedTag)) {
      Markup ref = identifiedMarkups.get(extendedTag);
      Markup markup = addMarkup(ref.getTag(), ctx.atts());
      ref.textNodes.forEach(tn -> {
        TextNode copy = new TextNode(tn.getContent());
        limen.addTextNode(copy);
        openMarkup.forEach(m -> linkTextToMarkup(copy, m));
        linkTextToMarkup(copy, markup);
      });
    }
  }

  private Markup addMarkup(EidContext eid, AttsContext atts) {
    String extendedTag = eid.getText();
    return addMarkup(extendedTag, atts);
  }

  private Markup addMarkup(String extendedTag, AttsContext atts) {
    Markup markup = new Markup(limen, extendedTag);
    addAttributes(atts, markup);
    limen.addMarkup(markup);
    if (markup.hasId()) {
      identifiedMarkups.put(extendedTag, markup);
    }
    return markup;
  }

  private void addAttributes(AttsContext attsContext, Markup markup) {
    attsContext.avs().forEach(avs -> {
      String attrName = avs.NAME_O().getText();
      String quotedAttrValue = avs.STRING().getText();
      String attrValue = quotedAttrValue.substring(1, quotedAttrValue.length() - 1); // remove single||double quotes
      Annotation annotation = new Annotation(attrName, attrValue);
      markup.addAnnotation(annotation);
    });
  }

  private Markup removeFromOpenMarkup(GiContext gi) {
    String tag = gi.getText();
    Markup markup = removeFromMarkupStack(tag, openMarkup);
    return markup;
  }

  private Markup removeFromSuspendedMarkup(ResumeTagContext ctx) {
    String tag = ctx.gi().getText();
    Markup markup = removeFromMarkupStack(tag, suspendedMarkup);
    return markup;
  }

  private Markup removeFromMarkupStack(String extendedTag, Deque<Markup> markupStack) {
    Iterator<Markup> descendingIterator = markupStack.descendingIterator();
    Markup markup = null;
    while (descendingIterator.hasNext()) {
      markup = descendingIterator.next();
      if (markup.getExtendedTag().equals(extendedTag)) {
        break;
      }
    }
    markupStack.remove(markup);
    return markup;
  }

}