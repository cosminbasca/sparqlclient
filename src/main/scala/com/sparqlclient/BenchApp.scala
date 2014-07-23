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
    SELECT ?p ?label
    WHERE { <http://dbpedia.org/resource/Asturias> ?p ?label }
                      """)
    dbpedia.setMethod(POST)

    dbpedia.setReturnFormat(DataFormat.JSON)
//    dbpedia.setReturnFormat(DataFormat.XML)
//    dbpedia.setReturnFormat(DataFormat.JSONLD)
//    dbpedia.setReturnFormat(DataFormat.RDF)
//    dbpedia.setReturnFormat(DataFormat.CSV)
    println(dbpedia)
    for (results <- dbpedia.queryResults()) {
      println(results.toList)
    }


//    println(dbpedia.waitForResults())

    println("done")
    dbpedia.shutdown()
    println("after shutdown ... ")
  }
}
