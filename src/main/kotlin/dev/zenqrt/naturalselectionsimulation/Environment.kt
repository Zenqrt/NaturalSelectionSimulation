package dev.zenqrt.naturalselectionsimulation

import dev.zenqrt.naturalselectionsimulation.entity.AppleEntity
import dev.zenqrt.naturalselectionsimulation.entity.TestSubjectEntity
import dev.zenqrt.naturalselectionsimulation.entity.Traits
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import java.util.concurrent.ThreadLocalRandom

class Environment(private val environmentSettings: EnvironmentSettings, private val instance: Instance, private val originPosition: Pos) {

    private val testSubjects = mutableListOf<TestSubjectEntity>()
    private val availableHomePositions = mutableListOf<Pos>()
    private val homePositions = mutableListOf<Pos>()
    private val platformBlocks = mutableListOf<Pos>()
    private var generation = 0
    private var foodCount = 200
    val food = mutableListOf<AppleEntity>()

    fun create() {
        createPlatform()
        createHomePositions()
    }

    private fun createPlatform() {
        val platformSettings = environmentSettings.platformSettings

        for (x in -platformSettings.dimensionX..platformSettings.dimensionX)
            for (z in -platformSettings.dimensionZ..platformSettings.dimensionZ)
                originPosition.add(x.toDouble(), -1.0, z.toDouble()).also {
                    instance.setBlock(it, platformSettings.block)
                    platformBlocks += it
                }
    }

    private fun createHomePositions() {
        val platformSettings = environmentSettings.platformSettings

        for (x in -platformSettings.dimensionX..platformSettings.dimensionX) {
            homePositions += originPosition.add(x.toDouble(), 0.0, -platformSettings.dimensionX.toDouble())
            homePositions += originPosition.add(x.toDouble(), 0.0, platformSettings.dimensionX.toDouble())
        }

        for (z in -platformSettings.dimensionZ..platformSettings.dimensionZ) {
            homePositions += originPosition.add(-platformSettings.dimensionZ.toDouble(), 0.0, z.toDouble())
            homePositions += originPosition.add(platformSettings.dimensionZ.toDouble(), 0.0, z.toDouble())
        }
    }

    fun destroy() {
        platformBlocks.forEach { instance.setBlock(it, Block.AIR) }
        platformBlocks.clear()
    }

    fun nextGeneration(): GenerationStatistics {
        availableHomePositions.clear()
        availableHomePositions += homePositions

        clearFood()
        repeat(foodCount--) { spawnFood() }

        if(generation++ == 0) {
            val defaultTraits = environmentSettings.defaultTraits
            repeat(environmentSettings.testSubjectAmount) { summonTestSubject(defaultTraits) }
            return GenerationStatistics(generation, testSubjects.size, defaultTraits)
        }

        testSubjects.forEach { it.remove() }
        summonSurvivors()

        return GenerationStatistics(generation, testSubjects.size, Traits(
            speed = findAverageTrait { it.speed },
            size = findAverageTrait { it.size },
            sense = findAverageTrait { it.sense }
        ))
    }

    private fun summonSurvivors() {
        val testSubjectsSurvived = testSubjects.filter { it.canSurvive() }
        testSubjects.clear()

        testSubjectsSurvived.forEach {
            summonTestSubject(it.traits)

            if(it.canMultiply())
                summonTestSubject(
                    it.traits.copy(
                        speed = attemptMutation(it.traits.speed),
                        size = attemptMutation(it.traits.size),
                        sense = attemptMutation(it.traits.sense)
                    )
                )
        }
    }

    private fun clearFood() {
        food.forEach { it.remove() }
        food.clear()
    }

    private fun spawnFood() {
        val platformSettings = environmentSettings.platformSettings
        val appleEntity = AppleEntity()
        appleEntity.isGlowing = true
        appleEntity.setInstance(instance, originPosition.add(
            ThreadLocalRandom.current().nextDouble((-platformSettings.dimensionX + 2).toDouble(), (platformSettings.dimensionX - 2).toDouble()),
            0.0,
            ThreadLocalRandom.current().nextDouble((-platformSettings.dimensionX + 2).toDouble(), (platformSettings.dimensionX - 2).toDouble()),
        ))
        food += appleEntity
    }

    private fun findAverageTrait(valueFunction: (Traits) -> Double): Double = testSubjects.sumOf { valueFunction(it.traits) } / testSubjects.size

    private fun attemptMutation(value: Double): Double = if (ThreadLocalRandom.current().nextFloat() < 0.25) value + ThreadLocalRandom.current().nextDouble(-0.25,  0.25) else value

    private fun summonTestSubject(traits: Traits) {
        if(availableHomePositions.isEmpty())
            throw RuntimeException("There are no available home positions")

        val testSubjectEntity = createTestSubject(traits)
        val homePosition = availableHomePositions[ThreadLocalRandom.current().nextInt(availableHomePositions.size)]

        testSubjectEntity.setInstance(instance, homePosition)
        availableHomePositions -= homePosition
        testSubjects += testSubjectEntity
    }

    private fun createTestSubject(traits: Traits): TestSubjectEntity = TestSubjectEntity(this, testSubjects.size, traits, 500.0)

}

data class EnvironmentSettings(val testSubjectAmount: Int, val defaultTraits: Traits, val platformSettings: PlatformSettings)

data class PlatformSettings(val dimensionX: Int, val dimensionZ: Int, val block: Block)