package org.inspirenxe.skills.impl.component.apply;

import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;

public final class TransactionValidityApplicator implements EventApplicator<Transaction> {

  private final boolean isValid;

  public TransactionValidityApplicator(final boolean isValid) {
    this.isValid = isValid;
  }

  @Override
  public boolean accepts(Event trigger, Transaction target) {
    return trigger instanceof Cancellable && !((Cancellable) trigger).isCancelled();
  }

  @Override
  public void applyInternal(Event trigger, Transaction target) {
    target.setValid(this.isValid);
  }
}
