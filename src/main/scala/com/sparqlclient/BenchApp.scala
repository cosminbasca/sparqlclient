package com.sparqlclient

import java.net.URL

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * Created by basca on 22/07/14.
 */
object BenchApp extends App {
  override def main(args: Array[String]): Unit = {

    val dbpedia = SparqlClient("http://dbpedia.org/sparql")
    val query = """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?p ?label
    WHERE { <http://dbpedia.org/resource/Asturias> ?p ?label }
                """
//    println(dbpedia)

    for (results <- dbpedia(query)) {
      println(results.toList)
    }


    //    println(dbpedia.waitForResults())

    println("done")
    dbpedia.shutdown()
    println("after shutdown ... ")
  }
}
