import com.sparqlclient.SparqlClient
import com.sparqlclient.rdf.RdfTerm

import collection.mutable.Stack
import org.scalatest._

/**
 * Created by basca on 13/06/14.
 */

class TestDbpedia extends FlatSpec with BeforeAndAfter {
  "SparqlClient" should "be able to retrieve results from DBPEDIA if endpoint is up!" in {
    val dbpedia = SparqlClient("http://dbpedia.org/sparql")

    val query = """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?p ?label
    WHERE { <http://dbpedia.org/resource/Asturias> ?p ?label }
                """
    val results:Seq[Seq[RdfTerm]] = dbpedia(query, 10).toSeq
    assert(results.length == 255, "the number of results differs")

    dbpedia.shutdown()
  }
}