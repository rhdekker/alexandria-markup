package nl.knaw.huygens.alexandria.creole;

/*
 * #%L
 * alexandria-markup
 * =======
 * Copyright (C) 2016 - 2017 Huygens ING (KNAW)
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

import static java.util.Arrays.asList;
import static nl.knaw.huygens.alexandria.AlexandriaAssertions.assertThat;
import static nl.knaw.huygens.alexandria.creole.Basics.qName;
import static nl.knaw.huygens.alexandria.creole.NameClasses.name;
import static nl.knaw.huygens.alexandria.creole.Constructors.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DerivativesTest {
  private final Logger LOG = LoggerFactory.getLogger(getClass());

  @Test
  public void testEventsDerivation() {
    // [text}tekst{text]
    Basics.QName qName = qName("text");
    Event startE = Events.startTagEvent(qName);
    Basics.Context context = Basics.context();
    Event textE = Events.textEvent("tekst", context);
    Event endE = Events.endTagEvent(qName);
    List<Event> events = new ArrayList<>();
    events.addAll(asList(startE, textE, endE));

    Pattern schemaPattern = element(//
        "text",//
        text()//
    );

    ValidationErrorListener errorListener = new ValidationErrorListener();
    Derivatives derivatives = new Derivatives(errorListener);
    Pattern pattern = derivatives.eventsDeriv(schemaPattern, events);
    LOG.info("derived pattern={}", pattern);
    assertThat(pattern).isEqualTo(empty());

    Pattern pattern1 = Derivatives.eventsDeriv(schemaPattern, endE);
    LOG.info("derived pattern={}", pattern1);
    assertThat(pattern1).isEqualTo(notAllowed());
  }

  @Test
  public void testBiblicalExample() {
    Pattern book = createSchema();
    List<Event> events = createEvents();

    ValidationErrorListener errorListener = new ValidationErrorListener();
    Derivatives derivatives = new Derivatives(errorListener);
    Pattern pattern = derivatives.eventsDeriv(book, events);
    assertThat(pattern).isEqualTo(empty());
    assertThat(pattern).isNullable();
  }

  private List<Event> createEvents() {
    //<book|<page no="1"|
    //  <title|Genesis|title>
    //  ...
    //  <section|
    //    <heading|The flood and the tower of Babel|heading>
    //    ...
    //    <chapter no="7"|
    //      ...
    //      <para|...<s|<verse no="23"|God wiped out every living thing
    //    that existed on earth, <index~1 ref="i0037"|man and
    //        <index~2 ref="i0038"|beast|index~1>, reptile|page>
    //      <page no="74"|and bird|index~2>; they were all wiped out
    //    over the whole earth, and only Noah and his company in the
    //    ark survived.|s>|verse>|para>
    //      <para|<verse no="24"|<s|When the waters had increased over
    //    the earth for a hundred and fifty days, |verse>|chapter>
    //      <chapter no="8"|<verse no="1"|God thought of Noah and all
    //    the wild animals and the cattle with him in the ark, and
    //    he made a wind pass over the earth, and the waters began to
    //    subside.|verse>|s>...|para>
    //      ...
    //    |chapter>
    //    ...
    //  |section>
    //  ...
    //|page>|book>
    Event openBook = Events.startTagEvent(qName("book"));
    Event closeBook = Events.endTagEvent(qName("book"));
    Event openPage = Events.startTagEvent(qName("page"));
    Event closePage = Events.endTagEvent(qName("page"));
    Event openTitle = Events.startTagEvent(qName("title"));
    Event closeTitle = Events.endTagEvent(qName("title"));
    Event titleText = Events.textEvent("Genesis");
//    Event ellipsisText = Events.textEvent("...");
    Event openHeading = Events.startTagEvent(qName("heading"));
    Event closeHeading = Events.endTagEvent(qName("heading"));
    Event openSection = Events.startTagEvent(qName("section"));
    Event closeSection = Events.endTagEvent(qName("section"));
    Event openChapter = Events.startTagEvent(qName("chapter"));
    Event closeChapter = Events.endTagEvent(qName("chapter"));
    Event openPara = Events.startTagEvent(qName("para"));
    Event closePara = Events.endTagEvent(qName("para"));
    Event openS = Events.startTagEvent(qName("s"));
    Event closeS = Events.endTagEvent(qName("s"));
    Event openVerse = Events.startTagEvent(qName("verse"));
    Event closeVerse = Events.endTagEvent(qName("verse"));
    Event openIndex1 = Events.startTagEvent(qName("index"), "1");
    Event closeIndex1 = Events.endTagEvent(qName("index"), "1");
    Event openIndex2 = Events.startTagEvent(qName("index"), "2");
    Event closeIndex2 = Events.endTagEvent(qName("index"), "2");
    Event headingText = Events.textEvent("The flood and the tower of Babel");
    Event someText = Events.textEvent("some text");

    List<Event> events = new ArrayList<>();
    events.addAll(asList(//
        openBook, openPage,//
        openTitle, titleText, closeTitle,//
        openSection,//
        openHeading, headingText, closeHeading,//
        openChapter,//
        openPara, openS, openVerse, someText,//
        openIndex1, someText,//
        openIndex2, someText, closeIndex1, someText, closePage,//
        openPage, someText, closeIndex2, someText,//
        closeS, closeVerse, closePara,//
        openPara, openVerse, openS, someText,//
        closeVerse, closeChapter,//
        openChapter, openVerse, someText,//
        closeVerse, closeS, closePara,//
        closeChapter,//
        closeSection,//
        closePage, closeBook//
//        , closeBook // <- huh?
    ));
    return events;
  }

  private Pattern createSchema() {
    Pattern page = range(name("page"), text());
    Pattern title = element("title", text());
    Pattern verse = range(name("verse"), text());
    Pattern chapter = range(name("chapter"), oneOrMore(verse));
    Pattern index = range(name("index"), text());
    Pattern indexedText = concurOneOrMore(mixed(zeroOrMore(index)));
    Pattern heading = element("heading", indexedText);
    Pattern s = range(name("s"), indexedText);
    Pattern para = range(name("para"),//
        concur(//
            oneOrMore(verse),//
            oneOrMore(s)
        )//
    );
    Pattern section = range(name("section"),//
        group(//
            heading,//
            oneOrMore(para)//
        )//
    );
    return element("book",//
        concur(//
            oneOrMore(page),//
            group(//
                title,//
                concur(//
                    oneOrMore(chapter),//
                    oneOrMore(section)//
                )//
            )//
        )//
    );
  }

}
