package com.sparqlclient.rdf

import java.net.URI

/**
 * Created by basca on 23/07/14.
 */
class URIRef(val value:URI) extends RdfTerm {
  override def n3: String = s"<${value.toString}>"
}

object URIRef {
  def apply(value: String):URIRef = {
    new URIRef(new URI(value))
  }
}
