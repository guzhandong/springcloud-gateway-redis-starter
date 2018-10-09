# springcloud-gateway-redis-starter

提供spring-cloud-gateway在集群环境下部署时，routes信息动态变更的redis扩展。

将gateway的routes信息存储在持久化组件中（当前实现是redis，可以扩展其他etcd、zookeeper等），用来解决集群部署下routes的动态变更问题。

扩展其他持久化组件请参考RedisRouteDefinitionRepository 类，并修改AutoConfigRedisRouteRepository配置类中的的持久化bean

> author : guzhandong  

> email : 569199386@qq.com

> springboot version : 2.0.2.RELEASE



## quickstart

> install project for maven
```
git pull git@github.com:guzhandong/springcloud-gateway-redis-starter.git
cd springcloud-gateway-redis-starter
mvn clean install
```


> add dependent property in pom.xml

```
<dependency>
    <groupId>com.guzhandong.springframework.boot</groupId>
    <artifactId>springcloud-gateway-redis-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

```


> add property in application.yml


```
spring:
  redis:
    host: 127.0.0.1
    port: 6379

```



## use demo
修改路由信息后需要执行刷新操作才会生效；添加和删除操作只是修改持久化组件中的路由信息，刷新操作会将持久化中的路由更新到内存中（执行一次刷新操作所有运行中的网关都会刷新）

使用 http 方式需要开启spring-cloud-gateway的管理端点，application.yml 添加以下配置：

（请注意不要将 http 管理端点暴露到外网，保证其访问安全性）
```
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

> add route

- http
```
curl -d 'POST' http://localhost:8081/actuator/gateway/routes/route_id_8  -d '
{
  "predicates": [
    {
      "name": "Path",
      "args": {
        "_genkey_0": "/test/8"
      }
    }
  ],
  "filters": [
    {
      "name": "RequestRateLimiter",
      "args": {
        "redis-rate-limiter.replenishRate": "2",
        "redis-rate-limiter.burstCapacity": "4"
        
      }
    }
  ],
  "uri": "lb://PROJECT-DEMO"
}
'

```
- redis

```
127.0.0.1:6379> HSET GATEWAY_ROUTE route_id_8 '{"id":"route_id_8","predicates":[{"name":"Path","args":{"_genkey_0":"/test/8"}}],"filters":[{"name":"RequestRateLimiter","args":{"redis-rate-limiter.replenishRate":"2","redis-rate-limiter.burstCapacity":"4"}}],"uri":"lb://PROJECT-DEMO","order":0}'
```

> delete route

- http

```
curl -X 'DELETE' http://localhost:8081/actuator/gateway/routes/route_id_8

```
- redis

```
127.0.0.1:6379> HDEL GATEWAY_ROUTE route_id_8
```


> flush routes

- http

```
curl -X 'POST' http://localhost:8080/actuator/gateway/refresh
```
- redis

```
root@165e54f0b461:/data# redis-cli
127.0.0.1:6379> PUBLISH GATEWAY_ROUTE_NOTIFY permanent
(integer) 2
```

> get all routes

- http

```
curl -X 'GET' http://localhost:8081/actuator/gateway/routes
```

- redis

```
127.0.0.1:6379> HGETALL GATEWAY_ROUTE
1) "route_id_8"
2) "{\"id\":\"route_id_8\",\"predicates\":[{\"name\":\"Path\",\"args\":{\"_genkey_0\":\"/test/8\"}}],\"filters\":[{\"name\":\"RequestRateLimiter\",\"args\":{\"redis-rate-limiter.replenishRate\":\"2\",\"redis-rate-limiter.burstCapacity\":\"4\"}}],\"uri\":\"lb://PROJECT-DEMO\",\"order\":0}"
```
