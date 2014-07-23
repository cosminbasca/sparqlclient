import java.net.{URI, URL}

import com.sparqlclient.rdf.{Literal, BNode, URIRef, Node}
import com.sparqlclient.{RequestMethod, DataFormat, SparqlClient}

val dbpedia = new SparqlClient(new URL("http://dbpedia.org/sparql"))
dbpedia.setQuery( """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?label
    WHERE { <http://dbpedia.org/resource/Asturias> rdfs:label ?label }
    LIMIT 100
                  """)
dbpedia.setReturnFormat(DataFormat.JSON)

for (results <- dbpedia.query) {
  for (res <- results) {
    println(res.n3)
  }
}