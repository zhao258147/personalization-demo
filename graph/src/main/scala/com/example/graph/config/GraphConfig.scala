package com.example.graph.config

case class GraphConfig (
  http: HttpConfig
)

case class HttpConfig (
  interface: String,
  port: Int
)