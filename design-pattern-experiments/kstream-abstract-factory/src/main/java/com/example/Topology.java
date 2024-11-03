package com.example;

import com.example.model.buttons.Product;
import com.example.model.checkboxes.Order;
import com.example.model.factories.POFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Configuration
@Component
public class Topology {
    private final String inputTopic;

    private final String outputTopic;

    private final POFactory poFactory;

    @Autowired
    public Topology(@Value("${input-topic.name}") final String inputTopic,
                    @Value("${output-topic.name}") final String outputTopic,
                    POFactory poFactory) {
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
        this.poFactory = poFactory;
    }

    @Autowired
    public void defaultTopology(final StreamsBuilder builder) {
        Order order = poFactory.createOrder();
        int oVal = order.val();

        Product product = poFactory.createProduct();
        int pVal = product.val();

        builder.stream(inputTopic, Consumed.with(Serdes.Integer(), Serdes.String()))
                .mapValues((key, val)->key+oVal+pVal)
                .to(outputTopic, Produced.with(Serdes.Integer(), Serdes.Integer()));
    }
}
