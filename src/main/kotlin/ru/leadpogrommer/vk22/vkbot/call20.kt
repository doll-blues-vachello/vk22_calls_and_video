package ru.leadpogrommer.vk22.vkbot

import api.longpoll.bots.LongPollBot
import api.longpoll.bots.exceptions.VkApiException
import api.longpoll.bots.model.events.messages.MessageNew
import api.longpoll.bots.model.objects.basic.Message
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.objects.users.responses.GetResponse

class CallcenterBot(val myVk: VkApiClient, val actor: UserActor, val token: String, val operators: List<String>) : LongPollBot() {
    val operatorUsers: List<GetResponse>
    val freeOperators = mutableSetOf<Int>()
    val users = mutableListOf<Int>()
    val operatorCalls = mutableMapOf<Int, String>()
    init {
        operatorUsers = operators.map { myVk.users().get(actor).userIds(it).execute()[0] }
        freeOperators.addAll(operatorUsers.map{it.id})

        operatorUsers.forEach {
            val call = StartCallBuilder(myVk, actor).execute()
            operatorCalls[it.id] = call.joinLink

            vk.messages.send()
                .setPeerId(it.id)
                .setMessage("Теперь вы являетесь Оператором\nВаш звонок - ${call.joinLink}")
                .execute()
        }
    }

    val operatorIds = operatorUsers.map { it.id }.toSet()

    override fun onMessageNew(messageNew: MessageNew) {

        try {
            val message: Message = messageNew.message
            fun processAwaitingUsers(){
                while (!users.isEmpty() && !freeOperators.isEmpty()){
                    val user = users.removeFirst()
                    val operator = freeOperators.random()
                    freeOperators.remove(operator)
                    vk.messages.send()
                        .setPeerId(operator)
                        .setMessage("Вам назначен пользователь $user")
                        .execute()
                    vk.messages.send()
                        .setPeerId(user)
                        .setMessage("Вы дождались - ${operatorCalls[operator]}")
                        .execute()
                }
                users.forEachIndexed { i, user ->
                    vk.messages.send()
                        .setPeerId(user)
                        .setMessage("Ваша позиция в очереди - ${i+1}")
                        .execute()
                }
            }

            if (message.hasText()) {
                if(message.peerId in operatorIds){
                    if(message.text.lowercase().contains("свободен")){
                        freeOperators.add(message.peerId)
                        processAwaitingUsers()
                    }
                }else{
                    if(message.peerId !in users){
                        users.add(message.peerId)
                        processAwaitingUsers()
                    }
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
fun task20(vk: VkApiClient, actor: UserActor, token: String, operators: List<String>){
    val bot = CallcenterBot(vk, actor, token, operators)
    bot.startPolling()
}