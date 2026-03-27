package ua.atherium.atheriumquest.quest;

public enum NpcType {
    FERMER("fermer", "Фермер"),
    ALXIMIK("alximik", "Алхимик"),
    WEAPONS("weapons", "Оружейник");

    private final String id;
    private final String name;

    NpcType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static NpcType fromId(String id) {
        for (NpcType type : values()) {
            if (type.id.equalsIgnoreCase(id)) return type;
        }
        return null;
    }
}
