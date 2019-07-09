package org.zalando.switchboard;

import lombok.AllArgsConstructor;
import org.zalando.switchboard.Switchboard.RecipientsStage;

import static org.zalando.switchboard.Switchboard.AnsweringMachineStage;
import static org.zalando.switchboard.Switchboard.BuildStage;

@AllArgsConstructor
final class DefaultSwitchboardBuilder implements RecipientsStage {

    private final Subscriptions subscriptions;
    private final AnsweringMachine machine;

    DefaultSwitchboardBuilder() {
        this(new QueueSubscriptions(), new QueueAnsweringMachine());
    }

    @Override
    public AnsweringMachineStage recipients(final Subscriptions subscriptions) {
        return new DefaultSwitchboardBuilder(subscriptions, machine);
    }

    @Override
    public BuildStage answeringMachine(final AnsweringMachine machine) {
        return new DefaultSwitchboardBuilder(subscriptions, machine);
    }

    @Override
    public Switchboard build() {
        return new DefaultSwitchboard(subscriptions, machine);
    }

}
