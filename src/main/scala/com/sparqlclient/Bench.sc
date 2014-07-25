import com.sparqlclient.{SparqlClient, DataFormat}
import scala.concurrent.Await
import scala.concurrent.duration.Duration

val dbpedia = SparqlClient("http://dbpedia.org/sparql", format = DataFormat.Json)
val query = """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?p ?label
    WHERE { <http://dbpedia.org/resource/Asturias> ?p ?label }
    LIMIT 3
            """
println(dbpedia)
val futureResults = dbpedia(query)
println(s"the results = \n ${Await.result(futureResults, Duration(10, "seconds")).toList}")
println("done")
dbpedia.shutdown()