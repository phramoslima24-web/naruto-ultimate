package com.narutoto.ultimate.command;

import com.narutoto.ultimate.data.PlayerProfile;
import com.narutoto.ultimate.integration.AddonDetector;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.Map;

public final class UltimateCommands {
    private UltimateCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ultimate")
            .then(Commands.literal("profile")
                .executes(ctx -> showProfile(ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                    .requires(source -> source.hasPermission(2))
                    .executes(ctx -> showProfile(EntityArgument.getPlayer(ctx, "player")))))
            .then(Commands.literal("addons")
                .executes(ctx -> showAddons(ctx.getSource())))
            .then(Commands.literal("setrank")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("rank", StringArgumentType.word())
                        .executes(ctx -> setField(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), "Rank", StringArgumentType.getString(ctx, "rank"))))))
            .then(Commands.literal("setvillage")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("village", StringArgumentType.word())
                        .executes(ctx -> setField(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), "Village", StringArgumentType.getString(ctx, "village"))))))
            .then(Commands.literal("setclan")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("clan", StringArgumentType.word())
                        .executes(ctx -> setField(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), "Clan", StringArgumentType.getString(ctx, "clan"))))))
            .then(Commands.literal("addxp")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1000000))
                        .executes(ctx -> {
                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                            PlayerProfile.addExperience(player, amount);
                            ctx.getSource().sendSuccess(() -> Component.literal("Adicionado " + amount + " XP para " + player.getGameProfile().getName() + "."), true);
                            return 1;
                        }))))
            .then(Commands.literal("addrep")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", IntegerArgumentType.integer(-100000, 100000))
                        .executes(ctx -> {
                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                            PlayerProfile.addReputation(player, amount);
                            ctx.getSource().sendSuccess(() -> Component.literal("Reputação de " + player.getGameProfile().getName() + ": " + PlayerProfile.getInt(player, "Reputation")), true);
                            return 1;
                        })))));
    }

    private static int showProfile(ServerPlayer player) {
        player.sendSystemMessage(Component.literal(PlayerProfile.summary(player)));
        return 1;
    }

    private static int setField(CommandSourceStack source, ServerPlayer player, String field, String value) {
        PlayerProfile.setString(player, field, value.toUpperCase());
        source.sendSuccess(() -> Component.literal(field + " de " + player.getGameProfile().getName() + " definido para " + value + "."), true);
        return 1;
    }

    private static int showAddons(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§6=== Addons detectados ==="), false);
        for (Map.Entry<String, Boolean> entry : AddonDetector.status().entrySet()) {
            String status = entry.getValue() ? "§aATIVO" : "§7não instalado";
            source.sendSuccess(() -> Component.literal("§f" + entry.getKey() + ": " + status), false);
        }
        return 1;
    }
}
