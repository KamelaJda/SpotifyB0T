package me.kamelajda.utils;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

@Slf4j
public class EventBusErrorHandler implements SubscriberExceptionHandler {

    public static final EventBusErrorHandler instance = new EventBusErrorHandler();

    @Override
    public void handleException(@NotNull Throwable exception, @NotNull SubscriberExceptionContext context) {
        log.error(message(context), exception);
    }

    private static String message(SubscriberExceptionContext context) {
        Method method = context.getSubscriberMethod();
        return "Exception thrown by subscriber method "
                + method.getName()
                + '(' + method.getParameterTypes()[0].getName() + ')'
                + " on subscriber "
                + context.getSubscriber()
                + " when dispatching event: "
                + context.getEvent();
    }
}