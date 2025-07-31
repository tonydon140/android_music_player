package com.tonydon.music_tangjian.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.tencent.mmkv.MMKV
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object ApiKeyCryptoUtil {

    private const val KEY_ALIAS = "api_key_aes_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val ENCRYPTED_API_KEY = "encrypted_api_key"
    private const val ENCRYPTED_IV = "api_key_iv"

    private val mmkv: MMKV = MMKV.defaultMMKV()

    /** 初始化时调用一次，确保密钥存在 */
    fun generateAESKeyIfNecessary() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keySpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(keySpec)
            keyGenerator.generateKey()
        }
    }

    /** 加密并保存 API key */
    fun encryptAndStoreApiKey(apiKey: String) {
        val secretKey = getSecretKey() ?: return
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(apiKey.toByteArray(Charsets.UTF_8))

        // Base64 编码存储
        mmkv.encode(ENCRYPTED_API_KEY, Base64.encodeToString(encrypted, Base64.DEFAULT))
        mmkv.encode(ENCRYPTED_IV, Base64.encodeToString(iv, Base64.DEFAULT))
    }

    /** 解密并返回 API key（返回 null 表示未保存） */
    fun getDecryptedApiKey(): String? {
        val encryptedBase64 = mmkv.decodeString(ENCRYPTED_API_KEY) ?: return null
        val ivBase64 = mmkv.decodeString(ENCRYPTED_IV) ?: return null

        val encrypted = Base64.decode(encryptedBase64, Base64.DEFAULT)
        val iv = Base64.decode(ivBase64, Base64.DEFAULT)

        val secretKey = getSecretKey() ?: return null
        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val decryptedBytes = cipher.doFinal(encrypted)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /** 删除保存的密钥 */
    fun clearApiKey() {
        mmkv.removeValueForKey(ENCRYPTED_API_KEY)
        mmkv.removeValueForKey(ENCRYPTED_IV)
    }

    /** 从 Keystore 获取 AES 密钥 */
    private fun getSecretKey(): SecretKey? {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return entry?.secretKey
    }
}
