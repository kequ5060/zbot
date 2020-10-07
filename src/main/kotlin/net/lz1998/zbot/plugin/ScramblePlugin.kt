@file:Suppress("unused")

package net.lz1998.zbot.plugin

import net.lz1998.pbbot.alias.GroupMessageEvent
import net.lz1998.pbbot.bot.Bot
import net.lz1998.pbbot.bot.BotPlugin
import net.lz1998.pbbot.utils.Msg
import net.lz1998.zbot.aop.annotations.PrefixFilter
import net.lz1998.zbot.aop.annotations.SwitchFilter
import net.lz1998.zbot.config.ServiceConfig
import net.lz1998.zbot.enums.SlidysimEnum
import net.lz1998.zbot.enums.TNoodleEnum
import net.lz1998.zbot.service.ScrambleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.net.URL
import java.net.URLEncoder

@Component
@SwitchFilter("打乱")
class ScramblePlugin : BotPlugin() {

    @Autowired
    lateinit var scrambleService: ScrambleService

    @Autowired
    lateinit var restTemplate: RestTemplate

    fun getScramble(type: String): String? {
        var scramble: String = URL("http://${ServiceConfig.tnoodle}/scramble/.txt?=$type").readText()
        scramble = scramble.replace("\r", "")
        if (scramble.endsWith("\n")) {
            scramble = scramble.substring(0, scramble.length - 1)
        }
        if ("minx" == type) {
            scramble = scramble.replace("U' ", "U'\n")
            scramble = scramble.replace("U ", "U\n")
        }
        return scramble
    }


    @PrefixFilter(".")
    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        val groupId = event.groupId
        val rawMsg = event.rawMessage

        for (puzzle in TNoodleEnum.values()) {
            if (rawMsg == puzzle.instruction) {
                return try {
                    val scramble = getScramble(puzzle.shortName)
                    val imgUrl = "http://${ServiceConfig.tnoodle}/view/${puzzle.shortName}.png?scramble=" + URLEncoder.encode(scramble, "utf-8")
                    val retMsg = Msg.builder().text("${puzzle.showName}\n${scramble}\n").image(imgUrl)
                    bot.sendGroupMsg(groupId, retMsg, false)
                    MESSAGE_BLOCK
                } catch (e: IOException) {
                    e.printStackTrace()
                    val retMsg = "获取打乱失败"
                    bot.sendGroupMsg(groupId, retMsg, false)
                    MESSAGE_BLOCK
                }
            }
        }

        for (puzzle in SlidysimEnum.values()) {
            if (rawMsg == puzzle.instruction) {
                return try {
                    val scramble = scrambleService.getScrambleSlidysim(puzzle.n)
                    val retMsg = "${puzzle.showName}\n${scramble}"
                    bot.sendGroupMsg(groupId, retMsg, false)
                    MESSAGE_BLOCK
                } catch (e: Exception) {
                    e.printStackTrace()
                    val retMsg = "获取打乱失败"
                    bot.sendGroupMsg(groupId, retMsg, false)
                    MESSAGE_BLOCK
                }
            }
        }

        return MESSAGE_IGNORE
    }
}