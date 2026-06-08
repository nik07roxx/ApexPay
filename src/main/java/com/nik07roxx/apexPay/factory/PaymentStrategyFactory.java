package com.nik07roxx.apexPay.factory;

import com.nik07roxx.apexPay.exceptions.PaymentStrategyNotFound;
import com.nik07roxx.apexPay.strategy.PaymentStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    // Spring Boot auto-wires ALL beans implementing PaymentStrategy into this list
    @Autowired
    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        PaymentStrategy::getRoutingType,
                        strategy -> strategy
                ));
    }

    public PaymentStrategy getStrategy(String routingType) {
        PaymentStrategy strategy = strategies.get(routingType.toUpperCase());
        if (strategy == null) {
            throw new PaymentStrategyNotFound("Unsupported payment routing type: " + routingType);
        }
        return strategy;
    }
}
