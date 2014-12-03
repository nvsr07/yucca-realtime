package org.yucca.realtime.adaptor.output.mongodb;

import javax.cache.*;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * A base class for all cache implementations in Application Authentication
 * Framework module.
 */
public class BaseCache<K extends Serializable, V > {

	private static final String CACHE_MANAGER_CEP = "CEPManager";

	private CacheBuilder<K, V> cacheBuilder;
	private String cacheName;
	private int cacheTimeout;

	public BaseCache(String cacheName) {
		this.cacheName = cacheName;
		this.cacheTimeout = -1;
	}

	public BaseCache(String cacheName, int timeout) {
		this.cacheName = cacheName;

		if (timeout > 0) {
			this.cacheTimeout = timeout;
		} else {
			this.cacheTimeout = -1;
		}
	}

	private Cache<K, V> getBaseCache() {

		Cache<K, V> cache = null;
		CacheManager cacheManager = Caching.getCacheManagerFactory()
				.getCacheManager(CACHE_MANAGER_CEP);

		if (cacheTimeout > 0) {

			if (cacheBuilder == null) {
				synchronized (cacheName.intern()) {
					if (cacheBuilder == null) {
						cacheManager.removeCache(cacheName);
						cacheBuilder = cacheManager
								.<K, V> createCacheBuilder(cacheName)
								.setExpiry(
										CacheConfiguration.ExpiryType.ACCESSED,
										new CacheConfiguration.Duration(
												TimeUnit.SECONDS, cacheTimeout))
								.setStoreByValue(false);
						cache = cacheBuilder.build();
					} else {
						cache = cacheManager.getCache(cacheName);
					}
				}
			} else {
				cache = cacheManager.getCache(cacheName);
			}
		} else {
			cache = cacheManager.getCache(cacheName);
		}

		return cache;
	}

	/**
	 * Add a cache entry.
	 * 
	 * @param key
	 *            Key which cache entry is indexed.
	 * @param entry
	 *            Actual object where cache entry is placed.
	 */
	public void addToCache(K key, V entry) {
		// Element already in the cache. Remove it first
		clearCacheEntry(key);

		Cache<K, V> cache = getBaseCache();
		if (cache != null) {
			cache.put(key, entry);
		}
	}

	/**
	 * Retrieves a cache entry.
	 * 
	 * @param key
	 *            CacheKey
	 * @return Cached entry.
	 */
	public V getValueFromCache(K key) {
		Cache<K, V> cache = getBaseCache();
		if (cache != null) {
			if (cache.containsKey(key)) {
				return (V) cache.get(key);
			}
		}
		return null;
	}

	/**
	 * Clears a cache entry.
	 * 
	 * @param key
	 *            Key to clear cache.
	 */
	public void clearCacheEntry(K key) {
		Cache<K, V> cache = getBaseCache();
		if (cache != null) {
			if (cache.containsKey(key)) {
				cache.remove(key);
			}
		}
	}

	/**
	 * Remove everything in the cache.
	 */
	public void clear() {
		Cache<K, V> cache = getBaseCache();
		if (cache != null) {
			cache.removeAll();
		}
	}
}