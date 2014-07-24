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
