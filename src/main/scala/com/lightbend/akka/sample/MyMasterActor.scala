/**
  * Created by zhouqihua on 2017/6/24.
  */

package com.lightbend.akka.sample

import akka.actor._
import com.typesafe.config._
import scala.io.StdIn

object MyMasterActor {

  import MasterActor._

  def main(args: Array[String]): Unit = {

    val conf = """
      akka {
        actor {
          provider = "akka.remote.RemoteActorRefProvider"
        }
        remote {
          enabled-transports = ["akka.remote.netty.tcp"]
          netty.tcp {
            hostname = "0.0.0.0"
            port = 17200
          }
        }
      }
    """

    val config = ConfigFactory.parseString(conf)
    val system = ActorSystem("masterActor", config)
    val masterActor = system.actorOf(Props[MasterActor], name = "masterActor")


    StdIn.readLine()

    val taskId: String = "FLOW-0000001"
    val master: String = "akka.tcp://masterActor@0.0.0.0:17200/user/masterActor"
    val from: String = "akka.tcp://localActor@0.0.0.0:17201/user/localActor"
    val to: String = "akka.tcp://remoteActor@0.0.0.0:17202/user/remoteActor"
    val content: String = "****** hello remote ******"
    val description: String = "Version Beta 0.1"

    masterActor ! SubmitNewFlow(taskId, master, from, to, content, description)

    StdIn.readLine()
    system.terminate()

  }
}
object MasterActor {
  final case class SubmitNewFlow(taskId: String, master: String, from: String, to: String, content: String, description: String)
  final case class AggregateFlow(taskId: String, master: String, from: String, to: String, content: String, description: String)
}

class MasterActor extends Actor with ActorLogging {
  import MasterActor._
  import LocalActor._

  override def receive: Receive = {

    case SubmitNewFlow(taskId, master, from, to, content, description) =>
      log.info(s"[MasterActor] submitNewFlow; [From sender]: $sender")
      log.info(s"[Flow Info] from: $from; to: $to; content: $content")

      val localActor = context.actorSelection(s"$from")
      localActor ! TransferFlow(taskId, master, from, to, content, description)

    case AggregateFlow(taskId, master, from, to, content, description) =>
      log.info(s"[MasterActor] aggregateFlow; [From sender]: $sender")
      log.info(s"[Flow Info] from: $from; to: $to; content: $content")
  }
}
