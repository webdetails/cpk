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
