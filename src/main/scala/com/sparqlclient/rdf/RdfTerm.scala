package com.sparqlclient.rdf

/**
 * Created by basca on 23/07/14.
 */

/**
 * represents the unified type of all RDF terms.
 */
trait RdfTerm {
  /**
   * the [[http://www.w3.org/TeamSubmission/n3/ Notation 3]] representation of the ''RDF'' term
   *
   * @return the string representation
   */
  def n3: String

  override def toString: String = n3
}
