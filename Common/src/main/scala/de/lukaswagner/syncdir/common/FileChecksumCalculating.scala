package de.lukaswagner.syncdir.common

import java.io.{File, FileInputStream}
import java.security.{DigestInputStream, MessageDigest}

/**
 * Calculates FileChecksums
 */
trait FileChecksumCalculating {
  private val messageDigest: MessageDigest = MessageDigest.getInstance("MD5")

  /**
   * calculates checksum for byte array up to max 2 GB
   *
   * @param rawBytes
   * @return
   */
  def calculateChecksum(rawBytes: Array[Byte]): Int = {
    messageDigest.update(rawBytes)
    val encryptedString = new String(messageDigest.digest)
    encryptedString.hashCode
  }

  /**
   * Calculates checksum for file without size limitation
   *
   * @param file the file
   * @return
   */
  def calculateChecksum(file: File): Int = {
    val inputStream = new FileInputStream(file)
    val digestInputStream = new DigestInputStream(inputStream, messageDigest)
    val buffer = new Array[Byte](4096)

    while (digestInputStream.read(buffer) > -1) {
      messageDigest.update(buffer)
    }
    val encryptedString = new String(messageDigest.digest)
    digestInputStream.close()
    encryptedString.hashCode
  }

}
