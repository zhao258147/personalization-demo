package com.example.personalization

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[PageViewStart], name = "PageViewStart"),
    new JsonSubTypes.Type(value = classOf[PageViewEnd], name = "PageViewEnd"),
    new JsonSubTypes.Type(value = classOf[PageViewFocus], name = "PageViewFocus"),
    new JsonSubTypes.Type(value = classOf[PageViewScroll], name = "PageViewScroll")))
sealed trait PageViewEventType

case class PageViewStart(nodeId: String, referrer: String) extends PageViewEventType
case class PageViewEnd(nodeId: String, permalink: String) extends PageViewEventType
case class PageViewFocus(nodeId: String, permalink: String, time: Long) extends PageViewEventType
case class PageViewScroll(nodeId: String, permalink: String) extends PageViewEventType

