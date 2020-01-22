package switchboard;

import lombok.AllArgsConstructor;
import lombok.With;
import switchboard.Switchboard.RegistryStage;

import static lombok.AccessLevel.PRIVATE;
import static switchboard.Switchboard.AnsweringMachineStage;
import static switchboard.Switchboard.BuildStage;

@AllArgsConstructor
@With(PRIVATE)
final class Builder implements RegistryStage {

    private final Registry registry;
    private final AnsweringMachine machine;

    Builder() {
        this(new DefaultRegistry(), new InMemoryAnsweringMachine());
    }

    @Override
    public AnsweringMachineStage registry(final Registry registry) {
        return withRegistry(registry);
    }

    @Override
    public BuildStage answeringMachine(final AnsweringMachine machine) {
        return withMachine(machine);
    }

    @Override
    public Switchboard build() {
        return new DefaultSwitchboard(registry, machine);
    }

}
