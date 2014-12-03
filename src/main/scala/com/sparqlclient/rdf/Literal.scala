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
 * an ''RDF'' literal
 *
 * note: for creating literals it is advisable to use the literal creator factory methods
 *
 * @param value the value of the literal
 * @param language the language of the literal
 * @param dataType the type of the literal (if typed)
 */
class Literal(val value: String, val language: Option[String] = None, val dataType: Option[URI] = None) extends RdfTerm {
  override def n3: String = {
    val quotedValue: String = s""""$value""""
    language match {
      case Some(lang) => s"$quotedValue@$lang"
      case None => dataType match {
        case Some(dType) => s"$quotedValue^^${new URIRef(dType).n3}"
        case None => s"$quotedValue"
      }
    }
  }
}

/**
 * factory for creating ''RDF'' Literals
 */
object Literal {
  /**
   * create a simple literal
   * @param value the value
   * @return the simple literal
   */
  def apply(value: String): Literal = {
    new Literal(value, None, None)
  }

  /**
   * create an internationalized literal
   * @param value the value
   * @param language the language id (e.g., "en" for english)
   * @return the internationalized literal
   */
  def apply(value: String, language: String): Literal = {
    new Literal(value, Some(language), None)
  }

  /**
   * create a typed literal
   * @param value the value
   * @param dataType the datatype URI
   * @return the typed literal
   */
  def apply(value: String, dataType: URI): Literal = {
    new Literal(value, None, Some(dataType))
  }
}
