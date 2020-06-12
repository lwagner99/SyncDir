package de.lukaswagner.syncdir.client

import java.nio.file.{Path, Paths}

import akka.actor.{Actor, _}
import akka.stream.scaladsl.{FileIO, Source}
import akka.stream.{ActorMaterializer, IOResult, SourceRef}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import de.lukaswagner.syncdir.common.{DownloadFileStream, TransmissionsAcknowledgement, Utils}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Receives files from server
 */
class ReceiveFileActor extends Actor with LazyLogging {
  implicit val mat: ActorMaterializer = ActorMaterializer()(context)

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case DownloadFileStream(transmissionID: String, relativeFilePath: String, ref: SourceRef[ByteString]) =>
      val filePath: Path = Utils.createOsIndependentPath(Config.syncDir.toString, relativeFilePath)
      if (!filePath.toFile.exists()) {
        filePath.getParent.toFile.mkdirs()
        filePath.toFile.createNewFile()
      }

      val result: Future[IOResult] = Source.fromGraph(ref).runWith(FileIO.toPath(filePath))
      result.onComplete {
        case Success(_) =>
          context.parent ! TransmissionsAcknowledgement(transmissionID)
        case Failure(exception) =>
          logger.error(s"ERROR while receiving file $relativeFilePath")
          logger.error(exception.toString)
      }

    case s =>
      logger.info(s"ReceiveFileActor does not support receiving method of type ${s.getClass}")
  }
}

object ReceiveFileActor {
  def props: Props = Props(new ReceiveFileActor())
}




