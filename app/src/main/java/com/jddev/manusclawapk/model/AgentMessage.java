package com.jddev.manusclawapk.model;

public class AgentMessage {
    public static final int ROLE_USER      = 0;
    public static final int ROLE_AGENT     = 1;
    public static final int ROLE_SYSTEM    = 2;
    public static final int ROLE_TOOL      = 3;

    public int    role;
    public String content;
    public long   ts;

    public AgentMessage(int role, String content) {
        this.role    = role;
        this.content = content;
        this.ts      = System.currentTimeMillis();
    }

    public String roleLabel() {
        switch (role) {
            case ROLE_USER:   return "YOU";
            case ROLE_AGENT:  return "AGENT";
            case ROLE_SYSTEM: return "SYS";
            case ROLE_TOOL:   return "TOOL";
            default:          return "?";
        }
    }
}
