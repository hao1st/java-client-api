/*
 * Copyright 2013-2019 MarkLogic Corporation
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

import com.marklogic.client.io.marker.StructureWriteHandle;

/**
 * A RawStructuredQueryDefinition provides access to a structured query
 * in a JSON or XML representation.
 */
public interface RawStructuredQueryDefinition extends RawQueryDefinition {
  /**
   * Specifies the handle for the JSON or XML representation
   * of a structured query and returns the query definition.
   * @param handle	the JSON or XML handle.
   * @return	the query definition.
   */
  RawStructuredQueryDefinition withHandle(StructureWriteHandle handle);

  /**
   * Returns the structured query definition as a serialized XML string.
   *
   * @return The serialized definition.
   */
  String serialize();

  /**
   * Returns the query criteria, that is the query string.
   * @return The query string.
   */
  String getCriteria();

  /**
   * Sets the query criteria as a query string.
   * @param criteria The query string.
   */
  void setCriteria(String criteria);

  /**
   * Sets the query criteria as a query string and returns the query
   * definition as a fluent convenience.
   * @param criteria The query string.
   * @return	This query definition.
   */
  RawStructuredQueryDefinition withCriteria(String criteria);
}
