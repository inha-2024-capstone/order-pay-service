package com.project.yogerOrder.payment.util.stateMachine;

import com.project.yogerOrder.global.util.stateMachine.Transition;
import com.project.yogerOrder.payment.entity.PaymentState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentStaticStateMachine {

    private static final Map<PaymentState, Map<PaymentStateChangeEvent, Transition<PaymentState, PaymentStateChangeEvent>>> transitions = new ConcurrentHashMap<>();

    private static final PaymentState errorState = PaymentState.ERRORED;

    static {
        entryTransition(PaymentState.PAID, PaymentStateChangeEvent.ORDER_CANCELED, PaymentState.CANCELED);
    }

    private static void entryTransition(PaymentState currentState, PaymentStateChangeEvent event, PaymentState nextState) {
        entryTransition(currentState, event, nextState, null);
    }

    private static void entryTransition(PaymentState currentState, PaymentStateChangeEvent event, PaymentState nextState, Runnable action) {
        transitions.putIfAbsent(currentState, new ConcurrentHashMap<>());
        transitions.get(currentState).put(event, new Transition<>(currentState, event, nextState, action));
    }

    public static PaymentState nextState(PaymentState currentState, PaymentStateChangeEvent event) {
        Transition<PaymentState, PaymentStateChangeEvent> transition = transitions.get(currentState).get(event);
        if (transition == null) return errorState;

        transition.runAction();

        return transition.getNextState();
    }
}
