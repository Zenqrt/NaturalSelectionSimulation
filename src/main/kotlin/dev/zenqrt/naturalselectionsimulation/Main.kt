package dev.zenqrt.naturalselectionsimulation

import dev.zenqrt.naturalselectionsimulation.commands.CreateEnvironmentCommand
import dev.zenqrt.naturalselectionsimulation.commands.NextGenerationCommand
import dev.zenqrt.naturalselectionsimulation.commands.SummonTestSubjectCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.GameMode
import net.minestom.server.event.player.PlayerCommandEvent
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.block.Block

lateinit var instanceContainer: InstanceContainer
val environments = mutableMapOf<String, Environment>()

fun main() {
    val server = MinecraftServer.init()

    instanceContainer = MinecraftServer.getInstanceManager().createInstanceContainer()
    instanceContainer.setGenerator { }

    val globalEventHandler = MinecraftServer.getGlobalEventHandler()

    globalEventHandler.addListener(PlayerLoginEvent::class.java) {
        it.setSpawningInstance(instanceContainer)
        it.player.respawnPoint = Pos(0.0, 42.0, 0.0)
        it.player.gameMode = GameMode.SPECTATOR
    }
    globalEventHandler.addListener(PlayerCommandEvent::class.java) { event ->
        MinecraftServer.getConnectionManager().onlinePlayers.forEach { it.sendMessage(Component.text("${event.player.username} ran the command: /${event.command}", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC)) }
    }

    val commandManager = MinecraftServer.getCommandManager()
    commandManager.register(CreateEnvironmentCommand())
    commandManager.register(NextGenerationCommand())

    MinecraftServer.getTeamManager().teams += setOf(Teams.GETTING_FOOD, Teams.CAN_SURVIVE, Teams.CAN_MULTIPLY)

    server.start("0.0.0.0", 25565)
}