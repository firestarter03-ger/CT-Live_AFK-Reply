package net.firestarter03.ctlive_afktimer;

public class Translations {
    // AFK Status Nachrichten
    public static final String AFK_STATUS_ON_DE = "§3[AFK] §7Du bist nun AFK!";
    public static final String AFK_STATUS_ON_EN = "§3[AFK] §7You are now AFK!";
    public static final String AFK_STATUS_OFF_DE = "§3[AFK] §7Willkommen zurück!";
    public static final String AFK_STATUS_OFF_EN = "§3[AFK] §7Welcome back!";

    // AFK Stats
    public static final String AFK_STATS_HEADER_DE = "§b-------AFK-Stats-------";
    public static final String AFK_STATS_HEADER_EN = "§b-------AFK Stats-------";
    public static final String AFK_STATS_TIME_DE = "§eZeit: §5";
    public static final String AFK_STATS_TIME_EN = "§eTime: §5";
    public static final String AFK_STATS_PLAYERS_DE = "§aSpieler Nachrichten von:";
    public static final String AFK_STATS_PLAYERS_EN = "§aMessages from players:";
    public static final String AFK_STATS_NO_PLAYERS_DE = "§aKeine Spieler Nachrichten";
    public static final String AFK_STATS_NO_PLAYERS_EN = "§aNo player messages";
    public static final String AFK_STATS_FOOTER_DE = "§b----------------------";
    public static final String AFK_STATS_FOOTER_EN = "§b----------------------";

    // Auto-Reply Nachricht
    public static final String AFK_MESSAGE_DE = "[Auto-Reply] Aktuell AFK. Ich antworte später!";
    public static final String AFK_MESSAGE_EN = "[Auto-Reply] Currently AFK. I'll reply later!";

    // Zeitformatierung
    public static String formatDuration(long millis, boolean isEnglish) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (isEnglish) {
            if (hours > 0) {
                return String.format("%d hrs %d min", hours, minutes % 60);
            } else if (minutes > 0) {
                return String.format("%d min %d sec", minutes, seconds % 60);
            } else {
                return String.format("%d sec", seconds);
            }
        } else {
            if (hours > 0) {
                return String.format("%d Std %d Min", hours, minutes % 60);
            } else if (minutes > 0) {
                return String.format("%d Min %d Sek", minutes, seconds % 60);
            } else {
                return String.format("%d Sek", seconds);
            }
        }
    }
} 