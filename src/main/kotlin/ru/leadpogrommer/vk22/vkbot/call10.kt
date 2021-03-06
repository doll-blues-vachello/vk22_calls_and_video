package ru.leadpogrommer.vk22.vkbot

import api.longpoll.bots.LongPollBot
import api.longpoll.bots.exceptions.VkApiException
import api.longpoll.bots.model.events.messages.MessageNew
import api.longpoll.bots.model.objects.basic.Message
import com.google.gson.annotations.SerializedName
import com.vk.api.sdk.client.AbstractQueryBuilder
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.docs.responses.SearchResponse



class HelloBot(val myVk: VkApiClient, val actor: UserActor, val token: String) : LongPollBot() {
    override fun onMessageNew(messageNew: MessageNew) {
        try {
            val message: Message = messageNew.message
            if (message.hasText()) {
                if(message.text == "Звонок"){
                    val tst = StartCallBuilder(myVk, actor).execute()
                    vk.messages.send().setPeerId(message.peerId)
                        .setMessage(tst.joinLink)
                        .execute()
                }
            }
        } catch (e: VkApiException) {
            e.printStackTrace()
        }
    }

    override fun getAccessToken(): String {
        return token
    }
}

// token: group token
fun task10(vk: VkApiClient, actor: UserActor, token: String){
    val bot = HelloBot(vk, actor, token)
    bot.startPolling()
}