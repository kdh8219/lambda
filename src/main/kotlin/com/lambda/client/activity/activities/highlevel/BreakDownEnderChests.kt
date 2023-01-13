package com.lambda.client.activity.activities.highlevel

import com.lambda.client.activity.Activity
import com.lambda.client.activity.activities.interaction.BreakBlock
import com.lambda.client.activity.activities.inventory.AcquireItemInActiveHand
import com.lambda.client.activity.activities.storage.PlaceContainer
import com.lambda.client.activity.activities.storage.StoreItemToShulkerBox
import com.lambda.client.activity.activities.types.RepeatingActivity
import com.lambda.client.event.SafeClientEvent
import com.lambda.client.module.modules.player.InventoryManager
import com.lambda.client.util.items.allSlots
import com.lambda.client.util.items.countItem
import com.lambda.client.util.items.item
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Blocks
import net.minecraft.init.Enchantments
import net.minecraft.init.Items

class BreakDownEnderChests(
    override val maximumRepeats: Int = 0,
    override var repeated: Int = 0
) : RepeatingActivity, Activity() {
    override fun SafeClientEvent.onInitialize() {
        val freeSlots = player.allSlots.filter { slot ->
            InventoryManager.ejectList.contains(slot.stack.item.registryName.toString()) || slot.stack.isEmpty
        }

        if (freeSlots.isEmpty()) {
            if (player.allSlots.countItem(Blocks.OBSIDIAN.item) > 0) {
                addSubActivities(
                    StoreItemToShulkerBox(Blocks.OBSIDIAN.item)
                )
                return
            }

            failedWith(NoSpaceLeftInInventoryException())
            return
        }

        addSubActivities(
            PlaceContainer(Blocks.ENDER_CHEST.defaultState)
        )
    }

    override fun SafeClientEvent.onChildSuccess(childActivity: Activity) {
        if (childActivity !is PlaceContainer) return

        addSubActivities(
            AcquireItemInActiveHand(
                Items.DIAMOND_PICKAXE,
                predicateItem = {
                    EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, it) == 0
                }
            ),
            BreakBlock(
                childActivity.containerPos,
                collectDrops = true,
                minCollectAmount = 64
            )
        )
    }

    class NoSpaceLeftInInventoryException : Exception("No space left in inventory")
}