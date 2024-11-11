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


public interface ICache<K, V> {

  /**
   * Stores a key, value pair in the cache.
   * If the cache already contains the given key, then the new value overrides the old.
   * @param key
   * @param value
   */
  void put( K key, V value );

  void put( K key, V value, int timeToLiveSeconds );

  /**
   * Removes value with given key from cache.
   * @param key
   * @return <code>true</code> if element existed.
   */
  public boolean remove( K key );

  /**
   *
   * @param key The key of the cached value.
   * @return The cached value for the given key or null if not found.
   */
  V get( K key );

  /**
   *
   * @return The keys of all cached values.
   */
  Iterable<K> getKeys();

  /**
   * Removes all elements from cache.
   */
  void clear();

  /**
   *
   * @return The default time to live (in seconds) for cached values.
   */
  Number getTimeToLiveSeconds();

}
