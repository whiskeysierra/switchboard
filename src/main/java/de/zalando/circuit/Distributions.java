package de.zalando.circuit;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;

public enum Distributions implements Distribution {

    DIRECT {
        
        @Override
        public <E> List<Subscription<E, ?>> distribute(List<Subscription<E, ?>> subscriptions) {
            checkState(subscriptions.size() == 1, "Too many subcriptions for event, expected one");
            return subscriptions.subList(0, 1);
        }
        
    }, 
    
    FIRST {
        
        @Override
        public <E> List<Subscription<E, ?>> distribute(List<Subscription<E, ?>> subscriptions) {
            return subscriptions.subList(0, 1);
        }
        
    },
    
    BROADCAST {
        
        @Override
        public <E> List<Subscription<E, ?>> distribute(List<Subscription<E, ?>> subscriptions) {
            return subscriptions;
        }
        
    }

}
