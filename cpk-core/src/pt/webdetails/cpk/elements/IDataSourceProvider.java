package pt.webdetails.cpk.elements;


import pt.webdetails.cpk.cache.ICache;
import pt.webdetails.cpk.datasources.DataSource;
import pt.webdetails.cpk.elements.impl.KettleResult;
import pt.webdetails.cpk.elements.impl.KettleResultKey;

public interface IDataSourceProvider {

  public DataSource getDataSource();

  // TODO: change KettleResultKey, KettleResult to interfaces?
  public ICache<KettleResultKey, KettleResult> getCache();
  public IDataSourceProvider setCache( ICache<KettleResultKey, KettleResult> cache );

}
