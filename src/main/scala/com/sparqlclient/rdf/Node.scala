package com.sparqlclient.rdf

/**
 * Created by basca on 23/07/14.
 */
trait Node {
  def n3: String

  override def toString: String = n3
}
