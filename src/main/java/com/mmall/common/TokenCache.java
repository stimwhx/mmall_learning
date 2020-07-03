package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.TimeUnit;

public class TokenCache {

   public static final String TOKEN_PREFIX= "token_";
    //这个类是用来处理token的缓存的
    //先写一个log
    public static Logger logger = LoggerFactory.getLogger(TokenCache.class);
    //然后用gava里的本地缓存,key和values都是string类型的         开始构建本地的一个builder。它是一个调用链的一个模式
    //initialCapacity(1000)初始化缓存大小为1000，最大值是10000，当超出最大值后。会用LRU算法移除缓存。设置有有校期,有校期为12个小时
    public static LoadingCache<String,String> loadingCache= CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //默认的数据加载实现，当调用get方法时，如果key没有对应的值 就调用这个方法去加载对应的value。
                @Override
                public String load(String key) throws Exception {
                    return "null";
                }
            });
public static void setToken(String key,String values){
    loadingCache.put(key,values);
}
public static String getKey(String key){
    String value=null;
    try {
        value=loadingCache.get(key);
        if("null".equals(value)){
            return null;
        }
        return value;

    }catch(Exception e){
        logger.error("localcache get error",e);
    }
    return null;

}
}
