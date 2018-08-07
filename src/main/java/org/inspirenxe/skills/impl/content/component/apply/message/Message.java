package org.inspirenxe.skills.impl.content.component.apply.message;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatType;

public class Message {

    private final Text text;
    private final ChatType chatType;

    public Message(Text text, ChatType chatType) {
        this.text = text;
        this.chatType = chatType;
    }

    public void send(MessageChannel messageChannel) {
        messageChannel.send(this.text, this.chatType);
    }

}
