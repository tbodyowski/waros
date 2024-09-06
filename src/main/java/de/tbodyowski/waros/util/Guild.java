package de.tbodyowski.waros.util;

import java.util.List;
import java.util.UUID;

public class Guild {

    private final String name;
    private final UUID owner;
    private final List<UUID> members;
    private String prefix;

    public Guild(String name, UUID owner, List<UUID> members) {
        this.name = name;
        this.owner = owner;
        this.members = members;
        this.members.add(owner);

    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID playerId) {
        if (!members.contains(playerId)) {
            members.add(playerId);
        }
    }

    public void removeMember(UUID playerId) {
        members.remove(playerId);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}