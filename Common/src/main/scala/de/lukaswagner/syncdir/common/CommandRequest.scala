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

case class DownloadFileRequest(filePath: String, transmissionID: String) extends CommandRequest

case class UploadFileRequest(filePath: String, transmissionID: String) extends CommandRequest

case class DeleteFileRequest(filePath: String, transmissionID: String) extends CommandRequest

case class DownloadFileStream(transmissionID: String, filePath: String, sourceRef: SourceRef[ByteString]) extends CommandRequest

case class UploadFileStream(transmissionID: String, filePath: String, sourceRef: SourceRef[ByteString], clientCoordinatorActor: ActorRef) extends CommandRequest

case class TransmissionsAcknowledgement(transmissionID: String) extends CommandRequest

