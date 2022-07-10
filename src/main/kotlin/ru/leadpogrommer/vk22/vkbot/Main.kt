@file:OptIn(ExperimentalCli::class)
package ru.leadpogrommer.vk22.vkbot


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

// TODO: operators must open group dialogue
//const val clientId = 8214775
//const val secret = "WhdSyfWe2c8rEB8AtQ7M"

var _code = ""
class AuthScreen: View() {


    override val root=webview {
        engine.load("https://oauth.vk.com/authorize?client_id=$clientId&redirect_uri=https://oauth.vk.com/blank.html&scope=327903")
        engine.locationProperty().onChange {
            it?:return@onChange
            println(it)
            if(it.contains("oauth.vk.com/blank.html")){
                _code = Regex("code=([a-zA-Z0-9]*)").find(it)!!.groupValues[1]
                Platform.exit()
            }
        }
    }
    companion object{
        var clientId: Int = 0
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
    return _code
}

fun getVkApi(clientId: Int, secret: String, codestring: String?): Pair<VkApiClient, UserActor> {
    val transportClient = HttpTransportClient()
    val vk = VkApiClient(transportClient)

    val codeFile = File("code.txt")
    val userId: String
    val token: String
    if(codestring != null){
        val data = codestring.split(",")
        userId = data[0]
        token = data[1]
    }
    else if(codeFile.exists()){
        val data = codeFile.readText().split(",")
        userId = data[0]
        token = data[1]
    }else{
        AuthScreen.clientId = clientId
        val code = getCode()
        val res = vk.oAuth().userAuthorizationCodeFlow(clientId, secret, "https://oauth.vk.com/blank.html", code).execute()
        println("Token expires in ${res.expiresIn}")
        userId = res.userId.toString()
        token = res.accessToken
        codeFile.writeText("$userId,$token")
    }
    val actor = UserActor(userId.toInt(), token)
    return vk to actor
}


fun main(args: Array<String>){


    val parser = ArgParser("vk22", strictSubcommandOptionsOrder = true)

    val clientID by parser.option(ArgType.Int, "clientID", description = "Client id of your bot").required()
    val secret by parser.option(ArgType.String, "secret", description = "Secret of your bot").required()
    val codestring by parser.option(ArgType.String, "code", description = "Disable interactive auth. Format: user_id,access_token (like in code.txt)")


    class Task50: Subcommand("50", "Generate trailer for vk group"){
        val groupID by argument(ArgType.String, description = "Group id")
        val numSlides by argument(ArgType.Int, description = "How many slides to create (0 for all)")
        val filename by argument(ArgType.String, description = "Output filename")

        override fun execute() {
            val (vk, actor) = getVkApi(clientID, secret, codestring)
            task50(vk, actor, groupID, numSlides, filename)
        }
    }
    class Task40: Subcommand("40", "Calculate comments"){
        val videoId by argument(ArgType.String, description = "Video id in format ownerid_videoid (example: -213724548_456239017)")
        val patterns by argument(ArgType.String, description = "Patterns").vararg()

        override fun execute() {
            val (vk, actor) = getVkApi(clientID, secret, codestring)
            task40(vk, actor, videoId, patterns)
        }
    }

    class Task30: Subcommand("30", "Check for new live streams"){
        val group by argument(ArgType.String, description = "Group short name or id")
        val timeout by argument(ArgType.Int, description = "Time between updates (in seconds)").optional()

        override fun execute() {
            val (vk, actor) = getVkApi(clientID, secret, codestring)
            task30(vk, actor, group, timeout ?: 60)
        }

    }

    class Task10:Subcommand("10", "Call creating bot"){
        val token by argument(ArgType.String, description = "Group token")

        override fun execute() {
            val (vk, actor) = getVkApi(clientID, secret, codestring)
            task10(vk, actor, token);
        }
    }

    // TODO: test on multiple operators/users
    // TODO: operators to cli
    class Task20:Subcommand("20", "Call Center bot"){
        val token by argument(ArgType.String, description = "Group token")
        val operators by argument(ArgType.String, description = "Operator user ids").vararg()

        override fun execute() {
            val (vk, actor) = getVkApi(clientID, secret, codestring)
            task20(vk, actor, token, operators);
        }

    }
    parser.subcommands(Task10(), Task20(), Task30(), Task40(), Task50())
    parser.parse(args)
}
