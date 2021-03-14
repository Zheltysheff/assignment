package com.company.models

case class JobSummary(pending: Int, running: Int, succeeded: Int, failed: Int) {

  private def takeWithLimit(count: Int, limit: Int): Int = if (count >= limit) limit else count

  def withLimit(limit: Int): JobSummary = this.copy(
    pending = takeWithLimit(pending, limit),
    running = takeWithLimit(running, limit),
    succeeded = takeWithLimit(succeeded, limit),
    failed = takeWithLimit(failed, limit)
  )

}
