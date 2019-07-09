package org.zalando.switchboard;

import lombok.AllArgsConstructor;
import org.zalando.switchboard.Switchboard.RecipientsStage;

import static org.zalando.switchboard.Switchboard.AnsweringMachineStage;
import static org.zalando.switchboard.Switchboard.BuildStage;

@AllArgsConstructor
final class DefaultSwitchboardBuilder implements RecipientsStage {

    private final Recipients recipients;
    private final AnsweringMachine machine;

    DefaultSwitchboardBuilder() {
        this(new QueueRecipients(), new QueueAnsweringMachine());
    }

    @Override
    public AnsweringMachineStage recipients(final Recipients recipients) {
        return new DefaultSwitchboardBuilder(recipients, machine);
    }

    @Override
    public BuildStage answeringMachine(final AnsweringMachine machine) {
        return new DefaultSwitchboardBuilder(recipients, machine);
    }

    @Override
    public Switchboard build() {
        return new DefaultSwitchboard(recipients, machine);
    }

}
