package me.dafnik.angular_todos_backend

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.security.*
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

/**
 * Loading and generating of the keys for the JWT signing
 * If there are no certificates at the given path, they will be created automatically.
 *
 * @author Alexander Kauer
 * @version 1.0.0
 * @since 2022-12-25
 */
@Component
class KeyUtils(
    val environment: Environment,
    @Value(CONFIG_KEY_DIRECTORY) val directoryPath: String
) {

    private val log = LoggerFactory.getLogger(KeyUtils::class.java)

    val userAccessTokenPublicKey: RSAPublicKey
        get() = userAccessTokenKeyPair.public as RSAPublicKey

    val userAccessTokenPrivateKey: RSAPrivateKey
        get() = userAccessTokenKeyPair.private as RSAPrivateKey

    val userRefreshTokenPublicKey: RSAPublicKey
        get() = userRefreshTokenKeyPair.public as RSAPublicKey

    val userRefreshTokenPrivateKey: RSAPrivateKey
        get() = userRefreshTokenKeyPair.private as RSAPrivateKey

    @Value(CONFIG_KEY_USER_ACCESS_TOKEN_PRIVATE)
    private val userAccessTokenPrivateKeyPath: String = ""

    @Value(CONFIG_KEY_USER_ACCESS_TOKEN_PUBLIC)
    private val userAccessTokenPublicKeyPath: String = ""

    @Value(CONFIG_KEY_USER_REFRESH_TOKEN_PRIVATE)
    private val userRefreshTokenPrivateKeyPath: String = ""

    @Value(CONFIG_KEY_USER_REFRESH_TOKEN_PUBLIC)
    private val userRefreshTokenPublicKeyPath: String = ""

    private val userAccessTokenKeyPair: KeyPair by lazy {
        getKeyPair(userAccessTokenPublicKeyPath, userAccessTokenPrivateKeyPath)
    }

    private val userRefreshTokenKeyPair: KeyPair by lazy {
        getKeyPair(userRefreshTokenPublicKeyPath, userRefreshTokenPrivateKeyPath)
    }

    private fun getKeyPair(publicKeyPath: String, privateKeyPath: String): KeyPair {
        val keyPair: KeyPair
        val publicKeyFile = File(publicKeyPath)
        val privateKeyFile = File(privateKeyPath)

        if (publicKeyFile.exists() && privateKeyFile.exists()) {
            log.info("loading keys from file: {}, {}", publicKeyPath, privateKeyPath)

            val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")

            val publicKeyBytes: ByteArray = Files.readAllBytes(publicKeyFile.toPath())
            val publicKeySpec: EncodedKeySpec = X509EncodedKeySpec(publicKeyBytes)
            val publicKey: PublicKey = keyFactory.generatePublic(publicKeySpec)

            val privateKeyBytes: ByteArray = Files.readAllBytes(privateKeyFile.toPath())
            val privateKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
            val privateKey: PrivateKey = keyFactory.generatePrivate(privateKeySpec)

            keyPair = KeyPair(publicKey, privateKey)
            return keyPair
        } else {
            require(Arrays.stream(environment.activeProfiles).noneMatch { s -> s.equals("prod") }) {
                "public and private keys don't exist"
            }
        }

        Files.createDirectories(Paths.get(directoryPath))

        try {
            log.info("Generating new public and private keys: {}, {}", publicKeyPath, privateKeyPath)

            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(KEY_SIZE)
            keyPair = keyPairGenerator.generateKeyPair()

            FileOutputStream(publicKeyPath).use { fos ->
                val keySpec = X509EncodedKeySpec(keyPair.public.encoded)
                fos.write(keySpec.encoded)
            }
            FileOutputStream(privateKeyPath).use { fos ->
                val keySpec = PKCS8EncodedKeySpec(keyPair.private.encoded)
                fos.write(keySpec.encoded)
            }
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalStateException(e)
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }

        return keyPair
    }

    companion object {
        private const val KEY_SIZE = 2048
    }
}
