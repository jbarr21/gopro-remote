package com.github.jbarr21.goproremote.common.utils;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class RxEventBus {
    private static final Bus BUS = new Bus(PublishSubject.create());

    public static void post(Object o) {
        BUS.onNext(o);
    }

    public static <T> Observable<T> events(Class<T> klass) {
        return BUS.events(klass);
    }

    public static Observable events() {
        return BUS.asObservable();
    }

    public static boolean hasObservers() {
        return BUS.hasObservers();
    }

    public static Bus get() {
        return BUS;
    }

    public static class Bus extends SerializedSubject<Object, Object> {
        public Bus(Subject<Object, Object> actual) {
            super(actual);
        }

        public void post(Object o) {
            onNext(o);
        }

        public <T> Observable<T> events(Class<T> klass) {
            return events().ofType(klass);
        }

        public Observable events() {
            return asObservable();
        }

        public boolean hasObservers() {
            return hasObservers();
        }
    }
}