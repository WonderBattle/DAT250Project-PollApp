// Package declaration for the service layer
package com.DAT250Project.PollApp.CacheConfig;

// Spring framework imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

// Java utility imports
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// Marks this class as a Spring service (business logic component)
@Service
public class RedisCacheService {

    // Automatically injects the RedisTemplate configured in RedisConfig
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // === MÉTODOS GENÉRICOS CON UUID ===

    // Stores a value in Redis with a specific timeout
    public void put(String keyPrefix, UUID id, Object value, long timeout, TimeUnit unit) {
        // Builds the complete key using prefix and UUID
        String key = buildKey(keyPrefix, id);
        // Stores the value in Redis with expiration time
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    // Stores a value in Redis without expiration (persists until manually deleted)
    public void put(String keyPrefix, UUID id, Object value) {
        String key = buildKey(keyPrefix, id);
        // Stores value without expiration
        redisTemplate.opsForValue().set(key, value);
    }

    // Retrieves a value from Redis without type safety
    public Object get(String keyPrefix, UUID id) {
        String key = buildKey(keyPrefix, id);
        // Gets the value from Redis (returns Object type)
        return redisTemplate.opsForValue().get(key);
    }

    // Retrieves a value from Redis with type safety and casting
    public <T> T get(String keyPrefix, UUID id, Class<T> type) {
        // Gets the value as Object
        Object value = get(keyPrefix, id);
        // Checks if value is of the expected type and casts it, otherwise returns null
        return type.isInstance(value) ? type.cast(value) : null;
    }

    // Deletes a key-value pair from Redis
    public void delete(String keyPrefix, UUID id) {
        String key = buildKey(keyPrefix, id);
        // Removes the key from Redis
        redisTemplate.delete(key);
    }

    // Checks if a key exists in Redis
    public boolean hasKey(String keyPrefix, UUID id) {
        String key = buildKey(keyPrefix, id);
        // Returns true if key exists, false otherwise
        return redisTemplate.hasKey(key);
    }

    // Helper method to build consistent Redis keys
    private String buildKey(String keyPrefix, UUID id) {
        // Creates key in format: "prefix:uuid"
        return keyPrefix + ":" + id.toString();
    }

    // === MÉTODOS ESPECÍFICOS PARA TU DOMINIO ===

    // Polls - Cache for 10 minutes
    public void cachePoll(UUID pollId, Object poll) {
        put("poll", pollId, poll, 10, TimeUnit.MINUTES);
    }

    public Object getPoll(UUID pollId) {
        return get("poll", pollId);
    }

    public void invalidatePoll(UUID pollId) {
        delete("poll", pollId);
    }

    // Poll Results (estadísticas) - Cache for 5 minutes (shorter TTL as results change frequently)
    public void cachePollResults(UUID pollId, Object results) {
        put("poll_results", pollId, results, 5, TimeUnit.MINUTES);
    }

    public Object getPollResults(UUID pollId) {
        return get("poll_results", pollId);
    }

    // Users - Cache for 30 minutes (longer TTL as user data changes infrequently)
    public void cacheUser(UUID userId, Object user) {
        put("user", userId, user, 30, TimeUnit.MINUTES);
    }

    public Object getUser(UUID userId) {
        return get("user", userId);
    }

    // Votes - Cache for 15 minutes
    public void cacheVote(UUID voteId, Object vote) {
        put("vote", voteId, vote, 15, TimeUnit.MINUTES);
    }

    // Options - Cache for 20 minutes
    public void cacheVoteOption(UUID optionId, Object option) {
        put("option", optionId, option, 20, TimeUnit.MINUTES);
    }

    // === MÉTODOS PARA COLECCIONES ===

    // Caches all polls belonging to a specific user
    public void cacheUserPolls(UUID userId, Object polls) {
        put("user_polls", userId, polls, 10, TimeUnit.MINUTES);
    }

    public Object getUserPolls(UUID userId) {
        return get("user_polls", userId);
    }

    // Caches all votes for a specific poll
    public void cachePollVotes(UUID pollId, Object votes) {
        put("poll_votes", pollId, votes, 5, TimeUnit.MINUTES);
    }

    // === MÉTODOS DE BÚSQUEDA MASIVA ===

    // Caches the complete list of all polls (for listing pages)
    public void cacheAllPolls(Object polls) {
        // Uses a simple string key without UUID for global collections
        redisTemplate.opsForValue().set("all_polls", polls, 5, TimeUnit.MINUTES);
    }

    public Object getAllPolls() {
        return redisTemplate.opsForValue().get("all_polls");
    }

    // Caches the complete list of all users
    public void cacheAllUsers(Object users) {
        redisTemplate.opsForValue().set("all_users", users, 10, TimeUnit.MINUTES);
    }

    public Object getAllUsers() {
        return redisTemplate.opsForValue().get("all_users");
    }
}
