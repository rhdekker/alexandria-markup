package nl.knaw.huygens.alexandria.storage.wrappers;

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

import org.assertj.core.api.AbstractObjectAssert;

public class AnnotationWrapperAssert extends AbstractObjectAssert<AnnotationWrapperAssert, AnnotationWrapper> {

  public AnnotationWrapperAssert(final AnnotationWrapper actual) {
    super(actual, AnnotationWrapperAssert.class);
  }

  public AnnotationWrapperAssert hasTag(final String tag) {
    isNotNull();
    if (!actual.getTag().equals(tag)) {
      failWithMessage("Expected annotation's tag to be <%s> but was <%s>", tag, actual.getTag());
    }
    return myself;
  }

}
