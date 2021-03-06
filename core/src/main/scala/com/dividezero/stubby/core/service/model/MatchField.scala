package com.dividezero.stubby.core.service.model

object FieldType extends Enumeration {
  type FieldType = Value
  val PATH, METHOD, QUERY_PARAM, HEADER, BODY = Value
}

object MatchType extends Enumeration {
  type MatchType = Value
  val NOT_FOUND, MATCH_FAILURE, MATCH = Value
}

import MatchType._
import FieldType._

case class PartialMatchField(
    val fieldType: FieldType,
    val fieldName: String,
    val expectedValue: Any) { // expected value can be a Pattern, a JSON object etc.

  def asMatch(actualValue: Any) = new MatchField(this, MATCH, Some(actualValue))
  def asNotFound = new MatchField(this, NOT_FOUND)
  def asMatchFailure(actualValue: Any) = new MatchField(this, MATCH_FAILURE, Some(actualValue))
  def asMatchFailure(actualValue: Any, message: String) = new MatchField(this, MATCH_FAILURE, None, Some(message))
}

case class MatchField(
    fieldType: FieldType,
    fieldName: String,
    expectedValue: Any,
    matchType: MatchType,
    actualValue: Option[Any], // could be string, JSON object etc.
    message: Option[String]) {

  def this(
    partial: PartialMatchField,
    matchType: MatchType,
    actualValue: Option[Any] = None,
    message: Option[String] = None) =
    this(
      partial.fieldType,
      partial.fieldName,
      partial.expectedValue,
      matchType,
      actualValue,
      message)

  def score: Int = matchType match { // attempt to give some weight to matches so we can guess 'near misses'
    case NOT_FOUND => 0
    case MATCH_FAILURE => fieldType match {
      case PATH | METHOD => 0 // these guys always exist, so the fact that they're found is unimportant
      case HEADER | QUERY_PARAM | BODY => 1 // if found but didn't match  
    }
    case MATCH => fieldType match {
      case PATH => 5 // path is most important in the match
      case _ => 2
    }
  }
  
}
