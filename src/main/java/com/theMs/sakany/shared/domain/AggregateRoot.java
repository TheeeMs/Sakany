package com.theMs.sakany.shared.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AggregateRoot {
    private List<DomainEvent> events = new ArrayList<>();

    protected void registerEvent(DomainEvent event){
        events.add(event);
    }
    public List<DomainEvent> getDomainEvents(){
        return Collections.unmodifiableList(events);
    }
    public void clearDomainEvents(){
        events.clear();
    }


}
//Must have:
//A collection to store domain events
//registerEvent(DomainEvent event) — called inside domain methods
//getDomainEvents() — returns the events (immutable list)
//clearDomainEvents() — called after events are published
//Think About:
//Should it be abstract class or just class?
//Should the domainEvents list be private or protected?
//Should registerEvent() be protected (only subclasses call it) or public?