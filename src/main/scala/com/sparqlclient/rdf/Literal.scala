package com.sparqlclient.rdf

import java.net.URI

/**
 * Created by basca on 23/07/14.
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

object Literal {
  def apply(value: String): Literal = {
    new Literal(value, None, None)
  }

  def apply(value: String, language: String): Literal = {
    new Literal(value, Some(language), None)
  }

  def apply(value: String, dataType: URI): Literal = {
    new Literal(value, None, Some(dataType))
  }
}
