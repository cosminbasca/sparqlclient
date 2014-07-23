package com.sparqlclient.rdf

/**
 * Created by basca on 23/07/14.
 */
class BNode(val value:String) extends RdfTerm {
  override def n3: String = s"_:$value"
}

object BNode {
  def apply(value: String): BNode = {
    new BNode(value)
  }
}
