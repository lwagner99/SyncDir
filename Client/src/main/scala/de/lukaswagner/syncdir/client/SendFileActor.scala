package de.lukaswagner.syncdir.client

import java.nio.file.{Path, Paths}

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import akka.stream.scaladsl.{FileIO, StreamRefs}
import akka.stream.{ActorMaterializer, SourceRef}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import de.lukaswagner.syncdir.common.{UploadFileRequest, UploadFileStream, Utils}

import scala.concurrent.Future


/**
 * Sends file to server
 */
class SendFileActor extends Actor with LazyLogging {
  implicit val mat: ActorMaterializer = ActorMaterializer()(context)

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case UploadFileRequest(relativeFilePath, transmissionID) =>
      val filePath: Path = Utils.createOsIndependentPath(Config.syncDir.toString, relativeFilePath)
      val sourceRef: Future[SourceRef[ByteString]] = FileIO.fromPath(filePath).runWith(StreamRefs.sourceRef())
      val message: Future[UploadFileStream] = sourceRef.map(UploadFileStream(transmissionID, relativeFilePath, _, context.parent))
      message pipeTo sender()

    case s =>
      logger.info(s"SendFileActor does not support receiving method of type ${s.getClass}")
  }
}

object SendFileActor {
  def props: Props = Props(new SendFileActor())
}











