package test
import scala.actors.Actor._
class MessagePassing {
  var startTime : Long = 0
  val caller = self

  val engrossedActor = actor {
    println("Number of messages received so far? " + mailboxSize)
    caller ! "send"
    Thread.sleep(3000)
    println("Number of messages received while I was busy? " + mailboxSize)
    receive {
      case msg =>
        val receivedTime = System.currentTimeMillis() - startTime
        println("Received message " + msg + "after " + receivedTime + " ms")
    }
    caller ! "received"
  }

  receive { case _ =>}

  println("Sending Message ")
  startTime = System.currentTimeMillis()
  engrossedActor ! "hello buddy"
  val endTime = System.currentTimeMillis() - startTime

  printf("Took less than %dms to send message\n", endTime)

  receive {
    case _ =>
  }
}

object MessagePassing extends App {
  new MessagePassing()
}