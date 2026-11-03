package com.cercafunctions.domain.authorization
import com.cercafunctions.domain.calendar.CalendarOccurrence
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
/** The unpredictable reference is also the version-HMAC key; neither key nor texts are stored. */
internal object ReferenceCrypto {
    private val random=SecureRandom()
    fun valid(reference:String)=Regex("[A-Za-z0-9_-]{43}").matches(reference)
    fun create():String=Base64.getUrlEncoder().withoutPadding().encodeToString(ByteArray(32).also(random::nextBytes))
    fun hash(reference:String)=hex(MessageDigest.getInstance("SHA-256").digest(reference.toByteArray(Charsets.UTF_8)))
    fun equal(a:String,b:String)=MessageDigest.isEqual(a.toByteArray(Charsets.US_ASCII),b.toByteArray(Charsets.US_ASCII))
    fun version(reference:String,event:CalendarOccurrence):String {
        val bytes=ByteArrayOutputStream()
        DataOutputStream(bytes).use{out->
            for(value in listOf(event.key.eventId.toString(),event.key.begin.toString(),event.end.toString(),event.title,event.calendarName)) {
                val encoded=value.toByteArray(Charsets.UTF_8);out.writeInt(encoded.size);out.write(encoded)
            }
        }
        val mac=Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(reference.toByteArray(Charsets.UTF_8),"HmacSHA256"))
        return hex(mac.doFinal(bytes.toByteArray()))
    }
    private fun hex(bytes:ByteArray)=bytes.joinToString(""){"%02x".format(it)}
}
