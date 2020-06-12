package de.lukaswagner.syncdir.server

import java.io.File
import java.nio.file.{Path, Paths}

import akka.actor.{Actor, Props}
import akka.stream.scaladsl.{FileIO, Source}
import akka.stream.{ActorMaterializer, IOResult}
import com.typesafe.scalalogging.LazyLogging
import de.lukaswagner.syncdir.common.{TransmissionsAcknowledgement, UploadFileStream, Utils}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Receives files from client
 */
class ReceiveFileActor extends Actor with LazyLogging {

  import context.dispatcher

  implicit val mat: ActorMaterializer = ActorMaterializer()(context)

  override def receive: Receive = {
    case UploadFileStream(transmissionID, relativeFilePath, sourceRef, clientCoordinatorActor) =>
      val filePath: Path = Utils.createOsIndependentPath(Config.syncDir.toString, relativeFilePath)

      if (!filePath.toFile.exists()) {
        filePath.getParent.toFile.mkdirs()
        filePath.toFile.createNewFile()
      }

      val result: Future[IOResult] = Source.fromGraph(sourceRef).runWith(FileIO.toPath(filePath))
      result.onComplete {
        case Success(_) =>
          clientCoordinatorActor ! TransmissionsAcknowledgement(transmissionID)
        case Failure(exception) =>
          logger.error(s"ERROR when receiving file $relativeFilePath")
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





