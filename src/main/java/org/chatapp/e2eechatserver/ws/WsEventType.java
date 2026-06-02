package org.chatapp.e2eechatserver.ws;

public class WsEventType {
    public static final String CONVERSATION_CREATED = "conversation.created";

    public static final String KEY_ENVELOPE_AVAILABLE = "key.envelope.available";

    public static final String GROUP_MEMBER_ADDED = "group.member.added";
    public static final String GROUP_MEMBER_LEFT = "group.member.left";
    public static final String GROUP_MEMBER_REMOVED = "group.member.removed";
    public static final String REMOVED_FROM_GROUP = "removed.from.group";
    public static final String GROUP_NAME_CHANGED = "group.name.changed";

    public static final String MESSAGE_CREATED = "message.created";
    public static final String MESSAGE_CREATED_INFO = "message.created.info";

    private WsEventType() { }
}
