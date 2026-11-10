package com.narutoto.ultimate.integration;

import net.minecraftforge.fml.ModList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Detects optional addons without creating hard compile-time dependencies. */
public final class AddonDetector {
    private static final Map<String, String> ADDONS = new LinkedHashMap<>();
    static {
        ADDONS.put("narutoto_clans", "Clãs");
        ADDONS.put("narutoto_allbijuu", "All Bijuu");
        ADDONS.put("narutoto_mangekyou", "Mangekyou");
        ADDONS.put("narutoto_akatsuki_anbu", "Akatsuki & ANBU");
        ADDONS.put("ninjawars", "Ninja Wars");
        ADDONS.put("rinnegan_paths", "Rinnegan: Six Paths");
        ADDONS.put("rinnesharingan", "Rinne-Sharingan");
        ADDONS.put("edotensei", "Edo Tensei");
        ADDONS.put("juubiaddon", "Juubi");
        ADDONS.put("narutolimit", "Limite de Atributos");
    }
    private AddonDetector() {}
    public static boolean isLoaded(String modId) { return ModList.get().isLoaded(modId); }
    public static Map<String, Boolean> status() {
        Map<String, Boolean> result = new LinkedHashMap<>();
        ADDONS.keySet().forEach(id -> result.put(id, isLoaded(id)));
        return result;
    }
    public static String loadedSummary() {
        return status().entrySet().stream().filter(Map.Entry::getValue)
            .map(e -> ADDONS.get(e.getKey())).collect(Collectors.joining(", "));
    }
}
