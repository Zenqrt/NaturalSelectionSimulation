package dev.zenqrt.naturalselectionsimulation.entity

import dev.zenqrt.naturalselectionsimulation.Environment
import dev.zenqrt.naturalselectionsimulation.Teams
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.attribute.Attribute
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntityCreature
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.ai.EntityAIGroupBuilder
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.goal.FollowTargetGoal
import net.minestom.server.entity.ai.target.ClosestEntityTarget
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import java.time.Duration
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow

class TestSubjectEntity(private val environment: Environment, private val subjectNumber: Int, val traits: Traits, var energy: Double) : EntityCreature(EntityType.VILLAGER) {

    var foodObtained = 0
    var hasFood = false
    lateinit var homePosition: Pos

    init {
        addAIGroup(EntityAIGroupBuilder()
            .addGoalSelector(ReturnHomeGoal(this))
            .addGoalSelector(SearchForFoodGoal(this))
            .addGoalSelector(FollowToFoodGoal(this))
            .addTargetSelector(ClosestEntityTarget(this, 2 * traits.sense) { it is AppleEntity })
            .build())

        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = traits.speed.toFloat()
        isGlowing = true
        isCustomNameVisible = true
        team = Teams.GETTING_FOOD
    }

    override fun aiTick(time: Long) {
        if(energy <= 0) {
            this.aiGroups.clear()
            this.navigator.setPathTo(null)
            return
        }

        super.aiTick(time)
        useEnergy()

        if(!(hasFood))
            environment.food.firstOrNull { it.getDistance(this) <= traits.size && !it.isRemoved }?.also { consumeFood(it) }

        customName = Component.text("Subject ").append(Component.text("#$subjectNumber", NamedTextColor.RED)).append(Component.text(" ${energy.toInt()} ⚡", NamedTextColor.YELLOW))
    }

    private fun consumeFood(appleEntity: AppleEntity) {
        appleEntity.remove()
        hasFood = true
        this.itemInMainHand = ItemStack.of(Material.APPLE)
    }

    override fun spawn() {
        super.spawn()

        homePosition = this.position
    }

    private fun useEnergy() {
        energy -= traits.size.pow(3) * traits.speed.pow(2) + traits.sense
    }

    fun canSurvive(): Boolean = foodObtained >= 1
    fun canMultiply(): Boolean = foodObtained >= 2

}

data class Traits(val speed: Double, val size: Double, val sense: Double)

class FollowToFoodGoal(private val testSubjectEntity: TestSubjectEntity) : FollowTargetGoal(testSubjectEntity, Duration.ofSeconds(1)) {

    override fun shouldStart(): Boolean = !(testSubjectEntity.canMultiply() || testSubjectEntity.hasFood) && super.shouldStart()
    override fun shouldEnd(): Boolean = testSubjectEntity.hasFood || super.shouldEnd()

}

class SearchForFoodGoal(private val testSubjectEntity: TestSubjectEntity) : GoalSelector(testSubjectEntity) {

    private var closePositions = listOf<Vec>()

    init {
        closePositions += getNearbyBlocks(10)
    }

    override fun shouldStart(): Boolean = !(testSubjectEntity.canMultiply() || testSubjectEntity.hasFood) && (entityCreature.navigator.pathPosition == null || entityCreature.position.sameBlock(entityCreature.navigator.pathPosition!!))

    override fun start() {
        var remainingAttempt: Int = closePositions.size
        while (remainingAttempt-- > 0) {
            val index: Int = ThreadLocalRandom.current().nextInt(closePositions.size)
            val position: Vec = closePositions[index]
            val target = entityCreature.position.add(position)
            val result = entityCreature.navigator.setPathTo(target)

            if (result)
                break
        }
    }

    override fun tick(time: Long) {}
    override fun shouldEnd(): Boolean = true
    override fun end() {}

    private fun getNearbyBlocks(radius: Int): List<Vec> {
        val blocks: MutableList<Vec> = ArrayList()
        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    blocks.add(Vec(x.toDouble(), y.toDouble(), z.toDouble()))
                }
            }
        }
        return blocks
    }
}

class ReturnHomeGoal(private val testSubjectEntity: TestSubjectEntity) : GoalSelector(testSubjectEntity) {

    override fun shouldStart(): Boolean = testSubjectEntity.hasFood

    override fun start() {
        testSubjectEntity.navigator.setPathTo(testSubjectEntity.homePosition)
    }

    override fun tick(time: Long) {
        if(testSubjectEntity.navigator.pathPosition != testSubjectEntity.homePosition)
            testSubjectEntity.navigator.setPathTo(testSubjectEntity.homePosition)
    }

    override fun shouldEnd(): Boolean = testSubjectEntity.canMultiply() || testSubjectEntity.homePosition.sameBlock(testSubjectEntity.position)

    override fun end() {
        testSubjectEntity.foodObtained++
        testSubjectEntity.hasFood = false
        testSubjectEntity.itemInMainHand = ItemStack.AIR

        testSubjectEntity.team = if (testSubjectEntity.canMultiply()) Teams.CAN_MULTIPLY else Teams.CAN_SURVIVE
    }
}