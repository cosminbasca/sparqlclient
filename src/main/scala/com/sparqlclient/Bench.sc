//
// author: Cosmin Basca
//
// Copyright 2010 University of Zurich
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
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