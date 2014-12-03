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
package com.sparqlclient

import com.ning.http.client.Response

/**
 * Created by basca on 25/07/14.
 */
class HttpException(msg: String) extends RuntimeException(msg)

object HttpException {
  def apply(code: Int) : HttpException = new HttpException(s"HTTP Request failed with status code: $code")

  def apply(response: Response): HttpException = new HttpException(s"HTTP Request failed with status code: ${response.getStatusCode} (content: ${response.getResponseBody}, headers: ${response.getHeaders})")

  def apply(code: Int, cause: Throwable): Throwable = HttpException(code).initCause(cause)

  def apply(response: Response, cause: Throwable): Throwable = HttpException(response).initCause(cause)
}
