package com.company.models

import io.circe.Json

sealed trait JobStatus

object JobStatus {
  case object Pending extends JobStatus
  case object Running extends JobStatus
  case object Succeeded extends JobStatus
  case object Failed extends JobStatus
  case object Unknown extends JobStatus
}
