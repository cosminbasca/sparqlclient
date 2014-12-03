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
