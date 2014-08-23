import com.sparqlclient.{DataFormat, SparqlClient}
import com.sparqlclient.rdf.RdfTerm

import collection.mutable.Stack
import org.scalatest._

/**
 * Created by basca on 13/06/14.
 */

class TestDbpedia extends FlatSpec with BeforeAndAfter {
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

  "SparqlClient" should "be able to retrieve XML results from DBPEDIA if endpoint is up!" in {
    val maxResults: Int = 10
    val results:Seq[Seq[RdfTerm]] = getDBpediaResults(maxResults = maxResults, format = DataFormat.Xml)
    assert(results.length == maxResults, "the number of results differs")
  }

  "SparqlClient" should "be able to retrieve JSON results from DBPEDIA if endpoint is up!" in {
    val maxResults: Int = 10
    val results:Seq[Seq[RdfTerm]] = getDBpediaResults(maxResults = maxResults, format = DataFormat.Json)
    assert(results.length == maxResults, "the number of results differs")
  }
}
