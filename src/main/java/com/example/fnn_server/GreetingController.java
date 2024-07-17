package com.example.fnn_server;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class GreetingController {

    Network n = new Network(new int[]{784, 30, 10}, 0.1, 30, 10);

    @MessageMapping("/hello/{id}")
    @SendTo("/canvas/{id}")
    public Greeting greet(@DestinationVariable String id, HelloMessage message) throws InterruptedException {
        String messageString = message.getName();
        messageString = messageString.substring(1, messageString.length() - 1);

        // Split the string by commas
        String[] stringArray = messageString.split(",");

        // Convert each element to float and store in a float array
        double[][] floatArray = new double[stringArray.length][1];
        for (int i = 0; i < stringArray.length; i++) {
            floatArray[i][0] = Double.parseDouble(stringArray[i].trim());
        }

        return new Greeting(n.num(floatArray));
    }
}