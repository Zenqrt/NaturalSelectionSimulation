package dev.zenqrt.naturalselectionsimulation.commands

import dev.zenqrt.naturalselectionsimulation.Environment
import dev.zenqrt.naturalselectionsimulation.EnvironmentSettings
import dev.zenqrt.naturalselectionsimulation.PlatformSettings
import dev.zenqrt.naturalselectionsimulation.entity.Traits
import dev.zenqrt.naturalselectionsimulation.environments
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.instance.block.Block
import java.time.Duration

class CreateEnvironmentCommand : Command("createenvironment") {

    init {
       setDefaultExecutor { sender, _ ->
           sender.sendMessage(Component.text("Invalid command arguments: /createenvironment <name> <test_subject_amount> <platform_x> <platform_z> [automatic]"))
       }

        val nameArgument = ArgumentType.String("name")
        val testSubjectAmountArgument = ArgumentType.Integer("test_subject_amount")
        val platformXArgument = ArgumentType.Integer("platform_x")
        val platformZArgument = ArgumentType.Integer("platform_z")
        val automaticArgument = ArgumentType.Boolean("automatic")

        addSyntax({ sender, context ->
            if(sender !is Player)
                return@addSyntax

            val environment = Environment(EnvironmentSettings(context[testSubjectAmountArgument], Traits(0.5, 1.0, 1.0), PlatformSettings(context[platformXArgument], context.get(platformZArgument), Block.STONE)), sender.instance!!, sender.position)
            environment.create()

            val name = context[nameArgument]
            environments += name to environment
            sender.sendMessage("Created Environment '${context[nameArgument]}'")

            if(context.getOrDefault(automaticArgument, false))
                MinecraftServer.getSchedulerManager().buildTask { MinecraftServer.getCommandManager().execute(sender, "nextgeneration $name") }
                    .delay(Duration.ofSeconds(15))
                    .repeat(Duration.ofSeconds(30))
                    .schedule()

        }, nameArgument, testSubjectAmountArgument, platformXArgument, platformZArgument, automaticArgument)
    }

}