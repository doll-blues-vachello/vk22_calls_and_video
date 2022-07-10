package ru.leadpogrommer.vk22.vkbot

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import kotlinx.coroutines.*
import net.bramp.ffmpeg.FFmpegExecutor
import net.bramp.ffmpeg.builder.FFmpegBuilder
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URI
import javax.imageio.ImageIO

data class VideoInfo(val name: String, val image: URI, val views: Int)

@OptIn(DelicateCoroutinesApi::class)
fun task50(vk: VkApiClient, actor: UserActor, groupName: String, numSlides: Int, filename: String){
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
        val ctx = newFixedThreadPoolContext(4, "Image processing")
        videoInfos.mapIndexed { i, video ->
            launch(ctx){
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