package de.lukaswagner.syncdir.server

import java.io.File
import java.nio.file.{Path, Paths}

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import akka.stream.scaladsl.{FileIO, StreamRefs}
import akka.stream.{ActorMaterializer, SourceRef}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import de.lukaswagner.syncdir.common.{DownloadFileRequest, DownloadFileStream, Utils}

import scala.concurrent.Future

/**
 * Sends file to client
 */
class SendFileActor extends Actor with LazyLogging {

  import context.dispatcher

  implicit val mat: ActorMaterializer = ActorMaterializer()(context)

  override def receive: Receive = {
    case DownloadFileRequest(relativeFilePath, transmissionID) =>
      val filePath: Path = Utils.createOsIndependentPath(Config.syncDir.toString, relativeFilePath)
      val ref: Future[SourceRef[ByteString]] = FileIO.fromPath(filePath).runWith(StreamRefs.sourceRef())
      val reply: Future[DownloadFileStream] = ref.map(DownloadFileStream(transmissionID, relativeFilePath, _))
      reply pipeTo sender()

    case s =>
      logger.info(s"SendFileActor does not support receiving method of type ${s.getClass}")
  }
}

object SendFileActor {
  def props: Props =
    Props(new SendFileActor())
}


