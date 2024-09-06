package de.tbodyowski.waros.manager;

import de.tbodyowski.waros.Main;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigVarManager {

    private final FileConfiguration configFile = Main.getInstance().getConfig();
    private String Status_Prefix;
    private Boolean Status_Prefix_on_off;
    private Boolean Join_Leave_Message_on_off;
    private String JoinMessage;
    private String LeaveMassage;
    private Boolean DeathCounter_on_off;
    private Boolean BlockedWords_Toggle;
    private Integer Prefix_LengthLimit;
    private Boolean Uppercase_LengthLimit_Toggle;

    public ConfigVarManager() {
    }

    public String getStatus_Prefix() {
        updateVar();
        return Status_Prefix;
    }

    public void setStatus_Prefix(String status_Prefix) {
        configFile.set("Status-Prefix", status_Prefix);
        updateVar();
    }

    public Boolean getStatus_Prefix_on_off() {
        updateVar();
        return Status_Prefix_on_off;
    }

    public void setStatus_Prefix_on_off(Boolean status_Prefix_on_off) {
        configFile.set("Status-Prefix-on/off", status_Prefix_on_off);
        updateVar();
    }

    public Boolean getJoin_Leave_Message_on_off() {
        updateVar();
        return Join_Leave_Message_on_off;
    }

    public void setJoin_Leave_Message_on_off(Boolean join_Leave_Message_on_off) {
        configFile.set("Join/Leave-Message-on/off", join_Leave_Message_on_off);
        updateVar();
    }

    public String getJoinMessage() {
        updateVar();
        return JoinMessage;
    }

    public void setJoinMessage(String joinMessage) {
        configFile.set("JoinMessage", joinMessage);
        updateVar();
    }

    public String getLeaveMassage() {
        updateVar();
        return LeaveMassage;
    }

    public void setLeaveMassage(String leaveMassage) {
        configFile.set("LeaveMassage", leaveMassage);
        updateVar();
    }

    public Boolean getDeathCounter_on_off() {
        updateVar();
        return DeathCounter_on_off;
    }

    public void setDeathCounter_on_off(Boolean deathCounter_on_off) {
        configFile.set("DeathCounter-on/off", deathCounter_on_off);
        updateVar();

    }

    public Boolean getBlockedWords_Toggle() {
        updateVar();
        return BlockedWords_Toggle;
    }

    public void setBlockedWords_Toggle(Boolean blockedWords_Toggle) {
        configFile.set("BlockedWords-Toggle", blockedWords_Toggle);
        updateVar();
    }

    public Integer getPrefix_LengthLimit() {
        updateVar();
        return Prefix_LengthLimit;
    }

    public void setPrefix_LengthLimit(Integer prefix_LengthLimit) {
        configFile.set("Prefix-LengthLimit", prefix_LengthLimit);
        updateVar();
    }

    public Boolean getUppercase_LengthLimit_Toggle() {
        updateVar();
        return Uppercase_LengthLimit_Toggle;
    }

    public void setUppercase_LengthLimit_Toggle(Boolean uppercase_LengthLimit_Toggle) {
        configFile.set("Uppercase/LengthLimit-Toggle", uppercase_LengthLimit_Toggle);
        updateVar();
    }

    public void updateVar() {
        Main.getInstance().saveConfig();
        Status_Prefix_on_off = configFile.getBoolean("Status-Prefix-on/off");
        Status_Prefix = configFile.getBoolean("Status-Prefix-on/off") ? configFile.getString("Status-Prefix") + " " : "";
        Join_Leave_Message_on_off = configFile.getBoolean("Join/Leave-Message-on/off");
        JoinMessage = configFile.getString("JoinMessage");
        LeaveMassage = configFile.getString("LeaveMassage");
        DeathCounter_on_off = configFile.getBoolean("DeathCounter-on/off");
        BlockedWords_Toggle = configFile.getBoolean("BlockedWords-Toggle");
        Prefix_LengthLimit = configFile.getInt("Prefix-LengthLimit");
        Uppercase_LengthLimit_Toggle = configFile.getBoolean("Uppercase/LengthLimit-Toggle");
    }
}