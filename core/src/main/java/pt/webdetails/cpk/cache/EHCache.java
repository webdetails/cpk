/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package pt.webdetails.cpk.cache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ehcache.config.CacheConfiguration;

import static org.ehcache.jsr107.Eh107Configuration.fromEhcacheCacheConfiguration;

public class EHCache<K extends Serializable, V extends Serializable> implements ICache<K, V> {
  private static final Log logger = LogFactory.getLog( EHCache.class );
  private Cache<K, V> cache;
  private CacheManager cacheManager;

  public Cache<K, V> getCache() {
    return this.cache;
  }

  private synchronized CacheManager getCacheManager( ) {
    if (cacheManager == null) {
      CachingProvider cachingProvider = Caching.getCachingProvider();
      cacheManager = cachingProvider.getCacheManager();
    }
    return cacheManager;
  }

  public EHCache( String cacheName, CacheConfiguration<K, V> cacheConfiguration ) {
    cache = getCacheManager().getCache( cacheName );
    if ( cache == null ) {
      cache = getCacheManager().createCache( cacheName, fromEhcacheCacheConfiguration( cacheConfiguration ) );
    }
  }

  @Override
  public void put( K key, V value ) {
    // put element in cache with default cache lifetime
    ClassLoader oldClassLoader = null;
    try {
      oldClassLoader = changeClassLoader();
      cache.put( key, value );
    } catch ( Exception e ) {
      logger.error( "Error while attempting to write in cache", e );
    } finally {
      logCacheStatus();
      restoreClassLoader( oldClassLoader );
    }
  }

  @Override
  public V get( K key ) {
    ClassLoader oldClassLoader = null;
    try {
      oldClassLoader = changeClassLoader();
      return (V) cache.get( key );
    } catch ( Exception e ) {
      logger.error( "Error while attempting to read from cache", e );
      return null;
    } finally {
      logCacheStatus();
      restoreClassLoader( oldClassLoader );
    }
  }

   @Override
   public Iterable<K> getKeys() {
     Cache<K, V> ehCache = ( Cache<K, V> ) this.getCache();
     List<K> keys = new ArrayList<>();
     ehCache.forEach(entry -> keys.add(entry.getKey()));
     return keys;
   }

  @Override
  public boolean remove( K key ) {
    ClassLoader oldClassLoader = null;
    try {
      oldClassLoader = changeClassLoader();
      return cache.remove( key );
    } catch ( Exception e ) {
      logger.error( "Error while attempting to remove from cache", e );
      return false;
    } finally {
      logCacheStatus();
      restoreClassLoader( oldClassLoader );
    }
  }

  @Override
  public void clear() {
    ClassLoader oldClassLoader = null;
    try {
      oldClassLoader = changeClassLoader();
      cache.clear();
      logger.info( "Cache was cleared." );
    } catch ( Exception e ) {
      logger.error( "Error while attempting to clear cache", e );
    } finally {
      logCacheStatus();
      restoreClassLoader( oldClassLoader );
    }
  }

  /**
   * Makes sure we have the right class loader in the thread before DiskStore is used.
   * @return The old class loader.
   */
  private ClassLoader changeClassLoader() {
    //TODO: ehcache 2.5 has ClassLoaderAwareCache
    ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );
    return oldClassLoader;
  }

  /**
   * Restores the old class loader after DiskStore is used.
   * @param classLoader The class loader to restore.
   */
  private void restoreClassLoader( ClassLoader classLoader ) {
    Thread.currentThread().setContextClassLoader( classLoader );
  }

  private void logCacheStatus() {
    long cacheSize = 0;
    for ( Object entry : cache ) {
      cacheSize++;
    }
    logger.debug( "Cache status: " + cacheSize + " entries in cache." );
  }
}
