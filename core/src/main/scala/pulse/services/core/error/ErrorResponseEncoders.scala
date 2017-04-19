package pulse.services.core.error

import io.circe.Encoder

trait ErrorResponseEncoders {

  implicit val exceptionEncoder = Encoder.instance[Throwable] { e =>
    ???
  }

}
