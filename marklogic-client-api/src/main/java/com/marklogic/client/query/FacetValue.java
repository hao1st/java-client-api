/*
 * Copyright 2012-2019 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.client.query;

/**
 * A FacetValue represents a single value returned in a set of facet results.
 */
public interface FacetValue {
  /**
   * Returns the name of the facet value.
   * @return The name.
   */
  String getName();

  /**
   * Returns the count of items for that facet value.
   * @return The count
   */
  long   getCount();

  /**
   * Returns the label associated with that facet value.
   * @return The label.
   */
  String getLabel();
}
