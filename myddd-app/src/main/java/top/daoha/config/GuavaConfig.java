package top.daoha.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.daoha.trigger.listener.OrderPaySuccessListener;

import java.util.concurrent.TimeUnit;

@Configuration
public class GuavaConfig {

    //告诉Spring，这个方法返回的对象要注册到Spring容器中,并制定了相应的名字，可以通过这个名字进行注入
    @Bean(name = "weixinAccessToken")
    public Cache<String, String> weixinAccessToken() {//临时存储数据的容器 类似于一个特殊的Map（字典），但多了自动过期的功能
        return CacheBuilder.newBuilder()//创建一个缓存构建器，开始配置缓存
                .expireAfterWrite(2, TimeUnit.HOURS) //设置过期策略：写入后2小时过期
                .build();//根据前面的配置，创建最终的Cache对象
    }

    @Bean(name = "openidToken")  //Bean的类型来自方法返回值，而不是类名！
    public Cache<String, String> openidToken() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    @Bean
    public EventBus eventBusListener(OrderPaySuccessListener listener){
        EventBus eventBus= new EventBus();
        eventBus.register(listener);
        return eventBus;
    }


}
