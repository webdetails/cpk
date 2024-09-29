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


package pt.webdetails.cpk.elements;


import pt.webdetails.cpk.cache.ICache;
import pt.webdetails.cpk.datasources.DataSource;
import pt.webdetails.cpk.elements.impl.KettleResult;
import pt.webdetails.cpk.elements.impl.KettleResultKey;

public interface IDataSourceProvider {

  public DataSource getDataSource();

  // TODO: move get/set Cache to another interface ( e.g. ICachable<K,V>)
  // TODO: change KettleResultKey, KettleResult to interfaces?
  public ICache<KettleResultKey, KettleResult> getCache();
  public IDataSourceProvider setCache( ICache<KettleResultKey, KettleResult> cache );

}
