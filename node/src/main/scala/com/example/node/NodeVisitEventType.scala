package com.example.node

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[NodeVisitStart], name = "NodeVisitStart"),
    new JsonSubTypes.Type(value = classOf[NodeVisitEnd], name = "NodeVisitEnd"),
    new JsonSubTypes.Type(value = classOf[NodeVisitFocus], name = "NodeVisitFocus"),
    new JsonSubTypes.Type(value = classOf[NodeVisitScroll], name = "NodeVisitScroll")))
sealed trait NodeVisitEventType

case class NodeVisitStart(personId: String, referrer: String) extends NodeVisitEventType
case class NodeVisitEnd(personId: String, permalink: String) extends NodeVisitEventType
case class NodeVisitFocus(personId: String, permalink: String, time: Long) extends NodeVisitEventType
case class NodeVisitScroll(personId: String, permalink: String) extends NodeVisitEventType

