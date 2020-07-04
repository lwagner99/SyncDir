package de.lukaswagner.syncdir.common

import akka.actor.ActorRef
import akka.stream.SourceRef
import akka.util.ByteString


abstract class CommandRequest

/**
 * signals server to send meta data of directory (homeCloud directory)
 */
case class SyncDirRequest() extends CommandRequest

case class SyncDirResponse(filesMeta: List[FileChecksum]) extends CommandRequest

object SyncDirResponse {
  def apply(filesMeta: List[FileChecksum]): SyncDirResponse = {
    val parsedFilesMeta = filesMeta.map((fileChecksum: FileChecksum) => FileChecksum(Utils.createOsIndependentStringPath(fileChecksum.filePath), fileChecksum.checkSum))
    new SyncDirResponse(parsedFilesMeta)
  }
}

case class DownloadFileRequest(filePath: String, transmissionID: String) extends CommandRequest

object DownloadFileRequest {
  def apply(filePath: String, transmissionID: String): DownloadFileRequest = {
    val parsedFileStreamPath = Utils.createOsIndependentStringPath(filePath)
    new DownloadFileRequest(parsedFileStreamPath, transmissionID)
  }
}

case class UploadFileRequest(filePath: String, transmissionID: String) extends CommandRequest

object UploadFileRequest {
  def apply(filePath: String, transmissionID: String): UploadFileRequest = {
    val parsedFileStreamPath = Utils.createOsIndependentStringPath(filePath)
    new UploadFileRequest(parsedFileStreamPath, transmissionID)
  }
}

case class DeleteFileRequest(filePath: String, transmissionID: String) extends CommandRequest

object DeleteFileRequest {
  def apply(filePath: String, transmissionID: String): DeleteFileRequest = {
    val parsedFileStreamPath = Utils.createOsIndependentStringPath(filePath)
    new DeleteFileRequest(parsedFileStreamPath, transmissionID)
  }
}

case class DownloadFileStream(transmissionID: String, filePath: String, sourceRef: SourceRef[ByteString]) extends CommandRequest

object DownloadFileStream {
  def apply(transmissionID: String, filePath: String, sourceRef: SourceRef[ByteString]): DownloadFileStream = {
    val parsedFileStreamPath = Utils.createOsIndependentStringPath(filePath)
    new DownloadFileStream(transmissionID, parsedFileStreamPath, sourceRef)
  }
}

case class UploadFileStream(transmissionID: String, filePath: String, sourceRef: SourceRef[ByteString], clientCoordinatorActor: ActorRef) extends CommandRequest

object UploadFileStream {
  def apply(transmissionID: String, filePath: String, sourceRef: SourceRef[ByteString], clientCoordinatorActor: ActorRef): UploadFileStream = {
    val parsedFileStreamPath = Utils.createOsIndependentStringPath(filePath)
    new UploadFileStream(transmissionID, parsedFileStreamPath, sourceRef, clientCoordinatorActor)
  }
}

case class TransmissionsAcknowledgement(transmissionID: String) extends CommandRequest

