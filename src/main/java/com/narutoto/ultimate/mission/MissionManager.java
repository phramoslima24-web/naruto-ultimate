package com.narutoto.ultimate.mission;

import com.narutoto.ultimate.data.PlayerProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.Locale;

/** Rank-based missions issued and received by each village's Kage. */
public final class MissionManager {
    private static final String NPC_TAG = "NarutotoUltimateKage";
    private static final String[] VILLAGES = {"KONOHA", "SUNA", "KIRI", "IWA", "KUMO"};

    private MissionManager() {}

    public static boolean isVillage(String village) {
        for (String valid : VILLAGES) if (valid.equalsIgnoreCase(village)) return true;
        return false;
    }

    public static String kageTitle(String village) {
        return switch (village) {
            case "KONOHA" -> "Hokage";
            case "SUNA" -> "Kazekage";
            case "KIRI" -> "Mizukage";
            case "IWA" -> "Tsuchikage";
            case "KUMO" -> "Raikage";
            default -> "Kage";
        };
    }

    public static String[] villages() { return VILLAGES.clone(); }

    public static Villager spawnKage(ServerPlayer player, String village) {
        String normalized = village.toUpperCase(Locale.ROOT);
        if (!isVillage(normalized)) return null;
        Villager npc = EntityType.VILLAGER.create(player.serverLevel());
        if (npc == null) return null;
        npc.moveTo(player.getX() + 1.5D, player.getY(), player.getZ() + 0.5D, player.getYRot(), 0.0F);
        npc.setCustomName(Component.literal(kageTitle(normalized) + " de " + normalized));
        npc.setCustomNameVisible(true);
        npc.setPersistenceRequired();
        npc.getPersistentData().putBoolean(NPC_TAG, true);
        npc.getPersistentData().putString("UltimateVillage", normalized);
        player.serverLevel().addFreshEntity(npc);
        return npc;
    }

    public static boolean isKageNpc(Villager villager) {
        return villager.getPersistentData().getBoolean(NPC_TAG);
    }

    public static void openBoard(ServerPlayer player, Villager npc) {
        String village = npc.getPersistentData().getString("UltimateVillage");
        String playerVillage = PlayerProfile.getString(player, "Village");
        if (!isVillage(village)) {
            player.sendSystemMessage(Component.literal("§cEste Kage não está associado a uma vila válida."));
            return;
        }
        if ("NONE".equals(playerVillage)) {
            PlayerProfile.setString(player, "Village", village);
            playerVillage = village;
            player.sendSystemMessage(Component.literal("§aVocê agora pertence à Vila " + village + "!"));
        }
        if (!village.equalsIgnoreCase(playerVillage)) {
            player.sendSystemMessage(Component.literal("§cEste é o gabinete de " + village
                + ". Sua vila é " + playerVillage + ". Procure o Kage da sua própria vila."));
            return;
        }

        PlayerProfile.setBoolean(player, "KageAccess", true);
        PlayerProfile.setString(player, "KageAccessVillage", village);
        player.sendSystemMessage(Component.literal("§6§l=== GABINETE DO " + kageTitle(village).toUpperCase(Locale.ROOT) + " ==="));
        player.sendSystemMessage(Component.literal("§7" + kageTitle(village)
            + ": "Tenho uma missão para você, shinobi. Cumpra-a e volte para receber sua recompensa.""));
        showBoard(player);
    }

    public static void showBoard(ServerPlayer player) {
        String rank = PlayerProfile.getString(player, "CampaignStage");
        int completed = PlayerProfile.getInt(player, "Missions_" + rank);
        int required = missionsRequired(rank);

        player.sendSystemMessage(Component.literal("§eRank atual: §f" + rank
            + " §7| Missões concluídas neste rank: §f" + completed + "/" + required));

        if (PlayerProfile.getBoolean(player, "MissionActive")) {
            showActiveMission(player);
            return;
        }
        if (completed >= required) {
            player.sendSystemMessage(Component.literal("§aVocê concluiu todas as missões deste rank. Sua promoção será aplicada ao entregar a última missão."));
            return;
        }

        Mission mission = currentMission(rank, completed + 1);
        player.sendSystemMessage(Component.literal("§6Próxima missão: §f" + mission.id + " - " + mission.title));
        player.sendSystemMessage(Component.literal("§7" + mission.description));
        player.sendSystemMessage(Component.literal("§eObjetivo: §f" + mission.requiredKills + " inimigos hostis"
            + (mission.requiresBoss ? " e derrotar o Wither ou Ender Dragon" : "")));
        player.sendSystemMessage(Component.literal("§aPara aceitar: §f/ultimate aceitar"));
    }

    public static int accept(ServerPlayer player) {
        if (!hasKageAccess(player)) {
            player.sendSystemMessage(Component.literal("§cFale com o Kage da sua vila para pegar uma missão."));
            return 0;
        }
        if (PlayerProfile.getBoolean(player, "MissionActive")) {
            player.sendSystemMessage(Component.literal("§cVocê já tem uma missão ativa. Termine e volte ao Kage."));
            return 0;
        }
        String rank = PlayerProfile.getString(player, "CampaignStage");
        int completed = PlayerProfile.getInt(player, "Missions_" + rank);
        if (completed >= missionsRequired(rank)) {
            player.sendSystemMessage(Component.literal("§eVocê já concluiu as missões deste rank."));
            return 0;
        }
        Mission mission = currentMission(rank, completed + 1);
        PlayerProfile.setBoolean(player, "MissionActive", true);
        PlayerProfile.setBoolean(player, "MissionReady", false);
        PlayerProfile.setString(player, "MissionId", mission.id);
        PlayerProfile.setString(player, "MissionRank", rank);
        PlayerProfile.setInt(player, "MissionProgress", 0);
        PlayerProfile.setInt(player, "MissionRequiredKills", mission.requiredKills);
        PlayerProfile.setBoolean(player, "MissionRequiresBoss", mission.requiresBoss);
        PlayerProfile.setBoolean(player, "MissionBossDone", false);
        PlayerProfile.setBoolean(player, "KageAccess", false);
        player.sendSystemMessage(Component.literal("§aMissão aceita: §f" + mission.title));
        player.sendSystemMessage(Component.literal("§eObjetivo: §f" + mission.requiredKills + " inimigos hostis"
            + (mission.requiresBoss ? " + um chefe lendário" : "")));
        return 1;
    }

    public static int deliver(ServerPlayer player) {
        if (!hasKageAccess(player)) {
            player.sendSystemMessage(Component.literal("§cVolte ao gabinete do Kage da sua vila para entregar a missão."));
            return 0;
        }
        if (!PlayerProfile.getBoolean(player, "MissionActive")) {
            player.sendSystemMessage(Component.literal("§cVocê não tem uma missão ativa."));
            return 0;
        }
        if (!PlayerProfile.getBoolean(player, "MissionReady")) {
            showActiveMission(player);
            player.sendSystemMessage(Component.literal("§cAinda não concluiu o objetivo. Continue a missão."));
            return 0;
        }

        String rank = PlayerProfile.getString(player, "MissionRank");
        int completed = PlayerProfile.getInt(player, "Missions_" + rank) + 1;
        PlayerProfile.setInt(player, "Missions_" + rank, completed);
        PlayerProfile.setInt(player, "MissionsCompleted", PlayerProfile.getInt(player, "MissionsCompleted") + 1);
        int rewardXp = 40 + rankMultiplier(rank) * 20;
        int rewardRep = 5 + rankMultiplier(rank) * 5;
        PlayerProfile.addExperience(player, rewardXp);
        PlayerProfile.addReputation(player, rewardRep);
        clearMission(player);
        player.sendSystemMessage(Component.literal("§aMissão entregue! §e+" + rewardXp + " XP Ultimate §7e §e+" + rewardRep + " reputação."));

        int needed = missionsRequired(rank);
        if (completed >= needed) promote(player, rank);
        else player.sendSystemMessage(Component.literal("§eProgresso do rank: §f" + completed + "/" + needed + " missões."));
        return 1;
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!PlayerProfile.getBoolean(player, "MissionActive")
            || PlayerProfile.getBoolean(player, "MissionReady")) return;

        LivingEntity victim = event.getEntity();
        boolean boss = victim.getType() == EntityType.WITHER || victim.getType() == EntityType.ENDER_DRAGON;
        boolean hostile = victim instanceof Monster;
        if (!hostile && !boss) return;

        if (hostile) {
            int progress = PlayerProfile.getInt(player, "MissionProgress") + 1;
            PlayerProfile.setInt(player, "MissionProgress", progress);
        }
        if (boss) PlayerProfile.setBoolean(player, "MissionBossDone", true);

        int progress = PlayerProfile.getInt(player, "MissionProgress");
        int required = PlayerProfile.getInt(player, "MissionRequiredKills");
        boolean bossDone = !PlayerProfile.getBoolean(player, "MissionRequiresBoss")
            || PlayerProfile.getBoolean(player, "MissionBossDone");
        if (progress >= required && bossDone) {
            PlayerProfile.setBoolean(player, "MissionReady", true);
            player.sendSystemMessage(Component.literal("§a§lMISSÃO CONCLUÍDA! §eVolte ao Kage da sua vila para entregar a missão e receber a recompensa."));
        } else if (hostile || boss) {
            player.sendSystemMessage(Component.literal("§eProgresso da missão: §f" + Math.min(progress, required)
                + "/" + required + (PlayerProfile.getBoolean(player, "MissionRequiresBoss")
                ? " §7| Chefe lendário: " + (bossDone ? "§aSIM" : "§cPENDENTE") : "")));
        }
    }

    public static String status(ServerPlayer player) {
        String rank = PlayerProfile.getString(player, "CampaignStage");
        int completed = PlayerProfile.getInt(player, "Missions_" + rank);
        StringBuilder result = new StringBuilder("§6§l=== CAMPANHA SHINOBI ===\n");
        result.append("§eRank: §f").append(rank).append("\n");
        result.append("§eMissões neste rank: §f").append(completed).append("/").append(missionsRequired(rank)).append("\n");
        result.append("§eMissões totais concluídas: §f").append(PlayerProfile.getInt(player, "MissionsCompleted")).append("\n");
        if (PlayerProfile.getBoolean(player, "MissionActive")) {
            result.append("§eMissão ativa: §f").append(PlayerProfile.getString(player, "MissionId")).append("\n");
            result.append("§eProgresso: §f").append(PlayerProfile.getInt(player, "MissionProgress"))
                .append("/").append(PlayerProfile.getInt(player, "MissionRequiredKills"));
            if (PlayerProfile.getBoolean(player, "MissionRequiresBoss")) {
                result.append("\n§eChefe lendário: §f").append(PlayerProfile.getBoolean(player, "MissionBossDone") ? "Derrotado" : "Pendente");
            }
            if (PlayerProfile.getBoolean(player, "MissionReady")) result.append("\n§aVolte ao Kage para entregar a missão.");
        } else {
            result.append("§7Fale com o Kage da sua vila para receber a próxima missão.");
        }
        return result.toString();
    }

    private static void showActiveMission(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("§eMissão ativa: §f" + PlayerProfile.getString(player, "MissionId")));
        player.sendSystemMessage(Component.literal("§eProgresso: §f" + PlayerProfile.getInt(player, "MissionProgress")
            + "/" + PlayerProfile.getInt(player, "MissionRequiredKills")));
        if (PlayerProfile.getBoolean(player, "MissionRequiresBoss")) {
            player.sendSystemMessage(Component.literal("§eChefe lendário: §f"
                + (PlayerProfile.getBoolean(player, "MissionBossDone") ? "Derrotado" : "Pendente")));
        }
        if (PlayerProfile.getBoolean(player, "MissionReady")) {
            player.sendSystemMessage(Component.literal("§aMissão pronta para entrega! Volte ao Kage e use /ultimate entregar."));
        }
    }

    private static boolean hasKageAccess(ServerPlayer player) {
        return PlayerProfile.getBoolean(player, "KageAccess")
            && PlayerProfile.getString(player, "KageAccessVillage")
                .equalsIgnoreCase(PlayerProfile.getString(player, "Village"));
    }

    private static void clearMission(ServerPlayer player) {
        PlayerProfile.setBoolean(player, "MissionActive", false);
        PlayerProfile.setBoolean(player, "MissionReady", false);
        PlayerProfile.setString(player, "MissionId", "");
        PlayerProfile.setString(player, "MissionRank", "");
        PlayerProfile.setInt(player, "MissionProgress", 0);
        PlayerProfile.setInt(player, "MissionRequiredKills", 0);
        PlayerProfile.setBoolean(player, "MissionRequiresBoss", false);
        PlayerProfile.setBoolean(player, "MissionBossDone", false);
        PlayerProfile.setBoolean(player, "KageAccess", false);
    }

    private static void promote(ServerPlayer player, String oldRank) {
        String next = switch (oldRank) {
            case "GENIN" -> "CHUNIN";
            case "CHUNIN" -> "JOUNIN";
            case "JOUNIN" -> "ANBU";
            case "ANBU" -> "KAGE";
            default -> null;
        };
        if (next == null) {
            player.sendSystemMessage(Component.literal("§6§lCampanha principal concluída! Você completou todas as missões de Kage."));
            return;
        }
        PlayerProfile.setString(player, "CampaignStage", next);
        PlayerProfile.setString(player, "Rank", next);
        if ("ANBU".equals(next)) PlayerProfile.setString(player, "Organization", "ANBU");
        if ("KAGE".equals(next)) PlayerProfile.setString(player, "Organization", "KAGE");
        player.sendSystemMessage(Component.literal("§6§l=== PROMOÇÃO SHINOBI ==="));
        player.sendSystemMessage(Component.literal("§aVocê completou as " + missionsRequired(oldRank) + " missões de " + oldRank + "!"));
        player.sendSystemMessage(Component.literal("§eNovo rank: §f" + next));
        player.sendSystemMessage(Component.literal("§7Vá ao gabinete do Kage para receber suas novas missões."));
    }

    private static int missionsRequired(String rank) {
        return switch (rank) {
            case "GENIN" -> 5;
            case "CHUNIN" -> 10;
            case "JOUNIN" -> 15;
            case "ANBU" -> 20;
            case "KAGE" -> 25;
            default -> 5;
        };
    }

    private static int rankMultiplier(String rank) {
        return switch (rank) {
            case "GENIN" -> 1;
            case "CHUNIN" -> 2;
            case "JOUNIN" -> 3;
            case "ANBU" -> 4;
            default -> 5;
        };
    }

    private static Mission currentMission(String rank, int number) {
        String id = rank + "_" + String.format(Locale.ROOT, "%02d", number);
        int base = switch (rank) {
            case "GENIN" -> 3;
            case "CHUNIN" -> 5;
            case "JOUNIN" -> 7;
            case "ANBU" -> 9;
            default -> 11;
        };
        int kills = base + Math.min(8, (number - 1) / 2);
        boolean boss = ("ANBU".equals(rank) && number == 20)
            || ("KAGE".equals(rank) && number % 5 == 0);
        String[] genin = {"Patrulha dos arredores", "Proteja os viajantes", "Limpe o esconderijo inimigo", "Escolta de suprimentos", "Teste final de campo"};
        String[] chunin = {"Investigue a fronteira", "Intercepte os invasores", "Proteja o mensageiro", "Destrua o acampamento rival", "Recupere os pergaminhos", "Defenda a rota comercial", "Caçe os desertores", "Investigue atividade suspeita", "Proteja a equipe de reconhecimento", "Prova de liderança"};
        String[] jounin = {"Ameaça na fronteira", "Resgate da equipe perdida", "Infiltração no território inimigo", "Elimine o esquadrão hostil", "Proteja o conselho da vila", "Recupere informação secreta", "Ataque ao posto avançado", "Escolta de alto risco", "Caçada aos nukenin", "Defesa estratégica", "Interrompa o contrabando", "Operação de inteligência", "Desarme a ameaça", "Missão de elite", "Prova de Jounin"};
        String[] anbu = {"Operação sombra", "Elimine o alvo secreto", "Infiltração silenciosa", "Recupere o documento proibido", "Neutralize a célula inimiga", "Proteja o segredo da vila", "Caçada aos nukenin de elite", "Operação sem testemunhas", "Ataque ao esconderijo", "Rastreie o traidor", "Destrua a base secreta", "Proteja o Kage", "Recupere a arma lendária", "Interrogatório de campo", "Operação de alto risco", "Elimine a ameaça interna", "Infiltração final", "Defesa da vila", "Última operação", "Provação do Kage"};
        String[] kage = {"Ameaça de nível S", "Defesa das cinco nações", "Caçada à criatura lendária", "Proteja a aliança shinobi", "Desafio do líder"};
        String title = switch (rank) {
            case "GENIN" -> genin[(number - 1) % genin.length];
            case "CHUNIN" -> chunin[(number - 1) % chunin.length];
            case "JOUNIN" -> jounin[(number - 1) % jounin.length];
            case "ANBU" -> anbu[(number - 1) % anbu.length];
            default -> kage[(number - 1) % kage.length];
        };
        String description = boss
            ? "O Kage identificou uma ameaça lendária. Elimine os inimigos da região e derrote um chefe lendário."
            : "O Kage precisa de um shinobi confiável. Complete o objetivo e volte ao gabinete para receber sua recompensa.";
        return new Mission(id, title, description, kills, boss);
    }

    private record Mission(String id, String title, String description, int requiredKills, boolean requiresBoss) {}
}
