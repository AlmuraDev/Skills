package org.inspirenxe.skills.impl.content.component.apply.economy;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.impl.content.component.apply.cause.CauseFirstEventApplicator;
import org.inspirenxe.skills.impl.content.component.apply.math.MathOperation;
import org.inspirenxe.skills.impl.content.type.skill.component.event.EventData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;

public class EconomyApplicator extends CauseFirstEventApplicator<Event, Player> {

    private final MathOperation operation;

    protected EconomyApplicator(MathOperation operation) {
        super(Player.class, Event.class);
        this.operation = operation;
    }

    @Override
    protected void applyWithCause(EventData eventData, Event event, Player causeObject) {
        Sponge.getServiceManager().provide(EconomyService.class).ifPresent(econ -> {
            Skill skill = this.getSkill(eventData.getSkillType(), causeObject);
            Account account = econ.getOrCreateAccount(causeObject.getUniqueId()).get();
            Currency currency = econ.getDefaultCurrency();

            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(skill);
                account.setBalance(currency, this.operation.apply(account.getBalance(currency)), frame.getCurrentCause());
            }
        });

    }
}
