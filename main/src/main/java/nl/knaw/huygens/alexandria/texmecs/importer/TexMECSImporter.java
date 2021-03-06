package nl.knaw.huygens.alexandria.texmecs.importer;

/*
 * #%L
 * alexandria-markup
 * =======
 * Copyright (C) 2016 - 2018 HuC DI (KNAW)
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

import nl.knaw.huygens.alexandria.ErrorListener;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.storage.wrappers.DocumentWrapper;
import nl.knaw.huygens.alexandria.storage.wrappers.MarkupWrapper;
import nl.knaw.huygens.alexandria.texmecs.grammar.TexMECSLexer;
import nl.knaw.huygens.alexandria.texmecs.grammar.TexMECSParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

public class TexMECSImporter {
  private final Logger LOG = LoggerFactory.getLogger(getClass());
  private final TAGStore store;

  public TexMECSImporter(TAGStore store) {
    this.store = store;
  }

  public DocumentWrapper importTexMECS(String input) throws TexMECSSyntaxError {
    CharStream antlrInputStream = CharStreams.fromString(input);
    return importTexMECS(antlrInputStream);
  }

  public DocumentWrapper importTexMECS(InputStream input) throws TexMECSSyntaxError {
    try {
      CharStream antlrInputStream = CharStreams.fromStream(input);
      return importTexMECS(antlrInputStream);
    } catch (IOException e) {
      e.printStackTrace();
      throw new UncheckedIOException(e);
    }
  }

  private DocumentWrapper importTexMECS(CharStream antlrInputStream) {
    TexMECSLexer lexer = new TexMECSLexer(antlrInputStream);
    ErrorListener errorListener = new ErrorListener();
    lexer.addErrorListener(errorListener);
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    TexMECSParser parser = new TexMECSParser(tokens);
    parser.addErrorListener(errorListener);
    parser.setBuildParseTree(true);
    ParseTree parseTree = parser.document();
    int numberOfSyntaxErrors = parser.getNumberOfSyntaxErrors();
    LOG.info("parsed with {} syntax errors", numberOfSyntaxErrors);
    ParseTreeWalker parseTreeWalker = new ParseTreeWalker();
    TexMECSListener listener = new TexMECSListener(store);
    parseTreeWalker.walk(listener, parseTree);
    DocumentWrapper document = listener.getDocument();
    handleMarkupDominance(document);

    String errorMsg = "";
    if (listener.hasErrors()) {
      String errors = listener.getErrors().stream().collect(Collectors.joining("\n"));
      errorMsg = "Parsing errors:\n" + errors;
    }
    if (numberOfSyntaxErrors > 0) {
      String errors = errorListener.getErrors().stream().collect(Collectors.joining("\n"));
      errorMsg += "\n\nTokenizing errors:\n" + errors;
    }
    if (!errorMsg.isEmpty()) {
      throw new TexMECSSyntaxError(errorMsg);
    }
    return document;
  }

  private void handleMarkupDominance(DocumentWrapper document) {
    List<MarkupWrapper> markupList = document.getMarkupStream().collect(Collectors.toList());
    for (int i = 0; i < markupList.size() - 1; i++) {
      MarkupWrapper first = markupList.get(i);
      MarkupWrapper second = markupList.get(i + 1);
      if (first.getMarkup().getTextNodeIds().equals(second.getMarkup().getTextNodeIds())) {
        // LOG.info("dominance found: {} dominates {}", first.getExtendedTag(), second.getExtendedTag());
        first.setDominatedMarkup(second);
      }
    }
  }

}
