/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cpk.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

public class EHCache<K extends Serializable, V extends Serializable> implements ICache<K, V> {
  private static final Log logger = LogFactory.getLog( EHCache.class );
  private Cache cache = null;

  public Cache getCache() {
    return this.cache;
  }

  private synchronized CacheManager getCacheManager( ) {
    return CacheManager.create();
  }

  public EHCache( CacheConfiguration cacheConfiguration ) {
    Cache cache = this.getCacheManager().getCache( cacheConfiguration.getName() );
    if ( cache == null ) {
      cache = new Cache( cacheConfiguration );
      this.getCacheManager().addCache( cache );
    }

    this.cache = cache;
  }

  @Override
  public void put( K key, V value ) {
    // put element in cache with default cache lifetime
    this.put( key, value, this.getTimeToLiveSeconds().intValue() );
  }

  @Override
  public void put( K key, V value, int timeToLiveSeconds ) {
    ClassLoader oldClassLoader = null;
    try {
      oldClassLoader = changeClassLoader();
      final Element element = new Element( key, value );

      // element will live "timeToLiveSeconds" in cache regardless of use; infinite lifetime if "timeToLiveSeconds" = 0
      element.setTimeToLive( timeToLiveSeconds );
      if ( timeToLiveSeconds == 0 ) {
        element.setTimeToIdle( 0 );
      }

      this.getCache().put( element );
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
      final Element element = this.getCache().get( key );
      if ( element != null ) {
        @SuppressWarnings( "unchecked" )
        final V value = (V) element.getObjectValue();
        if ( value != null ) {
          // we have a entry in the cache ... great!
          logger.debug( "Found value in cache for " + key );
          return value;
        }
      }
      return null;
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
    @SuppressWarnings( "unchecked" )
    final Iterable<K> keys = this.getCache().getKeys();
    return keys;
  }

  @Override
  public boolean remove( K key ) {
    ClassLoader oldClassLoader = null;
    try {
      oldClassLoader = changeClassLoader();
      return this.getCache().remove( key );
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
      this.getCache().removeAll();
      logger.info( "Cache " + this.getCache().getName() + " was cleared." );
    } catch ( Exception e ) {
      logger.error( "Error while attempting to clear cache", e );
    } finally {
      logCacheStatus();
      restoreClassLoader( oldClassLoader );
    }
  }

  @Override
  public Number getTimeToLiveSeconds() {
    return this.getCache().getCacheConfiguration().getTimeToLiveSeconds();
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
    logger.debug( "Cache status: " + this.getCache().getMemoryStoreSize() + " in memory, "
      + this.getCache().getDiskStoreSize() + " in disk" );
  }
}
