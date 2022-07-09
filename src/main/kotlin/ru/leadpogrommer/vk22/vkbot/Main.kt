@file:OptIn(ExperimentalCli::class)
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
import kotlinx.cli.*
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

fun getVkApi(): Pair<VkApiClient, UserActor> {
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
    return vk to actor
}


fun main(args: Array<String>){


    val parser = ArgParser("vk22")

    class Task50: Subcommand("50", "Generate trailer for vk group"){
        override fun execute() {
            val (vk, actor) = getVkApi()
            task50(vk, actor)
        }
    }
    class Task40: Subcommand("40", "Calculate comments"){
        val videoId by argument(ArgType.String, description = "Video id in format ownerid_videoid (example: -213724548_456239017)")
        val patterns by argument(ArgType.String, description = "Patterns").vararg()

        override fun execute() {


            val (vk, actor) = getVkApi()
            task40(vk, actor, videoId, patterns)
        }
    }

    class Task30: Subcommand("30", "Check for new live streams"){
        val group by argument(ArgType.String, description = "Group short name or id")
        val timeout by argument(ArgType.Int, description = "Time between updates (in seconds)").optional()

        override fun execute() {
            val (vk, actor) = getVkApi()
            task30(vk, actor, group, timeout ?: 60)
        }

    }

    class Task10:Subcommand("10", "Call creating bot"){
        override fun execute() {
            val (vk, actor) = getVkApi()
            task10(vk, actor, "2a8ce4a7cc98160b1f14d81080136b53c3b346d7112d35a07a30d9e82a5dcba235be58e205538b86beffb");
        }

    }

    parser.subcommands(Task50(), Task40(), Task30(), Task10())
    parser.parse(args)


//    task50(vk, actor)





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