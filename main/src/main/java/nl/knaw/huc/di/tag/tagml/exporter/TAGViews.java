package nl.knaw.huc.di.tag.tagml.exporter;

/*-
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
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.view.TAGView;

import java.util.HashSet;

public class TAGViews {
  public static TAGView getShowAllMarkupView(final TAGStore store) {
    return new TAGView(store).setMarkupToExclude(new HashSet<>());
  }

  public static TAGView getShowNoMarkupView(final TAGStore store) {
    return new TAGView(store).setMarkupToInclude(new HashSet<>());
  }
}
