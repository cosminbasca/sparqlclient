import com.sparqlclient.SparqlClient

val dbpedia = SparqlClient("http://dbpedia.org/sparql")
val query = """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?p ?label
    WHERE { <http://dbpedia.org/resource/Asturias> ?p ?label }
            """
println(dbpedia)

for ((results, i) <- dbpedia(query, 10).zipWithIndex) {
  println(s"$i : ${results.toList}")
}

//    println(dbpedia.waitForResults())

println("done")
dbpedia.shutdown()