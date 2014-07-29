import com.sparqlclient.{HttpException, SparqlClient, DataFormat, RequestMethod}

val dbpedia = SparqlClient("http://dbpedia.org/sparql", format = DataFormat.Json)
val query = """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?p ?label
    WHERE { <http://dbpedia.org/resource/Asturias> ?p ?label }
    LIMIT 3
            """
//dbpedia.setQuery(query)
//println(s"REQUEST = \n${dbpedia.toString}")
try {
//  val results = dbpedia.queryResults(10)
  val results = dbpedia(query, 10)
  println(s"header = ${results._1}\n data = ${results._2.toList}")
} catch {
  case e: HttpException => println(s"ERROR = $e")
} finally {
  println("done")
  dbpedia.shutdown()
}