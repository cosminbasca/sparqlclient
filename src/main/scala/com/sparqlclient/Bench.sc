import com.sparqlclient.{DataFormat, SparqlClient}

val dbpedia = SparqlClient("http://dbpedia.org/sparql", format = DataFormat.Xml)
val query = """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?p ?label
    WHERE { <http://dbpedia.org/resource/Asturias> ?p ?label }
    LIMIT 3
            """
dbpedia.setQuery(query)
dbpedia.rawResults(10)
