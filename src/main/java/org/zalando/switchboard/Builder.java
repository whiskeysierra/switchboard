package org.zalando.switchboard;

import lombok.AllArgsConstructor;
import org.zalando.switchboard.Switchboard.RegistryStage;

import static org.zalando.switchboard.Switchboard.AnsweringMachineStage;
import static org.zalando.switchboard.Switchboard.BuildStage;

@AllArgsConstructor
final class Builder implements RegistryStage {

    private final Registry registry;
    private final AnsweringMachine machine;

    Builder() {
        this(new DefaultRegistry(), new QueueAnsweringMachine());
    }

    @Override
    public AnsweringMachineStage registry(final Registry registry) {
        return new Builder(registry, machine);
    }

    @Override
    public BuildStage answeringMachine(final AnsweringMachine machine) {
        return new Builder(registry, machine);
    }

    @Override
    public Switchboard build() {
        return new DefaultSwitchboard(registry, machine);
    }

}
