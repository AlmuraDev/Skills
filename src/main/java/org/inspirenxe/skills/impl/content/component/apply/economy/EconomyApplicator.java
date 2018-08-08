package org.inspirenxe.skills.impl.content.component.apply.economy;

import org.inspirenxe.skills.impl.content.component.apply.cause.CauseFirstEventApplicator;
import org.inspirenxe.skills.impl.content.component.apply.math.MathOperation;
import org.inspirenxe.skills.impl.content.component.filter.EventCompoundFilterQuery;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.util.Identifiable;

public class EconomyApplicator extends CauseFirstEventApplicator<Event, Identifiable> {

    private final MathOperation operation;

    protected EconomyApplicator(MathOperation operation) {
        super(Identifiable.class, Event.class);
        this.operation = operation;
    }

    @Override
    protected void applyWithCause(EventCompoundFilterQuery eventData, Event event, Identifiable causeObject) {
        Sponge.getServiceManager().provide(EconomyService.class).ifPresent(econ -> {
            Account account = econ.getOrCreateAccount(causeObject.getUniqueId()).get();
            Currency currency = econ.getDefaultCurrency();

            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                account.setBalance(currency, this.operation.apply(account.getBalance(currency)), frame.getCurrentCause());
            }
        });

    }
}
