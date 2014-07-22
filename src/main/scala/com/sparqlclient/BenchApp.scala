package com.sparqlclient

import java.net.URL

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * Created by basca on 22/07/14.
 */
object BenchApp extends App {
  override def main(args: Array[String]): Unit = {
    val dbpedia = new SparqlClient(new URL("http://dbpedia.org/sparql"))
    dbpedia.setQuery( """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?label
    WHERE { <http://dbpedia.org/resource/Asturias> rdfs:label ?label }
    LIMIT 100
                      """)
    dbpedia.setReturnFormat(DataFormat.CSV)
    val str = dbpedia.waitForResults()
    println(str)
  }
}
