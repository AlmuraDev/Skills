package org.inspirenxe.skills.impl.content.component.apply.message;

import org.inspirenxe.skills.impl.content.component.apply.cause.CauseFirstEventApplicator;
import org.inspirenxe.skills.impl.content.component.filter.EventCompoundFilterQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.text.channel.MessageChannel;

public class MessageApplicator extends CauseFirstEventApplicator<Event, Player> {

    private final Message message;

    protected MessageApplicator(Message message) {
        super(Player.class, Event.class);
        this.message = message;
    }

    @Override
    protected void applyWithCause(EventCompoundFilterQuery eventData, Event event, Player causeObject) {
        this.message.send(MessageChannel.fixed(causeObject));
    }
}
