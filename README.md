# 개인 프로젝트 : FlowerDelivery

![flower](https://user-images.githubusercontent.com/84487181/120077053-6cc6a900-c0e3-11eb-85cd-0f381041cb7c.jpg)

FlowerDelivery 서비스를 MSA/DDD/Event Storming/EDA 를 포괄하는 분석/설계/구현/운영 전단계를 커버하도록 구성한 프로젝트임

- 체크포인트 : https://workflowy.com/s/assessment/qJn45fBdVZn4atl3


# Table of contents

- [FlowerDelivery](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [분석/설계](#분석설계)
    - [Event Storming 결과](#Event-Storming-결과)
    - [완성된 1차 모형](#완성된-1차-모형)
    - [바운디드 컨텍스트](#바운디드-컨텍스트)
    - [기능적 요구사항 검증](#기능적-요구사항을-커버하는지-검증)
    - [비기능적 요구사항 검증](#비기능-요구사항에-대한-검증)
    - [헥사고날 아키텍처 다이어그램 도출](#헥사고날-아키텍처-다이어그램-도출)
       
  - [구현:](#구현)
    - [DDD 의 적용](#ddd-의-적용)   
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#동기식-호출-과-Fallback-처리)
 

# 서비스 시나리오

[ 기능적 요구사항 ]
1. 고객이 주문을 하면 주문 정보를 바탕으로 배송이 시작된다.
2. 주문은 상품 재고 수량을 초과하여 발생될 수 없다.
3. 고객이 주문취소를 하게 되면 주문정보는 삭제되나, 배송팀에서는 취소된 주문건을 별도의 저장소에 저장한다.
4. 주문팀의 주문 취소는 반드시 배송 취소장부 등록이 선행되어야 한다.
5. 주문과 배송 서비스는 게이트웨이를 통해 고객과 통신한다.
6. 고객은 주문 서비스를 통해 배송현황 정보를 열람할 수 있어야 한다.
7. Report 서비스를 통해 모든 서비스의 진행 내용을 통합 제공한다. 


[ 비기능적 요구사항 ]
1. 트랜잭션
    1. 재고 수량을 확인하여 판매가능한 정보만 주문 메뉴에 노출한다  Sync 호출 
1. 장애격리
    1. Delivery 서비스가 중단되더라도 주문은 365일 24시간 받을 수 있어야 한다  Async (event-driven), Eventual Consistency
1. 성능
    1. 상점 주인은 Report 서비스를 통해서 주문/매출 정보를 확인할 수 있어야 한다  CQRS
    1. 배달상태가 바뀔때마다 알림을 줄 수 있어야 한다 Event driven


# 분석/설계

## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  http://www.msaez.io/#/storming/zx6kKm2e0QdKBjE5esDfrhfkbI92/mine/728f1cc39869f7b8c3cf60ca22bf40c1


### 이벤트 도출
![이벤트6개](https://user-images.githubusercontent.com/84487181/120094129-7ab71100-c159-11eb-9c1f-5bc48c88f353.PNG)

### 부적격 이벤트 탈락
![부적격이벤트](https://user-images.githubusercontent.com/84487181/120094598-3da04e00-c15c-11eb-96a2-07f275cfd830.PNG)

    - 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
        - 주문시>메뉴카테고리선택됨, 주문시>메뉴검색됨 :  UI 의 이벤트이지, 업무적인 의미의 이벤트가 아니라서 제외

### 바운디드 컨텍스트

![bounded_context](https://user-images.githubusercontent.com/84487181/120095268-0af85480-c160-11eb-8e57-499ab925c9cd.PNG)

    - 도메인 서열 분리 
        - Core Domain:  Order, Product, Delivery : 없어서는 안될 핵심 서비스이며, 연견 Up-time SLA 수준을 99.999% 목표, 배포주기 : 1주일 1회 미만, Delivery 1개월 1회 미만
        - Supporting Domain: Report : 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기 : 1주일 1회 이상을 기준 ( 각팀 배포 주기 Policy 적용 )

### 완성된 1차 모형

![1st_finish](https://user-images.githubusercontent.com/84487181/120095294-25cac900-c160-11eb-809a-ae05db92b446.PNG)


### 기능적 요구사항을 커버하는지 검증

![2nd_finish](https://user-images.githubusercontent.com/84487181/120096367-033bae80-c166-11eb-9c72-5bb35bdeb089.png)

    - 고객이 주문을 하면 주문 정보를 바탕으로 배송이 시작된다. (ok)   
    - 주문은 상품 재고 수량을 초과하여 발생될 수 없다. (ok)
    - 고객이 주문취소를 하게 되면 주문정보는 삭제되나, 배송팀에서는 취소된 주문건을 별도의 저장소에 저장한다. (ok)
    - 주문팀의 주문 취소는 반드시 배송 취소장부 등록이 선행되어야 한다. (ok)
    - 주문과 배송 서비스는 게이트웨이를 통해 고객과 통신한다. (ok)
    - 고객은 주문 서비스를 통해 배송현황 정보를 열람할 수 있어야 한다. (ok)
    - Report 서비스를 통해 모든 서비스의 진행 내용을 통합 제공한다. (ok) 


### 비기능 요구사항에 대한 검증

![3rd_finish](https://user-images.githubusercontent.com/84487181/120096379-1189ca80-c166-11eb-9928-578ee31c1244.png)

    - 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리
    - 판매 가능 상품 :  판매가 가능한 상품만 주문 메뉴에 노출됨 , ACID 트랜잭션, Request-Response 방식 처리
    - 주문 완료시 상품 접수 및 Delivery:  Order 서비스에서 Delivery 마이크로서비스로 주문요청이 전달되는 과정에 있어서 Delivery 마이크로 서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함.
    - Product, Customer, Report MicroService 트랜잭션:  주문 접수 상태, 상품 준비 상태 등 모든 이벤트에 대해 Kafka를 통한 Async 방식 처리, 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, Eventual Consistency 를 기본으로 채택함.




## 헥사고날 아키텍처 다이어그램 도출

![hexa](https://user-images.githubusercontent.com/84487181/120078804-232e8c00-c0ec-11eb-8447-4646b59077b5.png)

    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 8084 이다)

```
cd order
mvn spring-boot:run 

cd delivery
mvn spring-boot:run  

cd product
mvn spring-boot:run  

cd report
mvn spring-boot:run  
```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다.
```
package flower;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Delivery_table")
public class Delivery {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private String status;

    @PostPersist
    public void onPostPersist(){
        DeliveryStarted deliveryStarted = new DeliveryStarted();
        BeanUtils.copyProperties(this, deliveryStarted);
        deliveryStarted.publishAfterCommit();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}



```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package flower;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderRepository extends PagingAndSortingRepository<Order, Long>{
}
```
- 적용 후 REST API 의 테스트
```
# 주문 처리
http POST http://localhost:8081/orders productId=1000 qty=30

# 배달 완료 처리
http PATCH http://localhost:8082/deliveries/1 status="Delivery Completed"

# 주문 상태 확인
http GET http://localhost:8082/orders/1
```

## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 주문(order)->고객(customer) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 상품 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
package flower.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="product", url="http://localhost:8084")
public interface ProductService {

    @RequestMapping(method= RequestMethod.GET, path="/checkAndModifyStock")
    public boolean checkAndModifyStock(@RequestParam("productId") String productId, 
                                        @RequestParam("qty") int qty);

}
```

- 주문 받은 즉시 재고 수량을 차감하도록 구현
```
@RequestMapping(value = "/checkAndModifyStock",
        method = RequestMethod.GET,
        produces = "application/json;charset=UTF-8")

public boolean checkAndModifyStock(@RequestParam("productId") Long productId,
                                @RequestParam("qty") int qty)
        throws Exception {
                boolean status = false;
                Optional<Product> productOptional = productRepository.findByProductId(productId);
                Product product = productOptional.get();

                if(product.getStock() >= qty) {
                        product.setStock(product.getStock() - qty);
                        status = true;

                        productRepository.save(product);
                }

                return status;
        }

 }
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 고객 시스템이 장애가 나면 주문도 못받는다는 것을 확인:

```
# 고객 (customer) 서비스를 잠시 내려놓음 (ctrl+c, replicas 0 으로 설정)

#주문처리 
http POST http://localhost:8082/orders customerId=100 productId=100   #Fail
http POST http://localhost:8082/orders customerId=101 productId=101   #Fail

#고객서비스 재기동
cd 결제
mvn spring-boot:run

#주문처리
http POST http://localhost:8082/orders customerId=100 productId=100   #Success
http POST http://localhost:8082/orders customerId=101 productId=101   #Success
```



## 비동기식 호출 publish-subscribe

주문이 완료된 후, 배송 시스템에게 이를 알려주는 행위는 동기식이 아닌 비동기식으로 처리한다.
- 이를 위하여 주문이 접수된 후에 곧바로 주문 접수 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
package flower;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Order_table")
public class Order {

@Id
@GeneratedValue(strategy=GenerationType.AUTO)
private Long id;
private String productId;
private Integer qty;
private String status;

@PostPersist
public void onPostPersist(){
    boolean rslt = OrderApplication.applicationContext.getBean(flower.external.ProductService.class)
    .checkAndModifyStock(this.getProductId(), this.getQty());

    if (rslt) {
        Ordered ordered = new Ordered();
        BeanUtils.copyProperties(this, ordered);
        ordered.publishAfterCommit();
    }
}
```
- 배송 서비스에서는 주문 상태 접수 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
package flower;
...

@Service
public class PolicyHandler{

    @Autowired
    DeliveryRepository deliveryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrdered_StartDelivery(@Payload Ordered ordered){

        if(ordered.isMe()){
            // do, Biz Logics..
            Delivery delivery = new Delivery();
            delivery.setOrderId(ordered.getId());
            delivery.setStatus("DeliveryStarted");

            deliveryRepository.save(delivery);
        }            
    }
}

```

배송 시스템은 주문 시스템과 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 배송시스템이 유지보수로 인해 잠시 내려간 상태라도 주문을 받는데 문제가 없다:
```
# 배송 서비스 (delivery) 를 잠시 내려놓음 (ctrl+c)

#주문처리
http POST http://localhost:8082/orders customerId=100 productId=100   #Success

#주문상태 확인
http GET http://localhost:8082/orders/1     # 주문상태 Ordered 확인

#배송 서비스 기동
cd delivery
mvn spring-boot:run

#주문상태 확인
http GET localhost:8082/orders/1     # 주문 상태 Waited로 변경 확인
```


