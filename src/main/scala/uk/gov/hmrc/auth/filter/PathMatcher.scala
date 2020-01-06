/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.auth.filter

import scala.util.matching.Regex.Match


case class PathMatcher(pattern: String) {

  lazy val (regex, pathVariables) = {
    // TODO - support full Play route syntax
    val pathElements = pattern.split('/')
    val (regexElements, pathVariables) = pathElements.foldLeft((Seq[String](), Seq[String]())) {
      case ((regexElements, pathVariables), pathElement) =>
        if (pathElement.startsWith(":")) (regexElements :+ "([^/]+)", pathVariables :+ pathElement.drop(1))
        else (regexElements :+ pathElement, pathVariables)
    }
    val regex = regexElements.mkString("", "/", "$").r
    (regex, pathVariables)
  }

  def matchPath(path: String): Option[Map[String, String]] = {

    def processResult(result: Match): Map[String, String] =
      if (result.groupCount != pathVariables.size) throw new RuntimeException("Internal error, unexpected number of groups")
      else (pathVariables zip result.subgroups).toMap

    val matchResult = regex.findAllMatchIn(path).toSeq
    matchResult match {
      case Seq() => None
      case Seq(result) => Some(processResult(result))
      case _ => throw new IllegalArgumentException(s"path $path resulted in more than one match")
    }
  }

}
