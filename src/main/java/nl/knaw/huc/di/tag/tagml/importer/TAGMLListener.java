package nl.knaw.huc.di.tag.tagml.importer;

/*-
 * #%L
 * alexandria-markup
 * =======
 * Copyright (C) 2016 - 2018 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import nl.knaw.huc.di.tag.tagml.grammar.TAGMLLexer;
import nl.knaw.huc.di.tag.tagml.grammar.TAGMLParser;
import nl.knaw.huc.di.tag.tagml.grammar.TAGMLParserBaseListener;
import nl.knaw.huygens.alexandria.ErrorListener;
import nl.knaw.huygens.alexandria.storage.*;
import nl.knaw.huygens.alexandria.storage.wrappers.AnnotationWrapper;
import nl.knaw.huygens.alexandria.storage.wrappers.DocumentWrapper;
import nl.knaw.huygens.alexandria.storage.wrappers.MarkupWrapper;
import nl.knaw.huygens.alexandria.storage.wrappers.TextNodeWrapper;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static nl.knaw.huc.di.tag.tagml.grammar.TAGMLParser.*;

public class TAGMLListener extends TAGMLParserBaseListener {
  private static final Logger LOG = LoggerFactory.getLogger(TAGMLListener.class);

  private final TAGStore store;
  private final DocumentWrapper document;
  private final ErrorListener errorListener;
  private final Deque<MarkupWrapper> openMarkup = new ArrayDeque<>();
  private final Deque<MarkupWrapper> suspendedMarkup = new ArrayDeque<>();
  private final HashMap<String, MarkupWrapper> identifiedMarkups = new HashMap<>();
  private final HashMap<String, String> idsInUse = new HashMap<>();
  private final Map<String, String> namespaces = new HashMap<>();
  private boolean atDocumentStart = true;

  public TAGMLListener(final TAGStore store, ErrorListener errorListener) {
    this.store = store;
    this.document = store.createDocumentWrapper();
    this.errorListener = errorListener;
  }

  public DocumentWrapper getDocument() {
    return document;
  }

  @Override
  public void exitDocument(TAGMLParser.DocumentContext ctx) {
    update(document.getDocument());
    if (!openMarkup.isEmpty()) {
      String openRanges = openMarkup.stream()//
          .map(m -> "[" + m.getExtendedTag() + ">")//
          .collect(joining(", "));
      errorListener.addError(
          "Unclosed TAGML tag(s): %s",
          openRanges
      );
    }
    if (!suspendedMarkup.isEmpty()) {
      String suspendedMarkupString = suspendedMarkup.stream()//
          .map(this::suspendTag)//
          .collect(Collectors.joining(", "));
      errorListener.addError("Some suspended markup was not resumed: %s", suspendedMarkupString);
    }
  }

  private String suspendTag(MarkupWrapper markupWrapper) {
    return "<" + markupWrapper.getExtendedTag() + "]";
  }

  @Override
  public void exitNamespaceDefinition(NamespaceDefinitionContext ctx) {
    String ns = ctx.IN_NamespaceIdentifier().getText();
    String url = ctx.IN_NamespaceURI().getText();
    namespaces.put(ns, url);
  }

  @Override
  public void exitText(TextContext ctx) {
    String text = ctx.getText();
    LOG.info("text=<{}>", text);
    atDocumentStart = atDocumentStart && StringUtils.isBlank(text);
    if (!atDocumentStart) {
      TextNodeWrapper tn = store.createTextNodeWrapper(text);
      document.addTextNode(tn);
      openMarkup.forEach(m -> linkTextToMarkup(tn, m));
    }
  }

  @Override
  public void enterStartTag(StartTagContext ctx) {
    String markupName = ctx.markupName().name().getText();
    LOG.info("startTag.markupName=<{}>", markupName);
    if (markupName.contains(":")) {
      String namespace = markupName.split(":", 2)[0];
      if (!namespaces.containsKey(namespace)) {
        errorListener.addError(
            "%snamespace %s has not been defined.",
            errorPrefix(ctx), namespace
        );
      }
    }
    ctx.annotation()
        .forEach(annotation -> LOG.info("  startTag.annotation={{}}", annotation.getText()));

    TerminalNode prefix = ctx.markupName().IMO_Prefix();
    boolean optional = prefix != null && prefix.getText().equals("?");
    boolean resume = prefix != null && prefix.getText().equals("+");

    MarkupWrapper markup = resume
        ? resumeMarkup(ctx)
        : addMarkup(markupName, ctx.annotation(), ctx).setOptional(optional);

    if (markup != null) {
      TerminalNode suffix = ctx.markupName().IMO_Suffix();
      if (suffix != null) {
        String id = suffix.getText().replace("~", "");
        markup.setSuffix(id);
      }

      openMarkup.add(markup);
    }
  }

  private MarkupWrapper resumeMarkup(StartTagContext ctx) {
    String tag = ctx.markupName().getText().replace("+", "");
    MarkupWrapper markup = removeFromMarkupStack(tag, suspendedMarkup);
    if (markup == null) {
      errorListener.addError(
          "Resuming tag [+%s> found, which has no corresponding earlier suspending tag <-%s].",
          tag, tag
      );
    }
    return markup;
  }

  @Override
  public void exitEndTag(EndTagContext ctx) {
    removeFromOpenMarkup(ctx.markupName());
  }

  @Override
  public void exitMilestoneTag(MilestoneTagContext ctx) {
//    String markupName = ctx.name().getText();
//    LOG.info("milestone.markupName=<{}>", markupName);
//    ctx.annotation()
//        .forEach(annotation -> LOG.info("milestone.annotation={{}}", annotation.getText()));
    TextNodeWrapper tn = store.createTextNodeWrapper("");
    document.addTextNode(tn);
    openMarkup.forEach(m -> linkTextToMarkup(tn, m));
    MarkupWrapper markup = addMarkup(ctx.name().getText(), ctx.annotation(), ctx);
    linkTextToMarkup(tn, markup);
  }

  private class DocumentContext {
    private final TAGDocument document;
    private final Deque<TAGMarkup> openMarkupDeque = new ArrayDeque<>();
    private final Stack<TAGMarkup> openMarkupStack = new Stack<>();
    private final Stack<TAGAnnotation> annotationStack = new Stack<>();
    private final ListenerContext listenerContext;

    DocumentContext(TAGDocument document, ListenerContext listenerContext) {
      this.document = document;
      this.listenerContext = listenerContext;
    }

    void openMarkup(TAGMarkup markup) {
      openMarkupDeque.push(markup);
      openMarkupStack.push(markup);
      document.getMarkupIds().add(markup.getId());
    }

    void pushOpenMarkup(String rangeName) {
      // LOG.info("currentDocumentContext().openMarkupDeque={}", openMarkupDeque.stream().map(Markup::getTag).collect(Collectors.toList()));
      Optional<TAGMarkup> findFirst = openMarkupDeque.stream()//
          .filter(tr -> tr.getExtendedTag().equals(rangeName))//
          .findFirst();
      if (findFirst.isPresent()) {
        TAGMarkup markup = findFirst.get();
        if (markup.getTextNodeIds().isEmpty()) {
          // every markup should have at least one textNode
          TAGTextNode emptyTextNode = new TAGTextNode("");
          update(emptyTextNode);
          addTextNode(emptyTextNode);
          closeMarkup();
        }
        openMarkupStack.push(markup);
      } else {
        errorListener.addError(
            "%s Closing tag <%s] found without corresponding open tag.",
            errorPrefix(listenerContext.getCurrentToken()), rangeName
        );
      }
    }

    private String errorPrefix(Token currentToken) {
      return format("line %d:%d :", currentToken.getLine(), currentToken.getCharPositionInLine());
    }

    void popOpenMarkup() {
      openMarkupStack.pop();
    }

    void closeMarkup() {
      if (!openMarkupStack.isEmpty()) {
        TAGMarkup markup = openMarkupStack.pop();
        update(markup);
        openMarkupDeque.remove(markup);
      }
    }

    void addTextNode(TAGTextNode textNode) {
      openMarkupDeque.descendingIterator()//
          .forEachRemaining(m -> {
            m.addTextNode(textNode);
            document.associateTextWithMarkup(textNode, m);
          });
      document.addTextNode(textNode);
    }

    private TAGMarkup currentMarkup() {
      return openMarkupDeque.isEmpty() ? null : openMarkupStack.peek();
    }

    void openAnnotation(TAGAnnotation annotation) {
      if (annotationStack.isEmpty()) {
        TAGMarkup markup = currentMarkup();
        if (markup != null) {
          markup.addAnnotation(annotation);
        }
      } else {
        annotationStack.peek().addAnnotation(annotation);
      }
      annotationStack.push(annotation);
    }

    TAGDocument currentAnnotationDocument() {
      Long value = annotationStack.peek().getDocumentId();
      return store.getDocument(value);
    }

    void closeAnnotation() {
      TAGAnnotation annotation = annotationStack.pop();
      update(annotation);
    }
  }

  private class ListenerContext {
    private final Deque<DocumentContext> documentContextStack = new ArrayDeque<>();
    private final TAGMLLexer lexer;
    private final List<String> errors = new ArrayList<>();
    private String methodName;
    private Token currentToken;

    ListenerContext(TAGMLLexer lexer) {
      this.lexer = lexer;
    }

    Token nextToken() {
      currentToken = lexer.nextToken();
      return currentToken;
    }

    Token getCurrentToken() {
      return currentToken;
    }

    String getModeName() {
      return lexer.getModeNames()[lexer._mode];
    }

    String getRuleName() {
      int type = currentToken.getType();
      return type == -1 ? "EOF" : lexer.getRuleNames()[type - 1];
    }

    void pushDocumentContext(TAGDocument document) {
      documentContextStack.push(new DocumentContext(document, this));
    }

    DocumentContext currentDocumentContext() {
      return documentContextStack.peek();
    }

    DocumentContext popDocumentContext() {
      DocumentContext documentContext = documentContextStack.pop();
      update(documentContext.document);
      if (!documentContext.openMarkupDeque.isEmpty()) {
        String openRanges = documentContext.openMarkupDeque.stream()//
            .map(m -> "[" + m.getExtendedTag() + ">")//
            .collect(joining(", "));
        errorListener.addError("Unclosed TAGML tag(s): %s", openRanges);
      }
      return documentContext;
    }

    TAGMarkup newMarkup(String tagName) {
      TAGMarkup tagMarkup = new TAGMarkup(currentDocumentContext().document.getId(), tagName);
      update(tagMarkup);
      return tagMarkup;
    }

    void openMarkup(TAGMarkup markup) {
      currentDocumentContext().openMarkup(markup);
    }

    void pushOpenMarkup(String rangeName) {
      currentDocumentContext().pushOpenMarkup(rangeName);
    }

    void popOpenMarkup() {
      currentDocumentContext().popOpenMarkup();
    }

    void closeMarkup() {
      currentDocumentContext().closeMarkup();
    }

    void addTextNode(TAGTextNode textNode) {
      currentDocumentContext().addTextNode(textNode);
    }

    void openAnnotation(TAGAnnotation annotation) {
      currentDocumentContext().openAnnotation(annotation);
    }

    TAGDocument currentAnnotationDocument() {
      return currentDocumentContext().currentAnnotationDocument();
    }

    void closeAnnotation() {
      currentDocumentContext().closeAnnotation();
    }

    List<String> getErrors() {
      return errors;
    }

    boolean hasErrors() {
      return !errors.isEmpty();
    }

    String getMethodName() {
      return methodName;
    }

    void setMethodName(String methodName) {
      this.methodName = methodName;
    }
  }

  private MarkupWrapper addMarkup(String extendedTag, List<AnnotationContext> atts, ParserRuleContext ctx) {
    MarkupWrapper markup = store.createMarkupWrapper(document, extendedTag);
    addAnnotations(atts, markup);
    document.addMarkup(markup);
    if (markup.hasMarkupId()) {
      identifiedMarkups.put(extendedTag, markup);
      String id = markup.getMarkupId();
      if (idsInUse.containsKey(id)) {
        errorListener.addError(
            "%sid '%s' was already used in markup [%s>.",
            errorPrefix(ctx), id, idsInUse.get(id));
      }
      idsInUse.put(id, extendedTag);
    }
    return markup;
  }

  private void addAnnotations(List<AnnotationContext> annotationContexts, MarkupWrapper markup) {
    annotationContexts.forEach(actx -> {
      if (actx instanceof BasicAnnotationContext) {
        BasicAnnotationContext basicAnnotationContext = (BasicAnnotationContext) actx;
        String aName = basicAnnotationContext.annotationName().getText();
        String quotedAttrValue = basicAnnotationContext.annotationValue().getText();
        // TODO: handle recursion, value types
//      String attrValue = quotedAttrValue.substring(1, quotedAttrValue.length() - 1); // remove single||double quotes
        AnnotationWrapper annotation = store.createAnnotationWrapper(aName, quotedAttrValue);
        markup.addAnnotation(annotation);

      } else if (actx instanceof IdentifyingAnnotationContext) {
        IdentifyingAnnotationContext idAnnotationContext = (IdentifyingAnnotationContext) actx;
        String id = idAnnotationContext.idValue().getText();
        markup.setMarkupId(id);

      } else if (actx instanceof RefAnnotationContext) {
        RefAnnotationContext refAnnotationContext = (RefAnnotationContext) actx;
        String aName = refAnnotationContext.annotationName().getText();
        String refId = refAnnotationContext.refValue().getText();
        // TODO add ref to model
        AnnotationWrapper annotation = store.createAnnotationWrapper(aName, refId);
        markup.addAnnotation(annotation);
      }
    });
  }

  private void linkTextToMarkup(TextNodeWrapper tn, MarkupWrapper markup) {
    document.associateTextNodeWithMarkup(tn, markup);
    markup.addTextNode(tn);
  }

  private Long update(TAGObject tagObject) {
    return store.persist(tagObject);
  }

  private MarkupWrapper removeFromOpenMarkup(MarkupNameContext ctx) {
    String extendedMarkupName = ctx.name().getText();

    TerminalNode suffix = ctx.IMC_Suffix();
    if (suffix != null) {
      extendedMarkupName += suffix.getText();
    }

    MarkupWrapper markup = removeFromMarkupStack(extendedMarkupName, openMarkup);
    if (markup == null) {
      errorListener.addError(
          "%sClosing tag <%s] found without corresponding open tag.",
          errorPrefix(ctx), extendedMarkupName
      );
    } else {

      TerminalNode prefixNode = ctx.IMC_Prefix();
      if (prefixNode != null) {
        String prefixNodeText = prefixNode.getText();
        if (prefixNodeText.equals("?")) {
          // optional
          // TODO

        } else if (prefixNodeText.equals("-")) {
          // suspend
          suspendedMarkup.add(markup);
        }
      }
    }

    return markup;
  }

  private MarkupWrapper removeFromMarkupStack(String extendedTag, Deque<MarkupWrapper> markupStack) {
    Iterator<MarkupWrapper> descendingIterator = markupStack.descendingIterator();
    MarkupWrapper markup = null;
    while (descendingIterator.hasNext()) {
      markup = descendingIterator.next();
      if (markup.getExtendedTag().equals(extendedTag)) {
        break;
      }
      markup = null;
    }
    if (markup != null) {
      markupStack.remove(markup);
    }
    return markup;
  }

  private String errorPrefix(ParserRuleContext ctx) {
    Token startToken = ctx.start;
    return format("line %d:%d : ", startToken.getLine(), startToken.getCharPositionInLine());
  }

}
