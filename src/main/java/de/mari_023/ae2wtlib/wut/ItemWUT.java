package de.mari_023.ae2wtlib.wut;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import de.mari_023.ae2wtlib.TextConstants;
import de.mari_023.ae2wtlib.terminal.ItemWT;
import de.mari_023.ae2wtlib.wct.magnet_card.MagnetHandler;

import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import appeng.menu.locator.MenuLocator;

public class ItemWUT extends ItemWT {
    @Override
    public InteractionResultHolder<ItemStack> use(final Level w, final Player player, final InteractionHand hand) {
        if (WUTHandler.getCurrentTerminal(player.getItemInHand(hand)).equals("")) {
            if (!w.isClientSide())
                player.sendSystemMessage(TextConstants.TERMINAL_EMPTY);
            return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(hand));
        }
        /*
         * if(player.isShiftKeyDown()) { if(w.isClientSide()) Minecraft.getInstance().setScreen(new
         * WUTSelectScreen(player.getItemInHand(hand))); return new InteractionResultHolder<>(InteractionResult.SUCCESS,
         * player.getItemInHand(hand)); } else
         */
        return super.use(w, player, hand);
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 800d
                * (countInstalledTerminals(stack) + 1 + getUpgrades(stack).getInstalledUpgrades(AEItems.ENERGY_CARD));
    }

    @Override
    public boolean open(final Player player, ItemStack stack, final MenuLocator locator, boolean returningFromSubmenu) {
        return WUTHandler.open(player, locator, returningFromSubmenu);
    }

    @Override
    public MenuType<?> getMenuType(ItemStack stack) {
        return WUTHandler.wirelessTerminals.get(WUTHandler.getCurrentTerminal(stack)).menuType();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level world, final List<Component> lines,
            final TooltipFlag advancedTooltips) {
        lines.add(TextConstants.UNIVERSAL);
        for (var terminal : WUTHandler.wirelessTerminals.entrySet()) {
            if (WUTHandler.hasTerminal(stack, terminal.getKey()))
                lines.add(terminal.getValue().terminalName());
        }
        super.appendHoverText(stack, world, lines, advancedTooltips);
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack stack) {
        return UpgradeInventories.forItem(stack, countInstalledTerminals(stack) * 2, this::onUpgradesChanged);
    }

    public void onUpgradesChanged(ItemStack stack, IUpgradeInventory upgrades) {
        setAEMaxPowerMultiplier(stack,
                countInstalledTerminals(stack) + Upgrades.getEnergyCardMultiplier(upgrades));
    }

    public int countInstalledTerminals(ItemStack stack) {
        int terminals = 0;
        for (String s : WUTHandler.terminalNames) {
            if (WUTHandler.hasTerminal(stack, s))
                terminals++;
        }
        return terminals;
    }

    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
        if (level.isClientSide())
            return;
        if (!(entity instanceof ServerPlayer player))
            return;
        if (!WUTHandler.hasTerminal(itemStack, "crafting"))
            return;
        MagnetHandler.handle(player, itemStack);
    }
}
