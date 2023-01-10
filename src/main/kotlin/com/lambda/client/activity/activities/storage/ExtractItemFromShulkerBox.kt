package com.lambda.client.activity.activities.storage

import com.lambda.client.activity.Activity
import com.lambda.client.activity.activities.highlevel.BreakDownEnderChests
import com.lambda.client.activity.activities.interaction.BreakBlockWithTool
import com.lambda.client.activity.activities.interaction.CloseContainer
import com.lambda.client.activity.activities.interaction.OpenContainer
import com.lambda.client.activity.activities.interaction.PlaceBlock
import com.lambda.client.activity.activities.inventory.AcquireItemInActiveHand
import com.lambda.client.activity.activities.inventory.SwapOrSwitchToSlot
import com.lambda.client.activity.activities.utils.getShulkerInventory
import com.lambda.client.event.SafeClientEvent
import com.lambda.client.util.items.allSlots
import com.lambda.client.util.items.block
import com.lambda.client.util.items.item
import net.minecraft.init.Blocks
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class ExtractItemFromShulkerBox(
    private val item: Item,
    private val amount: Int = 0, // 0 = all
    private val predicateItem: (ItemStack) -> Boolean = { true },
    private val predicateSlot: (ItemStack) -> Boolean = { true }
) : Activity() {
    override fun SafeClientEvent.onInitialize() {
//        if (player.inventorySlots.item)

        val candidates = mutableMapOf<Slot, Int>()

        player.allSlots.forEach { slot ->
            getShulkerInventory(slot.stack)?.let { inventory ->
                val count = inventory.count { it.item == item && predicateItem(it) }

                if (count > 0) candidates[slot] = count
            }
        }

        if (candidates.isEmpty()) {
            failedWith(NoShulkerBoxFoundExtractException(item))
//            if (item != Blocks.OBSIDIAN.item) {
//                failedWith(NoShulkerBoxFoundExtractException(item))
//                return
//            }

//            if (owner?.owner !is PlaceBlock) return
//
//            addSubActivities(
//                BreakDownEnderChests(maximumRepeats = 64),
//                AcquireItemInActiveHand(Blocks.OBSIDIAN.item)
//            )
            return
        }

        candidates.minBy { it.value }.key.let { slot ->
            addSubActivities(
                SwapOrSwitchToSlot(slot),
                PlaceContainer(slot.stack.item.block.defaultState)
            )
        }
    }

    override fun SafeClientEvent.onChildSuccess(childActivity: Activity) {
        if (childActivity !is PlaceContainer) return

        addSubActivities(
            OpenContainer(childActivity.containerPos),
            PullItemsFromContainer(item, amount, predicateItem),
            CloseContainer(),
            BreakBlockWithTool(childActivity.containerPos, collectDrops = true),
            AcquireItemInActiveHand(item, predicateItem, predicateSlot)
        )
    }

    class NoShulkerBoxFoundExtractException(item: Item) : Exception("No shulker box was found containing ${item.registryName}")
}