package com.example.personalization.config

case class PersonalizationConfig (
  http: HttpConfig
)

case class HttpConfig (
  interface: String,
  port: Int
)