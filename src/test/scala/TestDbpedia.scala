import com.sparqlclient.{DataFormat, SparqlClient}
import com.sparqlclient.rdf.RdfTerm

import collection.mutable.Stack
import org.scalatest._

/**
 * Created by basca on 13/06/14.
 */

class TestDbpedia extends FlatSpec with BeforeAndAfterAll {
  var dbpedia: SparqlClient = null

  override def beforeAll(): Unit = {
    dbpedia = SparqlClient("http://dbpedia.org/sparql")
  }

  override def afterAll(): Unit = {
    dbpedia.shutdown()
  }

  def getDBpediaResults(maxResults: Int=10, format: DataFormat.Value=DataFormat.Xml): Seq[Seq[RdfTerm]] = {
    dbpedia.setReturnFormat(format)

    val query = s"""
    select distinct ?Concept where {[] a ?Concept} LIMIT $maxResults
                """
    val results:Seq[Seq[RdfTerm]] = dbpedia(query, 10)._2.toSeq
    results
  }

  "SparqlClient" should "be able to retrieve XML results from DBPEDIA if endpoint is up!" in {
    val maxResults: Int = 100
    val results:Seq[Seq[RdfTerm]] = getDBpediaResults(maxResults = maxResults, format = DataFormat.Xml)
    assert(results.length == maxResults, "the number of results differs")
  }

  "SparqlClient" should "be able to retrieve JSON results from DBPEDIA if endpoint is up!" in {
    val maxResults: Int = 100
    val results:Seq[Seq[RdfTerm]] = getDBpediaResults(maxResults = maxResults, format = DataFormat.Json)
    assert(results.length == maxResults, "the number of results differs")
  }
}
