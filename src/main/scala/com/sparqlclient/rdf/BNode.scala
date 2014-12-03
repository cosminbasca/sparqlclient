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

/**
 * Created by basca on 23/07/14.
 */

/**
 * an ''RDF'' Blank Node
 *
 * @param value the blank node id
 */
class BNode(val value:String) extends RdfTerm {
  override def n3: String = s"_:$value"
}

/**
 * factory for creating blank nodes
 */
object BNode {
  /**
   * create a blank node from a string id
   * @param value the id
   * @return the blank node
   */
  def apply(value: String): BNode = {
    new BNode(value)
  }
}
