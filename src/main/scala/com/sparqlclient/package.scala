package com

/**
 * Created by basca on 21/07/14.
 */
package object sparqlclient {

  object DataFormat extends Enumeration {
    type DataFormat = String
    val JSON = "json"
    val JSONLD = "json-ld"
    val XML = "xml"
    val TURTLE = "n3"
    val N3 = "n3"
    val RDF = "rdf"
  }

  object QueryType extends Enumeration {
    type QueryType = String
    val SELECT = "SELECT"
    val CONSTRUCT = "CONSTRUCT"
    val ASK = "ASK"
    val DESCRIBE = "DESCRIBE"
    val INSERT = "INSERT"
    val DELETE = "DELETE"
    val CREATE = "CREATE"
    val CLEAR = "CLEAR"
    val DROP = "DROP"
    val LOAD = "LOAD"
    val COPY = "COPY"
    val MOVE = "MOVE"
    val ADD = "ADD"
  }

}
