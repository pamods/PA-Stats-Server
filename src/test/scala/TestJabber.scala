
import java.net.URL
import java.net.HttpURLConnection
import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern
import java.net.URLEncoder
import java.io.DataOutputStream
import scala.collection.JavaConverters._
import org.jivesoftware.smack.ChatManager
import org.jivesoftware.smack.XMPPBOSHConnection
import org.jivesoftware.smack.PacketListener
import org.jivesoftware.smack.MessageListener
import org.jivesoftware.smack.Chat
import org.jivesoftware.smack.packet.Packet
import org.jivesoftware.smack.packet.Message

    // it seems the ssl cert uber uses is not part of the jdk by default. It needs to be imported by hand via the keytool to make this code work!
object TestJabber extends App {
  val loginForm = "https://uberent.com/GC/Authenticate"

  def getSessionToken(user: String, pass: String) = {
    val url = new URL(loginForm)
    val urlCon = url.openConnection().asInstanceOf[HttpURLConnection]
    val postData = s"""{TitleId: 4, AuthMethod: "UberCredentials", UberName: "$user", Password: "$pass"}"""
    urlCon.addRequestProperty("Content-Type", "application/json")
    urlCon.setRequestMethod("POST")
    urlCon.setDoOutput(true)
    val wr = new DataOutputStream(urlCon.getOutputStream())
    wr.writeBytes(postData)
    wr.flush()
    wr.close()
    val resp = getResponse(urlCon)
    val pattern = "\"?SessionTicket\"?\\s*:\\s*\"(.*?)\""
    getGroup(resp, pattern)
  }
  
  def getUberIdForUberName(session: String, uberName: String) = {
    
  }
  
  private def getResponse(urlCon: HttpURLConnection) = {
    val in = urlCon.getInputStream()
    val out = new ByteArrayOutputStream
    IOUtils.copy(in, out)
    new String(out.toByteArray())
  }

  private def getGroup(str: String, regex: String, grpIndex: Int = 1) = {
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(str)
    matcher.find()
    matcher.group(grpIndex)
  }

  val session = getSessionToken("", "")
  
  val con = new XMPPBOSHConnection(false, "xmpp.uberent.com", 5280, "/http-bind", "xmpp.uberent.com")
  println("created con object")
  con.connect()
  println("connect executed")
  con.login("15535003602013880865", session)
  println("authenticated")

  con.addPacketListener(new PacketListener {
    def processPacket(packet: Packet) = {
      println(packet);
    }
  }, null)

  val chatManager = ChatManager.getInstanceFor(con)

  val chat = chatManager.createChat("8268546284496977439@xmpp.uberent.com/PA", new MessageListener {
    def processMessage(chat: Chat, message: Message) = {
      println("received: " + message)
    }
  })

  chat.sendMessage("test 123!!!")

}
