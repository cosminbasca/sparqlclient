import java.net.URL

import com.sparqlclient.SparqlClient
import com.sparqlclient.ALLOWED_DATA_FORMATS

import scala.util.matching.Regex
val pattern: Regex = new Regex( """(?i)((\s*BASE\s*<.*?>)\s*|(\s*PREFIX\s+.+:\s*<.*?>)\s*)*(CONSTRUCT|SELECT|ASK|DESCRIBE|INSERT|DELETE|CREATE|CLEAR|DROP|LOAD|COPY|MOVE|ADD)""",
  "g0","base", "prefixes", "queryType")
val query = """
PREFIX space: <http://purl.org/net/schemas/space/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
SELECT * WHERE {
  ?paper <http://data.semanticweb.org/ns/swc/ontology#isPartOf> <http://data.semanticweb.org/conference/iswc/2008/poster_demo_proceedings> .
  ?paper <http://swrc.ontoware.org/ontology#author> ?p .
  ?p rdfs:label ?n .
}
            """.trim


val w = new SparqlClient(new URL("http://haha.com"))
//println(w.parseQueryType(query))

val enc = new sun.misc.BASE64Encoder()

enc.encode( "user:pass".getBytes())
enc.encode( "user:pass".getBytes())
enc.encode( "user:pass".getBytes())
enc.encode( "user:pass".getBytes())