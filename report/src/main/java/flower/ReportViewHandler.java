package flower;

import flower.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ReportViewHandler {


    @Autowired
    private ReportRepository reportRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrdered_then_CREATE_1 (@Payload Ordered ordered) {
        try {

            if (ordered.isMe()) {
                // view 객체 생성
                Report report = new Report();
                // view 객체에 이벤트의 Value 를 set 함
                report.setOrderId(ordered.getId());
                report.setProductId(ordered.getProductId());
                report.setQty(ordered.getQty());
                // view 레파지 토리에 save
                reportRepository.save(report);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryStarted_then_UPDATE_1(@Payload DeliveryStarted deliveryStarted) {
        try {
            if (deliveryStarted.isMe()) {
                // view 객체 조회
                List<Report> reportList = reportRepository.findByOrderId(deliveryStarted.getOrderId());
                for(Report report : reportList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    report.setDeliveryId(deliveryStarted.getId());
                    report.setStatus(deliveryStarted.getStatus());
                    // view 레파지 토리에 save
                    reportRepository.save(report);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryCancelled_then_UPDATE_2(@Payload DeliveryCancelled deliveryCancelled) {
        try {
            if (deliveryCancelled.isMe()) {
                // view 객체 조회
                List<Report> reportList = reportRepository.findByOrderId(deliveryCancelled.getOrderId());
                for(Report report : reportList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    report.setStatus(deliveryCancelled.getStatus());
                    // view 레파지 토리에 save
                    reportRepository.save(report);
                }
            } 
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}