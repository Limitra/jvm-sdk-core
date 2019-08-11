package com.limitra.sdk.core.provider

import com.google.api.client.util.Base64
import com.limitra.sdk.core._
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
  * AES and DES are used for encryption methods.
  */
sealed class CryptoProvider {
  private val _config = Config("Security")

  val AES = new Crypto("AES")
  val DES = new Crypto("DES")

  protected class Crypto(algorithmName: String) {
    private val _secret = _config.String("Secret")

    private def _encodeBase64(bytes: Array[Byte]) = Base64.encodeBase64String(bytes)

    private def _decodeBase64(string: String) = Base64.decodeBase64(string)

    private def _cipher(mode: Int): Cipher = {
      val encipher = Cipher.getInstance(algorithmName + "/ECB/PKCS5Padding")
      encipher.init(mode, new SecretKeySpec(_secret.getBytes("UTF-8"), algorithmName))
      encipher
    }

    def Encrypt(value: String): String = {
      try {
        val encoder = _cipher(Cipher.ENCRYPT_MODE)
        _encodeBase64(encoder.doFinal(value.getBytes("UTF-8")))
      } catch {
        case ex: Exception => {
          ex.getMessage()
        }
      }
    }

    def Decrypt(value: String): Option[String] = {
      try {
        val decoder = _cipher(Cipher.DECRYPT_MODE)
        Some(new String(decoder.doFinal(_decodeBase64(value)), "UTF-8"))
      } catch {
        case ex: Exception => {
          None
        }
      }
    }
  }
}
