package me.xa5.xrankup;

import java.util.Arrays;

public enum Action {
    ADD_GROUP("addgroup"), BROADCAST("broadcast"), REMOVE_GROUP("removegroup"), MESSAGE("message");

    private final String key;

    Action(String key) {
        this.key = key;
    }

    public static Action fromString(String actionStr) {
        return Arrays.stream(values()).filter(action -> action.getKey().equalsIgnoreCase(actionStr)).findFirst().orElse(null);
    }

    public String getKey() {
        return key;
    }
}