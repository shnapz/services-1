package pulse.services

import com.twitter.util._
import fs2.{Strategy, Task}

package object core {

  def toTask[A](fa: Future[A])(implicit strategy: Strategy): Task[A] =
    Task.async { cb => fa.respond {
      case Return(a) => cb(Right(a))
      case Throw(t) => cb(Left(t))
    }
    }
}
