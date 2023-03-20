package dev.zenqrt.naturalselectionsimulation

import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.scoreboard.Team

object Teams {
    val GETTING_FOOD = createTeam("getting_food", NamedTextColor.AQUA)
    val CAN_SURVIVE = createTeam("can_survive", NamedTextColor.YELLOW)
    val CAN_MULTIPLY = createTeam("can_multiply", NamedTextColor.GREEN)

    private fun createTeam(name: String, color: NamedTextColor): Team = MinecraftServer.getTeamManager().createBuilder(name).teamColor(color).build()
}