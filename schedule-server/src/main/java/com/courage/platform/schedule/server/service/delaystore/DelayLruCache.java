package com.courage.platform.schedule.server.service.delaystore;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 延迟处理缓存 处理触发了 还未入库的数据
 * Created by zhangyong on 2019/11/14.
 */
public class DelayLruCache {

    private static Cache loadingCache = CacheBuilder.newBuilder().maximumSize(10000).initialCapacity(5).build();

    public static void put(Long key, Object object) {
        loadingCache.put(key, object);
    }

    public static Object get(Long key) {
        return loadingCache.getIfPresent(key);
    }

}
