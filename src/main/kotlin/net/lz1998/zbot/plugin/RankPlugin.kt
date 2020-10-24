package net.lz1998.zbot.plugin

import net.lz1998.pbbot.alias.GroupMessageEvent
import net.lz1998.pbbot.bot.Bot
import net.lz1998.pbbot.bot.BotPlugin
import net.lz1998.zbot.aop.annotations.PrefixFilter
import net.lz1998.zbot.aop.annotations.SwitchFilter
import net.lz1998.zbot.config.ServiceConfig
import org.springframework.stereotype.Component
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.Charset

@Component
@SwitchFilter("rank")
class RankPlugin : BotPlugin() {

    val findPersonUrl: String get() = "http://${ServiceConfig.rank}/getRank/person?wcaid="

    @PrefixFilter([".rank"])
    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        val groupId = event.groupId
        val rawMsg = event.rawMessage.trim()
        val url = "${findPersonUrl}${URLEncoder.encode(rawMsg, Charsets.UTF_8.name())}"
        val retMsg = URL(url).readText(Charset.forName("GBK")).replace("#success", "")
        bot.sendGroupMsg(groupId, retMsg)
        return MESSAGE_BLOCK
    }
}