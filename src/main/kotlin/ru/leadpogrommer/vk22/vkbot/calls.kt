package ru.leadpogrommer.vk22.vkbot

import com.google.gson.annotations.SerializedName
import com.vk.api.sdk.client.AbstractQueryBuilder
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor

class CallResponse(){
    @SerializedName("join_link")
    lateinit var joinLink: String

    @SerializedName("call_id")
    lateinit var callId: String
}

class StartCallBuilder(vk: VkApiClient, actor: UserActor): AbstractQueryBuilder<StartCallBuilder, CallResponse>(vk, "messages.startCall", CallResponse::class.java){
    init {
        accessToken(actor.accessToken)
    }

    fun groupId(id: Int): StartCallBuilder{
        return unsafeParam("group_id", id)
    }

    override fun getThis(): StartCallBuilder {
        return this
    }

    override fun essentialKeys(): MutableCollection<String> {
        return mutableListOf("access_token")
    }

}