package de.lukaswagner.syncdir.server

import java.nio.file.Paths

import akka.actor.{Actor, Props}
import akka.stream.scaladsl.{FileIO, Source}
import akka.stream.{ActorMaterializer, IOResult}
import com.typesafe.scalalogging.LazyLogging
import de.lukaswagner.syncdir.common.{TransmissionsAcknowledgement, UploadFileStream}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Receives files from client
 */
class ReceiveFileActor extends Actor with LazyLogging {

  import context.dispatcher

  implicit val mat: ActorMaterializer = ActorMaterializer()(context)

  override def receive: Receive = {
    case UploadFileStream(transmissionID, filePath, sourceRef, clientCoordinatorActor) =>
      val file = Paths.get(s"${Config.storedFilesDir}/$filePath")
      if (!file.toFile.exists()) {
        file.getParent.toFile.mkdirs()
        file.toFile.createNewFile()
      }

      val result: Future[IOResult] = Source.fromGraph(sourceRef).runWith(FileIO.toPath(file))
      result.onComplete {
        case Success(_) =>
          clientCoordinatorActor ! TransmissionsAcknowledgement(transmissionID)
        case Failure(exception) =>
          logger.error(s"ERROR while receiving file $filePath")
          logger.error(exception.toString)
      }


    case s =>
      logger.info(s"ReceiveFileActor does not support receiving method of type ${s.getClass}")
  }
}

object ReceiveFileActor {
  def props: Props =
    Props(new ReceiveFileActor())
}





