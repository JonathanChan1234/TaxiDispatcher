package com.jonathan.taxidispatcher.utils;

public class Event<T> {
    public boolean hasBeenHandled = false;
    public T content;

    public Event(T content)  {
        this.content = content;
    }
    public T getContentIfNotHandled() {
        if(!hasBeenHandled) {
            hasBeenHandled = true;
            return content;
        } else {
            return null;
        }
    }

    public T getContent() {
        return content;
    }
}
