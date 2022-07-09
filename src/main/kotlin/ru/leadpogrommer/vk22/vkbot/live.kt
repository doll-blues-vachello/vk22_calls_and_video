package ru.leadpogrommer.vk22.vkbot

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.objects.video.VideoFull
import com.vk.api.sdk.objects.video.VideoLiveStatus


fun task30(vk: VkApiClient, actor: UserActor, groupId: String, timeout: Int){
    val group = vk.groups().getByIdObjectLegacy(actor).groupId(groupId).execute()[0]
    println(group.name)

    fun getVideos(): List<VideoFull>{
        val videos = mutableListOf<VideoFull>()
        while(true){
            val someVideos = vk.videos().get(actor).ownerId(-group.id).offset(videos.count()).execute().items
            if(someVideos.isEmpty())break
            videos.addAll(someVideos)
        }
        return videos
    }

    val alreadyReported = mutableSetOf<Int>()

    while (true){
        val videos = getVideos().filter { it.isLive }
        videos.forEach {
            if(it.liveStatus != VideoLiveStatus.FAILED && it.liveStatus != VideoLiveStatus.FINISHED && !alreadyReported.contains(it.id)){
                alreadyReported.add(it.id)
                println("New stream: ${it.title} (${it.player})")
            }
        }
        Thread.sleep(timeout*1000L)
    }

}