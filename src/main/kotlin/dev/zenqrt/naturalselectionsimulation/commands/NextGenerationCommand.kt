package dev.zenqrt.naturalselectionsimulation.commands

import dev.zenqrt.naturalselectionsimulation.environments
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import round

class NextGenerationCommand : Command("nextgeneration") {

    init {
        setDefaultExecutor { sender, _ ->
            sender.sendMessage(Component.text("Invalid command arguments: /nextgeneration <name>"))
        }

        val nameArgument = ArgumentType.String("name")

        addSyntax({ sender, context ->
            val name = context[nameArgument]
            environments[name]?.let {
                val statistics = it.nextGeneration()
                sender.sendMessage("Started next generation in $name")
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(
                    """
                        &eGeneration &6${statistics.generation} &eStatistics:
                          &8- &7Test Subject Amount: &a${statistics.testSubjectsAmount}
                          &8- &7Average Speed: &a${statistics.averageTraits.speed.round(3)}
                          &8- &7Average Size: &a${statistics.averageTraits.size.round(3)}
                          &8- &7Average Sense: &a${statistics.averageTraits.sense.round(3)}
                    """.trimIndent()))
            }

        }, nameArgument)
    }

}