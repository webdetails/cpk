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

//import mondrian.olap.InvalidArgumentException;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
//import net.sf.ehcache.config.CacheConfiguration;
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.lang.StringUtils;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import pt.webdetails.cpk.CpkEngine;
//import pt.webdetails.cda.CdaEngine;
//import pt.webdetails.cda.cache.monitor.CacheElementInfo;
//import pt.webdetails.cda.cache.monitor.ExtraCacheInfo;

//import javax.naming.OperationNotSupportedException;
//import javax.swing.table.TableModel;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.Serializable;

// TODO: Add extends Serializable constraint to K and V?
public class EHCache<K, V> implements ICache<K, V> {
  private static final Log logger = LogFactory.getLog( EHCache.class );
  private static CacheManager cacheManager;

  private Cache cache = null;

  private int timeToLiveSeconds = 60; //TODO

  static {
    try {
      CacheConfiguration cacheConfiguration = new CacheConfiguration();
      cacheConfiguration.setEternal( true );
      cacheConfiguration.setDiskPersistent( false );
      cacheConfiguration.setMemoryStoreEvictionPolicyFromObject( MemoryStoreEvictionPolicy.LFU );

      Configuration cacheManagerConfiguration = new Configuration();
      cacheManagerConfiguration.setDefaultCacheConfiguration( cacheConfiguration );

      cacheManager = CacheManager.create();
    } catch ( CacheException e ) {
      logger.fatal( "Failed to create cache manager.", e );
    }
  }


  private static synchronized CacheManager getCacheManager( ) {
    return cacheManager;
  }

  protected static synchronized Cache getCacheFromManager( String cacheName ) throws CacheException {
    CacheManager cacheManager = getCacheManager();
    if ( !cacheManager.cacheExists( cacheName ) ) {
      cacheManager.addCache( cacheName );
    }

    return cacheManager.getCache( cacheName );
  }


  //Constructors
  // TODO add ttlSec to constructors

  public EHCache( final Cache cache ) {
    this.cache = cache;
  }

  public EHCache( String cacheName ) {
    this( getCacheFromManager( cacheName ) );
  }



  // TODO
  @Override
  public void put( K key, V value ) {
    final Element storeElement = new Element( key, value );
    //storeElement.setTimeToLive( this.timeToLiveSeconds );
    //storeElement.setTimeToIdle( this.timeToLiveSeconds );
    storeElement.setEternal( true );
    this.cache.put( storeElement );
    //this.cache.flush();

    // Print cache status size
    logger.debug( "Cache status: " + this.cache.getMemoryStoreSize() + " in memory, "
      + this.cache.getDiskStoreSize() + " in disk" );
  }

  // TODO
  @SuppressWarnings( "unchecked" )
  @Override
  public V get( K key ) {
    ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
    try {
      //make sure we have the right class loader in thread to instantiate classes in case DiskStore is used
      //TODO: ehcache 2.5 has ClassLoaderAwareCache
      Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );
      final Element element = cache.get( key );
      if ( element != null ) {
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
  }

  public Cache getCache() {
    return this.cache;
  }

  public boolean remove( K key ) {
    return this.cache.remove( key );
  }

  @SuppressWarnings( "unchecked" )
  public Iterable<K> getKeys() {
    return this.cache.getKeys();
  }

  public void shutdownIfRunning() {
    if ( cacheManager != null ) {
      if ( cache != null ) {
        cache.flush();
      }
      if ( cacheManager.getStatus() == Status.STATUS_ALIVE ) {
        logger.debug( "Shutting down cache manager." );
        cacheManager.shutdown();
        cacheManager = null;
      }
    }
  }

}
