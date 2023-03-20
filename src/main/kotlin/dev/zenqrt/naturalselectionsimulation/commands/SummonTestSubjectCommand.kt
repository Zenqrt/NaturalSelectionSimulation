package dev.zenqrt.naturalselectionsimulation.commands

import dev.zenqrt.naturalselectionsimulation.entity.TestSubjectEntity
import dev.zenqrt.naturalselectionsimulation.entity.Traits
import dev.zenqrt.naturalselectionsimulation.instanceContainer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.coordinate.Pos

class SummonTestSubjectCommand : Command("summontestsubject") {

    init {
        setDefaultExecutor { sender, _ ->
            sender.sendMessage(Component.text("Invalid command arguments: /summontestsubject <speed> <size> <sense> <energy>", NamedTextColor.RED))
        }

        val speedArgument = ArgumentType.Double("speed")
        val sizeArgument = ArgumentType.Double("size")
        val senseArgument = ArgumentType.Double("sense")
        val energyArgument = ArgumentType.Double("energy")

//        addSyntax({ sender, context ->
//            val entity = TestSubjectEntity(environment, Traits(context[speedArgument], context[sizeArgument], context[senseArgument]), context[energyArgument])
//            entity.setInstance(instanceContainer, Pos(0.0, 40.0, 0.0))
//            sender.sendMessage("Summoned Test Subject.")
//        }, speedArgument, sizeArgument, senseArgument, energyArgument)

    }

}