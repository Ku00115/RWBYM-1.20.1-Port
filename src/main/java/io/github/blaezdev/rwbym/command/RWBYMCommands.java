package io.github.blaezdev.rwbym.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import io.github.blaezdev.rwbym.capability.aura.IAura;
import io.github.blaezdev.rwbym.capability.semblance.ISemblance;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RWBYMCommands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("rwbym")
                .then(Commands.literal("aura")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("get")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    return withAura(player, aura -> {
                                    send(player, "Aura: " + format(aura.getAmount())
                                            + " / " + format(aura.getMaxAura()));
                                    return 1;
                                    });
                                }))
                        .then(Commands.literal("set")
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            return withAura(player, aura -> {
                                            aura.setAmount(FloatArgumentType.getFloat(context, "amount"));
                                            send(player, "Aura set to "
                                                    + format(aura.getAmount()));
                                            return 1;
                                            });
                                        })))
                        .then(Commands.literal("add")
                                .then(Commands.argument("amount", FloatArgumentType.floatArg())
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            return withAura(player, aura -> {
                                            aura.addAmount(FloatArgumentType.getFloat(context, "amount"));
                                            send(player, "Aura is now "
                                                    + format(aura.getAmount()));
                                            return 1;
                                            });
                                        })))
                        .then(Commands.literal("max")
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            return withAura(player, aura -> {
                                            aura.setMaxAura(FloatArgumentType.getFloat(context, "amount"));
                                            send(player, "Max aura set to "
                                                    + format(aura.getMaxAura()));
                                            return 1;
                                            });
                                        }))))
                .then(Commands.literal("semblance")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("get")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    return withSemblance(player, semblance -> {
                                        send(player, "Semblance: " + semblance.getName()
                                                + " level " + semblance.getLevel()
                                                + (semblance.isActive() ? " active" : " inactive"));
                                        return 1;
                                    });
                                }))
                        .then(Commands.literal("set")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            return withSemblance(player, semblance -> {
                                                semblance.setName(StringArgumentType.getString(context, "name"));
                                                send(player, "Semblance set to " + semblance.getName());
                                                return 1;
                                            });
                                        })))
                        .then(Commands.literal("toggle")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    return withSemblance(player, semblance -> {
                                        semblance.setActive(!semblance.isActive());
                                        send(player, "Semblance "
                                                + (semblance.isActive() ? "active" : "inactive"));
                                        return 1;
                                    });
                                }))
                        .then(Commands.literal("level")
                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 10))
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            return withSemblance(player, semblance -> {
                                                semblance.setLevel(IntegerArgumentType.getInteger(context, "level"));
                                                send(player, "Semblance level set to " + semblance.getLevel());
                                                return 1;
                                            });
                                        })))));
    }

    private static int withAura(ServerPlayer player, AuraCommand command) {
        return player.getCapability(RWBYMCapabilities.AURA)
                .map(command::run)
                .orElse(0);
    }

    private static int withSemblance(ServerPlayer player, SemblanceCommand command) {
        return player.getCapability(RWBYMCapabilities.SEMBLANCE)
                .map(command::run)
                .orElse(0);
    }

    private static void send(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }

    private static String format(float value) {
        return String.format("%.1f", value);
    }

    private RWBYMCommands() {
    }

    @FunctionalInterface
    private interface AuraCommand {
        int run(IAura aura);
    }

    @FunctionalInterface
    private interface SemblanceCommand {
        int run(ISemblance semblance);
    }
}
