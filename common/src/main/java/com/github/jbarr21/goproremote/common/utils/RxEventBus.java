package com.github.jbarr21.goproremote.common.utils;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

public class RxEventBus {
    private SerializedSubject bus = new SerializedSubject(PublishSubject.create());

    public void post(Object o) {
        bus.onNext(o);
    }

    public <T> Observable<T> events(Class<T> klass) {
        return bus.asObservable().ofType(klass);
    }

    public Observable events() {
        return bus.asObservable();
    }

    public boolean hasObservers() {
        return bus.hasObservers();
    }
}
