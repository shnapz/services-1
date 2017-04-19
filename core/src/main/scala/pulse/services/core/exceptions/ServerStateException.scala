package pulse.services.core.exceptions

case class ServerStateException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)