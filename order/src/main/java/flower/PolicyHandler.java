package flower;

import flower.config.kafka.KafkaProcessor;

import java.util.Optional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired 
    OrderRepository orderRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_UpdateStatus(@Payload DeliveryStarted deliveryStarted){

        if(deliveryStarted.isMe()){
            Optional<Order> optionalOrder = orderRepository.findById(deliveryStarted.getOrderId());
            Order order = optionalOrder.get();
            order.setStatus(deliveryStarted.getStatus());

            orderRepository.save(order);
        }
            
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryCancelled_UpdateStatus(@Payload DeliveryCancelled deliveryCancelled){

        if(deliveryCancelled.isMe()){
            System.out.println("##### listener UpdateStatus : " + deliveryCancelled.toJson());
            System.out.println();
            System.out.println();
        }
            
    }
}
