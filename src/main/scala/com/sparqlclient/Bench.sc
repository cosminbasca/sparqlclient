import com.sparqlclient.{HttpException, SparqlClient, DataFormat, RequestMethod, HttpMethod}

//val dbpedia = SparqlClient("http://dbpedia.org/sparql", format = DataFormat.Json)
//val query = """
//    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
//    SELECT ?p ?label
//    WHERE { <http://dbpedia.org/resource/Asturias> ?p ?label }
//    LIMIT 3
//            """
////dbpedia.setQuery(query)
////println(s"REQUEST = \n${dbpedia.toString}")
//try {
////  val results = dbpedia.queryResults(10)
//  val results = dbpedia(query, 10)
//  println(s"header = ${results._1}\n data = ${results._2.toList}")
//} catch {
//  case e: HttpException => println(s"ERROR = $e")
//} finally {
//  println("done")
//  dbpedia.shutdown()
//}

//val local=SparqlClient("http://127.0.0.1:8090/sparql", format = DataFormat.Json, httpMethod = HttpMethod.GET)
//val query = """
//SELECT * WHERE {
//?s ?p ?o
//}
//            """
//val results = local(query, 10)
//println(s"header = ${results._1}\n data = ${results._2.toList}")