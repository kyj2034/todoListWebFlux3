package com.study.webflux.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class JustTest {  //Just 메소드는 말 그대로 "단지"리는 의미이다. text 변수에 대해서 단지 너만 바라본다. 라는 의미.
    
     public static void main(String[] args) {
        String text = "STRING";
        Mono<String> mono = reactor.core.publisher.Mono.just ( text ); // 오직 text 변수에서
        mono.subscribe( str ->{ //subscribe로 행동을 부여하지 않으면 동작을 안 한다.
            System.out.println(str);
        });
        
//        one();
        two();
    }
     
    /**
     *  1. Publisher 는 구독할 대상
     *  2. Subscriber 는 사용자에게 전달할 대상
     *  3. Subscription 는 사용자에게 전달할 내용을 정리한 도화지
     */
    private static Publisher<String> PBS = new Publisher<String>() { // 발행 클래스 Publisher<T> 인터페이스를 실제 생성하여 만든 모습.
        @Override
        public void subscribe(Subscriber<? super String> sbs) { // 시작의 의미, 스트림의 생성 (subscribe 메소드가 받는 클래스는 Subscriber 클래스이다.)
            Subscription subscript = new Subscription() { // 행위 작성할 도면과 같은 인터페이스
                @Override
                public void request(long n) {
                    sbs.onNext("abcd");
                    sbs.onNext("EFGHqq"); // onNext 추가, 행위를 한 번 더!
                    sbs.onComplete();
                }
                @Override
                public void cancel() {
                    
                }
            };
            System.out.println("---시작");
            sbs.onSubscribe ( subscript ); // 사용자 콜백에 대한 행위 넣기
            System.out.println("---종료");
        }
    };
    
    private static void one() {
        // 대부분 from으로 시작하는 메소드는 Mono<T>  또는 Flux<T> 형태의 객체를 반환한다.
        
        // Mono 에서의 행위는 0~1개 
        Mono.from ( PBS ).map (  arg -> arg.length () ).subscribe( arg ->{  //map 을 통해 string 값을 길이로 바꾸었다.
           System.out.println("Mono: " + arg); 
        });
        
        System.out.println(" - ");
        
        // Flux 에서의 행위는 0~n개
        Flux.from ( PBS ).map ( arg -> arg.length () ).subscribe (arg -> {
            System.out.println("Flux :" + arg);
        });
    }
    
    private static void two() {
        List<String> array = new ArrayList<String>() {{
            add("aa");
            add("bb");
            add("cc");
            add("dd");
            add("ee");
            add("ee");
        }};
        
//        Flux.fromIterable ( array ).groupBy ( arr -> arr ).map ( arg ->{ //첫번째 map, arg는 GroupedFlux
//           Mono<Map<Object, Object>> mono = arg.count ().map ( count -> { //두번째 map, arg.count)는 Mono<Long>
//              Map<Object,Object> item = new HashMap<>();
//              item.put ( arg.key(), count );
//              return item;
//           });
//           return mono;
//        }).subscribe ((data)->{
//            // 그냥 print 하면 최종적으로 map 이 Mono<Map<Object, Object>> 로 되돌려 주기에 해당 클래스명이 출력된다.
////            System.out.println(data);
//            // Map 데이터를 출력하기 위해선 Mono 라는 객체로 받았으므로 ... subscribe 을 사용한다.
//            data.subscribe(System.out::println);
//        });
        
        
        
        // 구독할 Flux의 객체가 단일 Flux가 아닌 구독의 구독의 형태로 되어버린 경우, flatMap(비순서), concatMap(순서) 를 사용한다.
        
        
        //flatMap 으로 변경하기
        Disposable dispose = Flux.fromIterable ( array ).groupBy ( arr -> arr ).flatMap ( arg-> { // map 으로 변환된 Flux의 요소를 병합을 통해 단일 Flux로 바꿔 주었다.
           Mono<Map<Object,Object>> mono = arg.count ().map (  count -> {
              Map<Object, Object> item = new HashMap<>();
              item.put ( arg.key(), count );
              return item;
           });
           return mono;
        }).collectList().subscribe ((data)->{  // collectList() 를 추가하면 Map을 리스트 형태로 받을 수 있다.
            System.out.println(data);
        });
        
        
        // subscribe를 통해서 구독한 결과는 Disposable라는 클래스를 반환한다.
        // 해당 클래스는 작업을 취소 또는 삭제할 수 있는지에 대한 정보를 지니고 있다.
        System.out.println("처분? ->" + dispose.isDisposed());
    }
}

