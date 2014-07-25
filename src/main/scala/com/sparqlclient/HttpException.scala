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
