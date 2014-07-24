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
