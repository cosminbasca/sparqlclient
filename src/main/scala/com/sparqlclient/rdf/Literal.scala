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
