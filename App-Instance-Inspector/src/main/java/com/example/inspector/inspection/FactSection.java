package com.example.inspector.inspection;

import java.util.Arrays;
import java.util.Optional;

public enum FactSection {
    HOSTNAME("hostname", "Nazwa", Category.SYSTEM),
    OS("os", "System", Category.SYSTEM),
    KERNEL("kernel", "Jądro", Category.SYSTEM),
    ARCH("arch", "Architektura", Category.SYSTEM),
    VIRT("virt", "Wirtualizacja", Category.SYSTEM),
    UPTIME("uptime", "Czas działania", Category.SYSTEM),
    BOOT("boot", "Ostatni start", Category.SYSTEM),
    LOAD("load", "Obciążenie", Category.SYSTEM),
    PROCESSES("processes", "Procesy", Category.SYSTEM),
    MODEL("model", "Model", Category.HARDWARE),
    CPU("cpu", "Procesor", Category.HARDWARE),
    CORES("cores", "Rdzenie", Category.HARDWARE),
    MEMORY("memory", "Pamięć", Category.HARDWARE),
    GPU("gpu", "Karta graficzna", Category.HARDWARE),
    DISKS("disks", "Dyski", Category.HARDWARE),
    BLOCKS("blocks", "Urządzenia blokowe", Category.HARDWARE),
    MOUNTS("mounts", "Punkty montowania", Category.HARDWARE),
    IP("ip", "Adresy IP", Category.NETWORK),
    MAC("mac", "Adresy MAC", Category.NETWORK),
    ROUTES("routes", "Trasy", Category.NETWORK),
    DNS("dns", "Serwery DNS", Category.NETWORK),
    LISTENING("listening", "Nasłuchujące porty", Category.NETWORK),
    RUNNING("running", "Działające usługi", Category.SOFTWARE),
    UNIT_FILES("unitfiles", "Wszystkie usługi", Category.SOFTWARE),
    TIMERS("timers", "Zadania systemowe", Category.SOFTWARE),
    CRON("cron", "Cron (harmonogram i program)", Category.SOFTWARE),
    DOCKER("docker", "Docker", Category.SOFTWARE),
    CONTAINERS("containers", "Kontenery", Category.SOFTWARE),
    IMAGES("images", "Obrazy", Category.SOFTWARE),
    VOLUMES("volumes", "Wolumeny", Category.SOFTWARE),
    RUNTIMES("runtimes", "Środowiska", Category.SOFTWARE),
    PACKAGES("packages", "Pakiety", Category.SOFTWARE),
    WHOAMI("whoami", "Konto badania", Category.USERS),
    ACCOUNTS("accounts", "Konta", Category.USERS),
    USER_GROUPS("usergroups", "Grupy", Category.USERS),
    USERS("users", "Zalogowani", Category.USERS),
    LOGINS("logins", "Ostatnie logowania", Category.USERS);

    private final String key;
    private final String label;
    private final Category category;

    FactSection(String key, String label, Category category) {
        this.key = key;
        this.label = label;
        this.category = category;
    }

    public static Optional<FactSection> of(String key) {
        return Arrays.stream(values()).filter(section -> section.key.equals(key)).findFirst();
    }

    public String key() {
        return key;
    }

    public String label() {
        return label;
    }

    public String categoryLabel() {
        return category.label;
    }

    private enum Category {
        SYSTEM("System"),
        HARDWARE("Sprzęt i dyski"),
        NETWORK("Sieć"),
        SOFTWARE("Oprogramowanie"),
        USERS("Użytkownicy");

        private final String label;

        Category(String label) {
            this.label = label;
        }
    }
}
