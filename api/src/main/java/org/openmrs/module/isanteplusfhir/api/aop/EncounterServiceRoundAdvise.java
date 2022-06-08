package org.openmrs.module.isanteplusfhir.api.aop;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.TestOrder;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

public class EncounterServiceRoundAdvise extends StaticMethodMatcherPointcutAdvisor implements Advisor {

    private OrderService orderService;

    @Override
    public Advice getAdvice() {
        return new PrintingAroundAdvice();
    }

    private class PrintingAroundAdvice implements MethodInterceptor {

        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable {

            System.out.println("------Method name-----" + methodInvocation.getMethod().getName());
            Object[] args = methodInvocation.getArguments();
            // encounterService = Context.getEncounterService();
            orderService = Context.getOrderService();
            if (args[0] instanceof Encounter) {
                System.out.println("----------------- start round Advice invoke----------------------------");
                Encounter encounter = (Encounter) args[0];
                if (encounter.getForm() != null) {
                    if (encounter.getForm().getName().equals("Analyse de Laboratoire")) {
                        Set<Order> orders = encounter.getObs().stream().map(obs -> {
                            TestOrder order = new TestOrder();
                            order.setConcept(obs.getConcept());
                            order.setEncounter(obs.getEncounter());
                            order.setPatient(encounter.getPatient());
                            order.setOrderer(
                                    obs.getEncounter().getEncounterProviders().stream().findAny().get().getProvider());
                            order.setCareSetting(orderService.getCareSetting(2));
                            return order;
                        }).collect(Collectors.toSet());
                        encounter.setOrders(orders);
                        if (methodInvocation instanceof ReflectiveMethodInvocation) {
                            ReflectiveMethodInvocation invocation = (ReflectiveMethodInvocation) methodInvocation;
                            args[0] = encounter;
                            invocation.setArguments(args);
                        }
                        System.out.println("-----------------Finish modifying args----------------------------");

                    }
                }

            }
            return methodInvocation.proceed();
        }

    }

    @Override
    public boolean isPerInstance() {
        return false;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (method.getName().equals("saveEncounter")) {
            return true;
        }
        return false;
    }

}
