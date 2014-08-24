import com.sparqlclient.{SparqlClient, DataFormat}
import com.sparqlclient.rdf.RdfTerm

def getDBpediaResults(maxResults: Int=10, format: DataFormat.Value=DataFormat.Xml): Seq[Seq[RdfTerm]] = {
  val dbpedia = SparqlClient("http://dbpedia.org/sparql", format=format)

  val query = s"""
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?p ?label
    WHERE { <http://dbpedia.org/resource/Asturias> ?p ?label }
    LIMIT $maxResults
                """
  val results:Seq[Seq[RdfTerm]] = dbpedia(query, 10)._2.toSeq
  dbpedia.shutdown()
  results
}

val results:Seq[Seq[RdfTerm]] = getDBpediaResults(maxResults = 3, format = DataFormat.Json)
println(s"DATA=$results")