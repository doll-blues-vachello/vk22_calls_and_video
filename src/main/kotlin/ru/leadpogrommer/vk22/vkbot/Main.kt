package ru.leadpogrommer.vk22.vkbot

//import com.sun.org.apache.xalan.internal.xsltc.compiler.Constants.REDIRECT_URI

//import api.longpoll.bots.LongPollBot
//import api.longpoll.bots.exceptions.VkApiException
//import api.longpoll.bots.model.events.messages.MessageNew
//import api.longpoll.bots.model.objects.basic.Message


//
//import com.vk.api.sdk.events.longpoll
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import javafx.application.Platform
import javafx.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.builder.FFmpegBuilder
import tornadofx.*
import java.awt.Color
import java.awt.Font
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URI
import javax.imageio.ImageIO


const val clientId = 8214775
const val secret = "WhdSyfWe2c8rEB8AtQ7M"

var _code = ""
class AuthScreen: View() {
    override val root=webview {
//            com.vk.api.sdk.
//        engine.
        engine.load("https://oauth.vk.com/authorize?client_id=$clientId&redirect_uri=https://oauth.vk.com/blank.html&scope=262367")
        engine.locationProperty().onChange {
            it?:return@onChange
            println(it)
            if(it.contains("oauth.vk.com/blank.html")){
                _code = Regex("code=([a-zA-Z0-9]*)").find(it)!!.groupValues[1]
                Platform.exit()
            }
        }
    }
}

class AuthApp: App(AuthScreen::class){
    override fun start(stage: Stage) {
        super.start(stage)
    }
}

fun getCode(): String{
    launch<AuthApp>()
    println(_code)
    print("After launch")
    return _code
}

data class VideoInfo(val name: String, val image: URI, val views: Int)

fun main(){
    print("Enter group id or short name:")
    val groupName = readLine()!!
    print("Enter maximum number of slides (0 = all):")
    val numSlides = readLine()!!.toInt()
    print("Enter output file name:")
    val filename = readLine()!!

    val transportClient = HttpTransportClient()
    val vk = VkApiClient(transportClient)

    val codeFile = File("code.txt")
    val userId: String
    val token: String
    if(codeFile.exists()){
        val data = codeFile.readText().split(",")
        userId = data[0]
        token = data[1]
    }else{
        val code = getCode()
        val res = vk.oAuth().userAuthorizationCodeFlow(clientId, secret, "https://oauth.vk.com/blank.html", code).execute()
        userId = res.userId.toString()
        token = res.accessToken
        codeFile.writeText("$userId,$token")
    }
//    println("code=$code;")
//    val res = vk.oAuth().userAuthorizationCodeFlow(clientId, secret, "https://oauth.vk.com/blank.html", code).execute()
    val actor = UserActor(userId.toInt(), token)
    val test = vk.groups().getByIdObjectLegacy(actor).groupId(groupName).execute()
    val groupId = test[0].id
    println(test.get(0).id)

    val videos = vk.videos().get(actor).ownerId(-groupId).execute()
//    println(videos)

    val videoInfos = videos.items.map { video->
        val image = video.image.maxByOrNull { img -> img.height }
        VideoInfo(video.title, image?.url ?: URI(""), video.views)

    }.sortedByDescending { it.views }.take(if(numSlides == 0)videos.count else numSlides)
    println(videoInfos)


    val imageDir = File("tmp")
    imageDir.mkdir()
    imageDir.listFiles()?.forEach {
        it.delete()
    }

    runBlocking {
        val client = HttpClient(CIO)
        videoInfos.mapIndexed { i, video ->
            launch(Dispatchers.IO){
                val res = client.get(video.image.toURL())
                val img = ImageIO.read(ByteArrayInputStream(res.bodyAsChannel().toByteArray()))
//                val graphics = img.graphics
//                graphics.font = Font.createFonts(File("font.ttf")).get(0).deriveFont(50.0f)
//                graphics.color = Color.WHITE
//                graphics.drawString(video.name, 10, 10)
//                img.graphics.ren
//                img.r
                println("$i - ${video.name} - views: ${video.views}")
                ImageIO.write(img, "jpg", File("tmp/${i.toString().padStart(3, '0')}.jpg"))
            }
        }.toList().joinAll()
    }

    var srtData = ""
    videoInfos.forEachIndexed{ i, video ->
        val tstart = i*5
        val tend = i*5 + 5
        fun tToS(t: Int) = "${(t/3600).toString().padStart(2, '0')}:${((t/60)%60).toString().padStart(2, '0')}:${(t%60).toString().padStart(2, '0')},000"
        srtData += "${i+1}\n${tToS(tstart)} --> ${tToS(tend)}\n${video.name}\n\n"
    }

    File("tmp/srt.srt").writeText(srtData)

    val ff = FFmpegBuilder()
        .setInput("tmp/%03d.jpg")
        .addExtraArgs("-framerate", "1/5")
        .addOutput(filename)
        .setVideoFilter("scale=1920:1080,subtitles=tmp/srt.srt")
        .setFormat("mp4")
        .setVideoFrameRate(10.0)
        .done()
    FFmpegExecutor().createJob(ff).run()

}
//fun main(){
////    val transportClient = HttpTransportClient()
////    val vk = VkApiClient(transportClient)
//////    LongPoll(vk)
////    val actor = GroupActor(213724548, "2a8ce4a7cc98160b1f14d81080136b53c3b346d7112d35a07a30d9e82a5dcba235be58e205538b86beffb")
//////    val lps = vk.messages().getLongPollServer(actor)
////    val lp = vk.groupsLongPoll().getLongPollServer(actor, 213724548).execute()
////    while (true){
////        val events = LongPoll(vk).getEvents(lp.server, lp.key, lp.ts).execute()
////        events.
////
////    }
//
////    val ms = MessagesHandler
//
////    vk.oAuth().groupAuthorizationCodeFlow(1, "2a8ce4a7cc98160b1f14d81080136b53c3b346d7112d35a07a30d9e82a5dcba235be58e205538b86beffb", "", "")
////    GroupActor
////    v
////  Grou`
////    val lpapi = GroupLongPollApi(vk, GroupActor(213724548, "2a8ce4a7cc98160b1f14d81080136b53c3b346d7112d35a07a30d9e82a5dcba235be58e205538b86beffb" ), 9999)
//    HelloBot().startPolling()
//}
//
//class HelloBot : LongPollBot() {
//    override fun onMessageNew(messageNew: MessageNew) {
//        try {
//            val message: Message = messageNew.message
//            if (message.hasText()) {
//                val response = "Hello! Received your message: " + message.getText()
//                vk.messages.send()
//                    .setPeerId(message.getPeerId())
//                    .setMessage(response)
//                    .execute()
//            }
//        } catch (e: VkApiException) {
//            e.printStackTrace()
//        }
//    }
//
//    override fun getAccessToken(): String {
//        return "2a8ce4a7cc98160b1f14d81080136b53c3b346d7112d35a07a30d9e82a5dcba235be58e205538b86beffb"
//    }
//}