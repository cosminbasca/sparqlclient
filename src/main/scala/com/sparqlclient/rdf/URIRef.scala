//
// author: Cosmin Basca
//
// Copyright 2010 University of Zurich
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.sparqlclient.rdf

import java.net.URI

/**
 * Created by basca on 23/07/14.
 */

/**
 * an ''RDF'' URI Reference
 *
 * @param value the actual uri
 */
class URIRef(val value:URI) extends RdfTerm {
  override def n3: String = s"<${value.toString}>"
}

/**
 * factory for creating [[com.sparqlclient.rdf.URIRef]] instances
 */
object URIRef {
  /**
   * creates a ''URI reference'' from a string representation of a URI
   * @param value the uri string
   * @return the URI reference
   */
  def apply(value: String):URIRef = {
    new URIRef(new URI(value))
  }

  /**
   * creates a ''URI reference'' from a URI
   * @param value the URI
   * @return the URI reference
   */
  def apply(value: URI):URIRef = {
    new URIRef(value)
  }
}
