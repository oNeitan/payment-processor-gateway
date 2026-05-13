package br.com.oneitan.repository;

import java.util.List;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.sortedset.ScoreRange;
import io.quarkus.redis.datasource.sortedset.SortedSetCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PaymentRepository {
    
    private SortedSetCommands<String, PaymentDto> zset;
    private static final String PAYMENTS_KEY = "payments:";
    
    @Inject
    public PaymentRepository(RedisDataSource redisDataSource) {
        this.zset = redisDataSource.sortedSet(PaymentDto.class);
    }

    public void registerRequest(String prefix, long timestampInSec, PaymentDto dto) {
        var key = PAYMENTS_KEY + prefix;
        zset.zadd(key, timestampInSec, dto);
    }

    public List<PaymentDto> getByServerContext (String context) {
        var key = PAYMENTS_KEY + context;
        return zset.zrange(key, 0, -1);
    }

    public List<PaymentDto> getByServerContextAndPeriod(String context, Long startSec, Long endSec) {
        var key = PAYMENTS_KEY + context;
        return zset.zrangebyscore(key, ScoreRange.from(startSec, endSec));
    }

    public record PaymentDto(String id, Long amount) {}
}
