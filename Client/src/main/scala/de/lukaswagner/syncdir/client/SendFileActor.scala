package de.lukaswagner.syncdir.client

import java.nio.file.Paths

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import akka.stream.scaladsl.{FileIO, StreamRefs}
import akka.stream.{ActorMaterializer, SourceRef}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import de.lukaswagner.syncdir.common.{UploadFileRequest, UploadFileStream}

import scala.concurrent.Future


/**
 * Sends file to server
 */
class SendFileActor extends Actor with LazyLogging {
  implicit val mat: ActorMaterializer = ActorMaterializer()(context)

  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case UploadFileRequest(filePath, transmissionID) =>
      val file = Paths.get(s"${Config.syncDir}/$filePath")
      val sourceRef: Future[SourceRef[ByteString]] = FileIO.fromPath(file).runWith(StreamRefs.sourceRef())
      val message: Future[UploadFileStream] = sourceRef.map(UploadFileStream(transmissionID, filePath, _, context.parent))
      message pipeTo sender()

    case s =>
      logger.info(s"SendFileActor does not support receiving method of type ${s.getClass}")
  }
}

object SendFileActor {
  def props: Props = Props(new SendFileActor())
}











