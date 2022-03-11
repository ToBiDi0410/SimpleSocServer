package de.tobias.simpsocserv;

public class Logger {

    public static void info(String section, String message) {
        System.out.println("[SimpleSocServer] [INFO] [" + section + "] " + message);
    }

    public static void warning(String section, String message) {
        System.out.println("[SimpleSocServer] [WARN] [" + section + "] " + message);
    }

    public static void error(String section, String message) {
        System.out.println("[SimpleSocServer] [ERROR] [" + section + "] " + message);
    }

}
