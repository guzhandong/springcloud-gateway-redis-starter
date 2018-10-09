package com.guzhandong.springformwork.gateway.route;

import reactor.core.publisher.Mono;

public interface IRouteCacheRefresh {

    /**
     * 刷新内存中的路由信息
     * @return
     */
    public Mono<Void> flushRoutesPermanentToMemery();
}
