/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

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
    final Element storeElement = new Element( key, value );
    this.cache.put( storeElement );

    // Print cache status size
    logger.debug( "Cache status: " + this.cache.getMemoryStoreSize() + " in memory, "
      + this.cache.getDiskStoreSize() + " in disk" );
  }

  @Override
  public V get( K key ) {
    ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
    try {
      //make sure we have the right class loader in thread to instantiate classes in case DiskStore is used
      //TODO: ehcache 2.5 has ClassLoaderAwareCache
      Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );
      final Element element = cache.get( key );
      if ( element != null ) {
        @SuppressWarnings( "unchecked" )
        final V value = (V) element.getObjectValue();
        if ( value != null ) {
          if ( logger.isDebugEnabled() ) {
            // we have a entry in the cache ... great!
            logger.debug( "Found value in cache. Returning" );
            // Print cache status size
            logger.debug( "Cache status: " + cache.getMemoryStoreSize() + " in memory, "
              + cache.getDiskStoreSize() + " in disk" );
          }
          return value;
        }
      }
      return null;
    } catch ( Exception e ) {
      logger.error( "Error while attempting to load from cache, bypassing cache (cause: " + e.getClass() + ")", e );
      return null;
    } finally {
      Thread.currentThread().setContextClassLoader( contextCL );
    }
  }

  @Override
  public void clear() {
    this.cache.removeAll();
    logger.info( "Cache " + this.cache.getName() + " was cleared." );
  }

  public Cache getCache() {
    return this.cache;
  }

}
