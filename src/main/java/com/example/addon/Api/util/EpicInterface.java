package com.example.addon.Api.util;

@FunctionalInterface
public interface EpicInterface<T, E> {
    E get(T t);
}
