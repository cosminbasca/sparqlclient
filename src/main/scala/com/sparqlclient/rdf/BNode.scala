package com.sparqlclient.rdf

/**
 * Created by basca on 23/07/14.
 */
class BNode(val value:String) extends Node {
  override def n3: String = s"_:$value"
}
