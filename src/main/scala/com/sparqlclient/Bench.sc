import com.sparqlclient.SparqlClient

import scala.concurrent.Await
import scala.concurrent.duration.Duration

val dbpedia = SparqlClient("http://dbpedia.org/sparql", format = "json")
val query = """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?p ?label
    WHERE { <http://dbpedia.org/resource/Asturias> ?p ?label }
            """
//println(dbpedia)

val futureResults = dbpedia(query)
println("after submitting query! ")
println("waiting for results....")

println(Await.result(futureResults, Duration(10, "seconds")).toList)

//
//for ((results, i) <- dbpedia(query, 10).zipWithIndex) {
//  println(s"$i : ${results.toList}")
//}
//    println(dbpedia.waitForResults())
println("done")
dbpedia.shutdown()