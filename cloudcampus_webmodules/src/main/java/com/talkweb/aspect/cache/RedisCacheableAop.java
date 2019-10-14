package com.talkweb.aspect.cache;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.talkweb.base.annotation.RedisCacheKey;
import com.talkweb.base.annotation.RedisCacheable;
import com.talkweb.base.common.CacheExpireTime;
import com.talkweb.base.common.CacheExpireTimeValues;

public class RedisCacheableAop
{

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheableAop.class);
    private static int expireTime = 0;
    @Resource(name="redisSentinelCacheDataTemplate")
	private RedisTemplate redisTemplate;
    private CacheExpireTimeValues cacheExpireTimeValues;

    public RedisCacheableAop()
    {
    }

    public Object cached(ProceedingJoinPoint pjp, RedisCacheable cache)
        throws Throwable
    {
        logger.info("----------------------------------Start caching---------------------------------" +
"--"
);
        String key = getCacheKey(pjp, cache);
        ValueOperations valueOper = redisTemplate.opsForValue();
        Object value = valueOper.get(key);
        if(value != null)
        {
            logger.info((new StringBuilder()).append("----------------------------------Hit cache:").append(key).append("-----------------------------------").toString());
            return value;
        }
        value = pjp.proceed();
        if(cache.expire() == CacheExpireTime.defaultExpireTime.getTimeValue())
        {
            expireTime = Integer.parseInt(cacheExpireTimeValues.getDefaultExpireTimeValue());
        } else
        if(cache.expire() == CacheExpireTime.maxExpireTime.getTimeValue())
        {
            expireTime = Integer.parseInt(cacheExpireTimeValues.getMaxExpireTimeValue());
        } else
        if(cache.expire() == CacheExpireTime.midExpireTime.getTimeValue())
        {
            expireTime = Integer.parseInt(cacheExpireTimeValues.getMidExpireTimeValue());
        } else
        if(cache.expire() == CacheExpireTime.minExpireTime.getTimeValue())
        {
            expireTime = Integer.parseInt(cacheExpireTimeValues.getMinExpireTimeValue());
        } else
        {
            expireTime = Integer.parseInt(cacheExpireTimeValues.getDefaultExpireTimeValue());
        }
        valueOper.set(key, value, expireTime, TimeUnit.SECONDS);
        logger.info((new StringBuilder()).append("----------------------------------Cache end(").append(expireTime).append(")\uFF1A").append(key).append(":").append(value).append("-----------------------------------").toString());
        return value;
    }

    private synchronized String getCacheKey(ProceedingJoinPoint pjp, RedisCacheable cache)
    {
        StringBuilder buf = new StringBuilder();
        buf.append(pjp.getSignature().getDeclaringTypeName()).append(".").append(pjp.getSignature().getName());
        if(cache.key().length() > 0)
        {
            buf.append(".").append(cache.key());
        }
        Object args[] = pjp.getArgs();
        if(cache.keyMode() == com.talkweb.base.annotation.RedisCacheable.KeyMode.DEFAULT)
        {
            java.lang.annotation.Annotation pas[][] = ((MethodSignature)pjp.getSignature()).getMethod().getParameterAnnotations();
            for(int i = 0; i < pas.length; i++)
            {
                java.lang.annotation.Annotation aannotation[] = pas[i];
                int j1 = aannotation.length;
                int k1 = 0;
                do
                {
                    if(k1 >= j1)
                    {
                        break;
                    }
                    java.lang.annotation.Annotation an = aannotation[k1];
                    if(an instanceof RedisCacheKey)
                    {
                        buf.append(".").append(args[i].toString());
                        break;
                    }
                    k1++;
                } while(true);
            }

        } else
        if(cache.keyMode() == com.talkweb.base.annotation.RedisCacheable.KeyMode.BASIC)
        {
            Object aobj[] = args;
            int j = aobj.length;
            for(int l = 0; l < j; l++)
            {
                Object arg = aobj[l];
                if(arg instanceof String)
                {
                    buf.append(".").append(arg);
                } else
                if((arg instanceof Integer) || (arg instanceof Long) || (arg instanceof Short))
                {
                    buf.append(".").append(arg.toString());
                } else
                if(arg instanceof Boolean)
                {
                    buf.append(".").append(arg.toString());
                }
            }

        } else
        if(cache.keyMode() == com.talkweb.base.annotation.RedisCacheable.KeyMode.ALL)
        {
            Object aobj1[] = args;
            int k = aobj1.length;
            for(int i1 = 0; i1 < k; i1++)
            {
                Object arg = aobj1[i1];
                buf.append(".").append(arg.toString());
            }

        }
        return buf.toString();
    }

}
