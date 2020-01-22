package switchboard;

import lombok.Value;

@Value(staticConstructor = "of")
public final class Key<T, A> {

    Class<T> type;
    A annotation;

}
