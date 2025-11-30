package com.zjgsu.hxy.AI_brain_teasers.model;

public class ChatMessage {
    private ChatMessageRole role;
    private String content;

    public ChatMessage() {}

    public ChatMessage(ChatMessageRole role, String content) {
        this.role = role;
        this.content = content;
    }

    public ChatMessageRole getRole() {
        return role;
    }

    public void setRole(ChatMessageRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ChatMessageRole role;
        private String content;

        public Builder role(ChatMessageRole role) {
            this.role = role;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public ChatMessage build() {
            return new ChatMessage(role, content);
        }
    }
}

