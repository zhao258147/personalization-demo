package com.example.node.config

case class NodeConfig (
  http: HttpConfig
)

case class HttpConfig (
  interface: String,
  port: Int
)