package ru.leadpogrommer.vk22.vkbot

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor

fun task40(vk: VkApiClient, actor: UserActor, videoId: String, patterns: List<String>){
    println(videoId)
    println(patterns)


    val comments = mutableListOf<String>()
    val video = vk.videos().get(actor).videos(videoId).execute().items[0]
    val vid = video.id
    val oid = video.ownerId



    while (true){
        val commentsBunch = vk.videos().getComments(actor, vid).ownerId(oid).offset(comments.size).execute()
        if(commentsBunch.items.size == 0)break;
        comments.addAll(commentsBunch.items.map { it.text }.toList())
    }

//    println(comments)
    println("----------------")
    patterns.forEach {pattern ->
        val re = Regex(pattern)
        val count = comments.count { re.matchEntire(it) != null }
        println("Pattern: $pattern; count: $count")
    }

//    println(video.items[0].ownerId)
//    val commentsPart = vk.videos().getComments(actor, vid).ownerId(oid).execute()
//    comments

}