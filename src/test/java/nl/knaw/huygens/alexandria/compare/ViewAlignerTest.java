package nl.knaw.huygens.alexandria.compare;

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

import nl.knaw.huygens.alexandria.AlexandriaBaseStoreTest;
import nl.knaw.huygens.alexandria.AlexandriaSoftAssertions;
import nl.knaw.huygens.alexandria.lmnl.importer.LMNLImporter;
import nl.knaw.huygens.alexandria.storage.wrappers.DocumentWrapper;
import nl.knaw.huygens.alexandria.view.TAGView;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static nl.knaw.huygens.alexandria.AlexandriaAssertions.assertThat;
import static nl.knaw.huygens.alexandria.compare.Segment.Type.*;
import static nl.knaw.huygens.alexandria.compare.SegmentMatcher.sM;
import static nl.knaw.huygens.alexandria.compare.TAGTokenMatcher.*;

public class ViewAlignerTest extends AlexandriaBaseStoreTest {

  private static final Logger LOG = LoggerFactory.getLogger(ViewAlignerTest.class);

  @Test
  public void testSegmentReplaced() {
    String sourceA = "[TEI}[s}a{s]{TEI]";
    String sourceB = "[TEI}[s}c{s]{TEI]";

    List<Segment> segments = calculateSegments(sourceA, sourceB);

    AlexandriaSoftAssertions softly = new AlexandriaSoftAssertions();
    SegmentMatcher expected0 = sM(aligned).tokensA(markupOpen("s")).tokensB(markupOpen("s"));
    softly.assertThat(segments.get(0)).matches(expected0);

    SegmentMatcher expected1 = sM(replacement).tokensA(text("a")).tokensB(text("c"));
    softly.assertThat(segments.get(1)).matches(expected1);

    SegmentMatcher expected2 = sM(aligned).tokensA(markupClose("s")).tokensB(markupClose("s"));
    softly.assertThat(segments.get(2)).matches(expected2);
  }

  @Test
  public void testAddition() {
    String sourceA = "[TEI}[s}a text{s]{TEI]";
    String sourceB = "[TEI}[s}a different text{s]{TEI]";

    List<Segment> segments = calculateSegments(sourceA, sourceB);

    AlexandriaSoftAssertions softly = new AlexandriaSoftAssertions();
    SegmentMatcher expected0 = sM(aligned)//
        .tokensA(markupOpen("s"), text("a "))//
        .tokensB(markupOpen("s"), text("a "));
    softly.assertThat(segments.get(0)).matches(expected0);

    SegmentMatcher expected1 = sM(addition)//
        .tokensA()//
        .tokensB(text("different "));
    softly.assertThat(segments.get(1)).matches(expected1);

    SegmentMatcher expected2 = sM(aligned)//
        .tokensA(text("text"), markupClose("s"))//
        .tokensB(text("text"), markupClose("s"));
    softly.assertThat(segments.get(2)).matches(expected2);

    softly.assertAll();
  }

  @Test
  public void testOmission() {
    String sourceA = "[TEI}[s}a text{s]{TEI]";
    String sourceB = "[TEI}[s}text{s]{TEI]";

    List<Segment> segments = calculateSegments(sourceA, sourceB);

    AlexandriaSoftAssertions softly = new AlexandriaSoftAssertions();
    SegmentMatcher expected0 = sM(aligned)//
        .tokensA(markupOpen("s"))//
        .tokensB(markupOpen("s"));
    assertThat(segments.get(0)).matches(expected0);

    SegmentMatcher expected1 = sM(omission)//
        .tokensA(text("a "))//
        .tokensB();
    assertThat(segments.get(1)).matches(expected1);

    SegmentMatcher expected2 = sM(aligned)//
        .tokensA(text("text"), markupClose("s"))//
        .tokensB(text("text"), markupClose("s"));
    assertThat(segments.get(2)).matches(expected2);

    softly.assertAll();
  }

  private List<Segment> calculateSegments(String input, String input1) {
    return store.runInTransaction(() -> {
      LMNLImporter importer = new LMNLImporter(store);
      DocumentWrapper document1 = importer.importLMNL(input);
      DocumentWrapper document2 = importer.importLMNL(input1);
      Set<String> tei = new HashSet<>(Collections.singletonList("TEI"));
      TAGView ignoreTEI = new TAGView(store).setMarkupToExclude(tei);
      List<TAGToken> tokens1 = new Tokenizer(document1, ignoreTEI).getTAGTokens();
      List<TAGToken> tokens2 = new Tokenizer(document2, ignoreTEI).getTAGTokens();
      Scorer scorer = new ContentScorer();
      Segmenter segmenter = new ContentSegmenter();
      return new ViewAligner(scorer, segmenter).align(tokens1, tokens2);
    });
  }

}