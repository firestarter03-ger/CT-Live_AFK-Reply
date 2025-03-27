package net.firestarter03.ctlive_afktimer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class CTLIVEAFKTimerClient implements ClientModInitializer {
    private static final int AFK_TIMEOUT = 5 * 60 * 20; // 5 Minuten
    private static long lastActionTime = 0;
    private static boolean isAFK = false;
    private static KeyBinding toggleAFKKey;
    private static final String AFK_MESSAGE = "[Auto-Reply] Aktuell AFK. Ich antworte später!";
    private final Set<String> lastRepliedPlayers = new HashSet<>();
    private static long afkStartTime = 0; // Neue Variable für AFK-Startzeit
    private static final Set<String> afkNotifiedPlayers = new HashSet<>(); // Neue Variable für benachrichtigende Spieler

    @Override
    public void onInitializeClient() {
        toggleAFKKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "AFK Toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "AFK Timer"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (toggleAFKKey.wasPressed()) {
                setAFKStatus(client, !isAFK);
            } else if (isPlayerActive(client)) {
                resetAFKStatus(client);
            } else if (client.player.age - lastActionTime >= AFK_TIMEOUT && !isAFK) {
                setAFKStatus(client, true);
            }
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!isAFK || client.player == null || client.getNetworkHandler() == null) return;

            String rawMessage = message.getString();
            String playerName = client.player.getName().getString();
            String cleanMessage = rawMessage.replaceAll("§[0-9a-fklmnor]", "")
                                         .replaceAll("[\\uE000-\\uF8FF]", "") // Entfernt Unicode-Privatnutzungsbereich
                                         .replaceAll("[\\u2000-\\u2FFF]", "") // Entfernt Unicode-Sonderzeichen
                                         .trim();

            // Debug-Ausgabe
            System.out.println("[AFK-Mod] Empfangene Nachricht: " + cleanMessage);

            // Prüfe, ob es sich um eine eigene Auto-Reply-Nachricht handelt
            if (cleanMessage.contains("[Auto-Reply]")) {
                return;
            }

            // 1. Prüfe auf private Nachrichten (MSG Format)
            if (cleanMessage.matches(".*› " + Pattern.quote(playerName) + "\\].*")) {
                String sender = cleanMessage.replaceAll(".*\\[([^›]+)› " + Pattern.quote(playerName) + "\\].*", "$1").trim();
                System.out.println("[AFK-Mod] Privatnachricht von: " + sender);
                if (!sender.isEmpty() && !sender.equalsIgnoreCase(playerName)) {
                    sendPrivateReply(client, sender);
                }
                return;
            }

            // 2. Prüfe auf Namenserwähnung im normalen Chat
            if (cleanMessage.contains("»")) {
                String[] parts = cleanMessage.split("»");
                if (parts.length > 1) {
                    String afterArrow = parts[1].trim();
                    if (afterArrow.contains(playerName)) {
                        String sender = parts[0].trim();
                        System.out.println("[AFK-Mod] Namenserwähnung gefunden nach » Symbol von: " + sender);
                        if (!sender.isEmpty() && !sender.equalsIgnoreCase(playerName) && !lastRepliedPlayers.contains(sender.toLowerCase())) {
                            sendPrivateReply(client, sender);
                        }
                        return;
                    }
                }
            }

            // 3. Allgemeine Namenserwähnung
            if (containsPlayerName(cleanMessage, playerName)) {
                String sender = extractSenderFromMessage(cleanMessage);
                System.out.println("[AFK-Mod] Extrahierter Absender: " + sender);

                if (sender != null && !sender.isEmpty() && !sender.equalsIgnoreCase(playerName) && !lastRepliedPlayers.contains(sender.toLowerCase())) {
                    System.out.println("[AFK-Mod] Namenserwähnung von: " + sender);
                    sendPrivateReply(client, sender);
                }
            }
        });

        // Registriere einen zusätzlichen Event-Handler für Chat-Nachrichten
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, timestamp) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!isAFK || client.player == null || client.getNetworkHandler() == null) return;

            String rawMessage = message.getString();
            String playerName = client.player.getName().getString();
            String cleanMessage = rawMessage.replaceAll("§[0-9a-fklmnor]", "")
                                         .replaceAll("[\\uE000-\\uF8FF]", "") // Entfernt Unicode-Privatnutzungsbereich
                                         .replaceAll("[\\u2000-\\u2FFF]", "") // Entfernt Unicode-Sonderzeichen
                                         .trim();

            // Debug-Ausgabe
            System.out.println("[AFK-Mod] Empfangene Chat-Nachricht: " + cleanMessage);

            // Prüfe auf Namenserwähnung im Chat
            if (cleanMessage.contains("»")) {
                String[] parts = cleanMessage.split("»");
                if (parts.length > 1) {
                    String afterArrow = parts[1].trim();
                    if (afterArrow.contains(playerName)) {
                        String senderName = parts[0].trim();
                        System.out.println("[AFK-Mod] Namenserwähnung gefunden nach » Symbol von: " + senderName);
                        if (!senderName.isEmpty() && !senderName.equalsIgnoreCase(playerName) && !lastRepliedPlayers.contains(senderName.toLowerCase())) {
                            sendPrivateReply(client, senderName);
                        }
                    }
                }
            }
        });
    }

    private boolean isPlayerActive(MinecraftClient client) {
        return client.options.forwardKey.isPressed() ||
                client.options.backKey.isPressed() ||
                client.options.leftKey.isPressed() ||
                client.options.rightKey.isPressed() ||
                client.options.jumpKey.isPressed() ||
                client.options.sneakKey.isPressed() ||
                client.options.useKey.isPressed() ||
                client.options.attackKey.isPressed() ||
                client.player.input.movementForward != 0 ||
                client.player.input.movementSideways != 0;
    }

    private void resetAFKStatus(MinecraftClient client) {
        if (isAFK) {
            showAFKStats(client);
            afkNotifiedPlayers.clear(); // Liste der benachrichtigenden Spieler zurücksetzen
            setAFKStatus(client, false);
        }
        lastActionTime = client.player.age;
    }

    private void setAFKStatus(MinecraftClient client, boolean afk) {
        isAFK = afk;
        if (afk) {
            afkStartTime = System.currentTimeMillis(); // AFK-Startzeit speichern
        }
        if (!afk) lastRepliedPlayers.clear();
        // AFK-Tag in der Actionbar anzeigen
        client.player.sendMessage(Text.literal(
                afk ? "§3[AFK] §7Du bist nun AFK!" : "§3[AFK] §7Wilkommen zurück!"
        ), true);
    }

    private void showAFKStats(MinecraftClient client) {
        long afkDuration = System.currentTimeMillis() - afkStartTime;
        String afkTime = formatDuration(afkDuration);
        
        // Sende jede Zeile einzeln als separate Nachricht direkt in den Chat
        client.inGameHud.getChatHud().addMessage(Text.literal("§b------AFK-Stats------"));
        client.inGameHud.getChatHud().addMessage(Text.literal("§eZeit: §5" + afkTime));
        
        if (!afkNotifiedPlayers.isEmpty()) {
            client.inGameHud.getChatHud().addMessage(Text.literal("§aSpieler Nachrichten von:"));
            for (String player : afkNotifiedPlayers) {
                client.inGameHud.getChatHud().addMessage(Text.literal("§e " + player));
            }
        } else {
            client.inGameHud.getChatHud().addMessage(Text.literal("§aKeine Spieler Nachrichten"));
        }
        
        // Abschließende Linie
        client.inGameHud.getChatHud().addMessage(Text.literal("§b---------------------"));
    }

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%d Std %d Min", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d Min %d Sek", minutes, seconds % 60);
        } else {
            return String.format("%d Sek", seconds);
        }
    }

    private boolean containsPlayerName(String text, String playerName) {
        // Debug-Ausgabe für den ursprünglichen Text
        System.out.println("[AFK-Mod] Prüfe Text auf Namenserwähnung: " + text);
        System.out.println("[AFK-Mod] Spielername zum Suchen: " + playerName);
        
        // Entferne Formatierungscodes und bereinige den Text
        String cleanedText = text.replaceAll("§[0-9a-fklmnor]", "")
                               .replaceAll("[\\uE000-\\uF8FF]", "") // Entfernt Unicode-Privatnutzungsbereich
                               .replaceAll("[\\u2000-\\u2FFF]", "") // Entfernt Unicode-Sonderzeichen
                               .trim();
        System.out.println("[AFK-Mod] Bereinigter Text: " + cleanedText);
        
        // Prüfe auf » Format
        if (cleanedText.contains("»")) {
            String[] parts = cleanedText.split("»");
            if (parts.length > 1) {
                String afterArrow = parts[1].trim();
                if (afterArrow.contains(playerName)) {
                    System.out.println("[AFK-Mod] Namenserwähnung gefunden nach » Symbol");
                    return true;
                }
            }
        }
        
        // Verbesserte Regex für Namenserwähnungserkennung
        String regex = "(?:^|\\s|[<\\[(])" + Pattern.quote(playerName) + "(?:$|\\s|[>\\])]|\\s" + Pattern.quote(playerName) + "\\s|»\\s*" + Pattern.quote(playerName) + "\\b)";
        boolean contains = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(cleanedText).find();
        
        if (contains) {
            System.out.println("[AFK-Mod] Namenserwähnung gefunden in: " + cleanedText);
        } else {
            System.out.println("[AFK-Mod] Keine Namenserwähnung gefunden");
        }
        return contains;
    }

    private String extractSenderFromMessage(String message) {
        System.out.println("[AFK-Mod] Extrahiere Absender aus Nachricht: " + message);
        
        // Entferne Formatierungscodes und spezielle Chat-Sonderzeichen
        String cleaned = message.replaceAll("§[0-9a-fklmnor]", "")
                              .replaceAll("[\\uE000-\\uF8FF]", "") // Entfernt Unicode-Privatnutzungsbereich
                              .replaceAll("[\\u2000-\\u2FFF]", "") // Entfernt Unicode-Sonderzeichen
                              .trim();
        System.out.println("[AFK-Mod] Bereinigte Nachricht: " + cleaned);

        // 1. [Spieler › Spieler] Format
        if (cleaned.contains("›")) {
            String[] parts = cleaned.split("›");
            if (parts.length > 0) {
                String sender = parts[0].replaceAll(".*\\[([^\\]]+)\\]", "$1").trim();
                if (!sender.isEmpty()) {
                    System.out.println("[AFK-Mod] Gefundener Absender (Format [Spieler ›]): " + sender);
                    return sender;
                }
            }
        }

        // 2. <Spieler> Nachricht
        if (cleaned.startsWith("<")) {
            int end = cleaned.indexOf(">");
            if (end > 0) {
                String sender = cleaned.substring(1, end).trim();
                if (!sender.isEmpty()) {
                    System.out.println("[AFK-Mod] Gefundener Absender (Format <Spieler>): " + sender);
                    return sender;
                }
            }
        }

        // 3. [Team] Spieler: Nachricht
        if (cleaned.startsWith("[") && cleaned.contains("]")) {
            int bracketEnd = cleaned.indexOf("]");
            String afterBracket = cleaned.substring(bracketEnd + 1).trim();
            if (afterBracket.contains(":")) {
                String sender = afterBracket.split(":")[0].trim();
                if (!sender.isEmpty()) {
                    System.out.println("[AFK-Mod] Gefundener Absender (Format [Team] Spieler:): " + sender);
                    return sender;
                }
            }
        }

        // 4. Spieler» Nachricht
        if (cleaned.contains("»")) {
            String sender = cleaned.split("»")[0].trim();
            if (!sender.isEmpty()) {
                System.out.println("[AFK-Mod] Gefundener Absender (Format Spieler»): " + sender);
                return sender;
            }
        }

        // 5. Sonderzeichen gefolgt von Spielername
        if (cleaned.matches("^[^a-zA-Z0-9_]+[a-zA-Z0-9_]+.*")) {
            String sender = cleaned.replaceAll("^[^a-zA-Z0-9_]+([a-zA-Z0-9_]+).*", "$1").trim();
            if (!sender.isEmpty()) {
                System.out.println("[AFK-Mod] Gefundener Absender (Format Sonderzeichen + Name): " + sender);
                return sender;
            }
        }

        // 6. Erstes Wort als Fallback
        String sender = cleaned.split("\\s+")[0].replaceAll("[^a-zA-Z0-9_]", "");
        if (!sender.isEmpty()) {
            System.out.println("[AFK-Mod] Gefundener Absender (Fallback): " + sender);
            return sender;
        }
        
        System.out.println("[AFK-Mod] Kein Absender gefunden");
        return null;
    }

    private void sendPrivateReply(MinecraftClient client, String sender) {
        try {
            if (sender == null || sender.isEmpty()) {
                System.out.println("[AFK-Mod] Ungültiger Absender, keine Nachricht gesendet");
                return;
            }
            
            String command = "msg " + sender + " " + AFK_MESSAGE;
            System.out.println("[AFK-Mod] Sende private Nachricht: " + command);
            client.getNetworkHandler().sendCommand(command);
            lastRepliedPlayers.add(sender.toLowerCase());
            afkNotifiedPlayers.add(sender); // Spieler zur Liste der Benachrichtigenden hinzufügen
            System.out.println("[AFK-Mod] Auto-Reply erfolgreich an: " + sender);
        } catch (Exception e) {
            System.out.println("[AFK-Mod] Fehler beim Senden der Nachricht: " + e.getMessage());
        }
    }
}