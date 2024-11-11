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


package pt.webdetails.cpk;

import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import pt.webdetails.cpf.utils.XmlParserFactoryProducer;
import pt.webdetails.cpk.cache.EHCache;
import pt.webdetails.cpk.cache.ICache;
import pt.webdetails.cpk.elements.Element;
import pt.webdetails.cpk.elements.IDataSourceProvider;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.elements.impl.KettleResult;
import pt.webdetails.cpk.elements.impl.KettleResultKey;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CpkEngine {

  private static Log logger = LogFactory.getLog( CpkEngine.class );
  private static final String DEFAULT_SETTINGS_FILENAME = "cpk.xml";
  private static final String DEFAULT_CACHE_SETTINGS_FILENAME = "ehcache.xml";
  private ICpkEnvironment environment;
  private String settingsFilename;
  private TreeMap<String, IElement> elementsMap;
  private IElement defaultElement;
  private String corsAllowed;
  private String domainWhitelist;

  private ICache<KettleResultKey, KettleResult> kettleResultCache;

  private String getDefaultCacheName() {
    return CpkEngine.class.getPackage().getName() + ":" + this.getEnvironment().getPluginName();
  }


  private CacheConfiguration getDefaultCacheConfiguration() {
    CacheConfiguration cacheConfiguration = new CacheConfiguration();
    cacheConfiguration.setName( this.getDefaultCacheName() );
    cacheConfiguration.setMaxEntriesLocalHeap( 100 );
    cacheConfiguration.setMaxEntriesLocalDisk( 10000 );
    cacheConfiguration.setTimeToIdleSeconds( 0 );
    cacheConfiguration.setTimeToLiveSeconds( 0 );
    cacheConfiguration.overflowToDisk( true );
    cacheConfiguration.diskPersistent( false );
    cacheConfiguration.setDiskExpiryThreadIntervalSeconds( 360 );
    cacheConfiguration.diskSpoolBufferSizeMB( 50 );
    cacheConfiguration.setMemoryStoreEvictionPolicyFromObject( MemoryStoreEvictionPolicy.LFU );

    return cacheConfiguration;
  }

  public ICache<KettleResultKey, KettleResult> getKettleResultCache() {
    return this.kettleResultCache;
  }

  private CpkEngine() {
    this.elementsMap = new TreeMap<String, IElement>();
  }

  // singleton
  public static CpkEngine getInstance() {
    return CpkEngineHolder.INSTANCE;
  }

  public synchronized void init( ICpkEnvironment environment ) {
    // invalid environment
    if ( environment == null ) {
      logger.fatal( "Failed to initialize CPK Plugin: null environment" );
      return;
    }

    // skip initialization, engine was previously initialized
    if ( this.environment != null ) {
      logger.warn( "CPK Plugin '" + this.environment.getPluginName() + "' was already initialized" );
      return;
    }

    // initialize engine
    this.environment = environment;
    this.settingsFilename = DEFAULT_SETTINGS_FILENAME;
    this.initializeKettleResultCache();
    this.reload();
  }

  /**
   * Initializes the cache that stores the results of executing kettle elements.
   * First it tries to read the configuration file DEFAULT_CACHE_SETTINGS_FILENAME.
   * If this fails it uses the default hardcoded configuration to initialize the cache.
   */
  private synchronized void initializeKettleResultCache() {
    CacheConfiguration cacheConfiguration;

    InputStream configFile = null;
    try {
      // get configuration from file
      configFile = this.getEnvironment().getContentAccessFactory().getPluginSystemReader( "" )
        .getFileInputStream( DEFAULT_CACHE_SETTINGS_FILENAME );
      Configuration cacheManagerConfiguration = ConfigurationFactory.parseConfiguration( configFile );
      Collection<CacheConfiguration> cacheConfigurations = cacheManagerConfiguration.getCacheConfigurations().values();
      // get one cache configuration, ignore if more are present
      cacheConfiguration = cacheConfigurations.iterator().next();
      // if no name was defined for the cache, use default cache name
      if ( cacheConfiguration.getName() == null || cacheConfiguration.getName().isEmpty() ) {
        cacheConfiguration.setName( this.getDefaultCacheName() );
      }
      logger.debug( this.getEnvironment().getPluginName() + " is using cache configuration from file "
        + DEFAULT_CACHE_SETTINGS_FILENAME );
    } catch ( Exception ioe ) {
      // unable to load cache configuration file. Using hardcoded default configuration.
      logger.info( "No Eh cache configuration file found " + DEFAULT_CACHE_SETTINGS_FILENAME + "." );
      cacheConfiguration = this.getDefaultCacheConfiguration();
      logger.info( this.getEnvironment().getPluginName() + " is using default hardcoded cache configuration." );
    } finally {
      IOUtils.closeQuietly( configFile );
    }

    this.kettleResultCache = new EHCache<KettleResultKey, KettleResult>( cacheConfiguration );
    this.reload();
  }

  public void reload() {
    logger.info( "Initializing CPK Plugin '" + this.environment.getPluginName() + "'" );
    long start = System.currentTimeMillis();

    // TODO: check why we need to reload environment
    this.environment.reload();

    // load elements
    this.loadElements();

    // load settings
    this.loadSettings();

    if ( this.getKettleResultCache() != null ) {
      this.getKettleResultCache().clear();
    }

    long end = System.currentTimeMillis();
    logger.info( "Finished initialization of CPK PLugin '" + this.environment.getPluginName() + "' in "
      + ( end - start ) + " ms" );
  }

  public ICpkEnvironment getEnvironment() {
    return this.environment;
  }

  // return element or null
  public IElement getElement( String elementId ) {
    logger.debug( "Getting element '" + elementId + "'" );
    return this.elementsMap.get( elementId );
  }

  // return read-only elements collection
  public Collection<IElement> getElements() {
    logger.debug( "Getting read-only collection of elements" );
    return Collections.unmodifiableCollection( this.elementsMap.values() );
  }

  // return read-only elements map < id, element >
  public Map<String, IElement> getElementsMap() {
    logger.debug( "Getting read-only map of elements" );
    return Collections.unmodifiableMap( this.elementsMap );
  }

  // return default element or null
  public IElement getDefaultElement() {
    logger.debug( "Getting default element '" + this.defaultElement.getName() + "'" );
    return this.defaultElement;
  }

  // TODO: refactor
  public Status getStatus() {
    if ( this.defaultElement != null ) {
      return new Status( this.elementsMap, this.defaultElement.getName(), this.environment );
    } else {
      return new Status( this.elementsMap, "", this.environment );
    }
  }

  /**
   * Are CORS operations allowed?
   * @return true if the setting exists and CORS is allowed, false otherwise
   */
  public String getCorsAllowed() {
    return corsAllowed;
  }

  /**
   * A comma separated list of allowed domains for CORS
   * @return a comma separated list of domains if the setting is present, null or an empty string otherwise
   */
  public String getDomainWhitelist() {
    return domainWhitelist;
  }

  private void loadElements() {
    try {
      // open settings file
      InputStream is = this.environment.getContentAccessFactory().getPluginSystemReader( null ).
        getFileInputStream( this.settingsFilename );

      // parse settings file
      SAXReader reader = XmlParserFactoryProducer.getSAXReader( null );
      Document doc = reader.read( is );

      // clean elements map
      this.elementsMap.clear();

      // go through each element type
      List elementTypeNodes = doc.selectNodes( "/cpk/elementTypes/elementType" );
      for ( Object elementTypeNode : elementTypeNodes ) {
        Node type = (Node) elementTypeNode;

        // get element type attributes
        String typeName = type.valueOf( "./@name" );
        String typeClass = type.valueOf( "./@class" );
        logger.info( "Loading '" + typeName + "' elements [" + typeClass + "]" );

        // go through each location for elements of that type
        List elementLocations = type.selectNodes( "elementLocations/elementLocation" );
        for ( Object elementLocation : elementLocations ) {
          Node location = (Node) elementLocation;

          // get location attributes
          String path = location.valueOf( "@path" );
          Boolean isRecursive = Boolean.parseBoolean( location.valueOf( "@isRecursive" ) );
          String pattern = location.valueOf( "@pattern" );
          Boolean adminOnly = Boolean.parseBoolean( location.valueOf( "@adminOnly" ) );

          // go through each file in that location and load elements
          Collection<File> files = this.environment.getPluginUtils().getPluginResources( path, isRecursive, pattern );
          if ( files != null ) {
            for ( File file : files ) {
              loadElement( typeName, typeClass, file.getAbsolutePath(), adminOnly );
            }
          }
        }
      }

      // get default element
      this.defaultElement = findDefaultElement(
        doc.selectSingleNode( "/cpk/elementTypes" ).valueOf( "@defaultElement" ).toLowerCase() );

      // close file
      is.close();
    } catch ( IOException e ) {
      logger.error( "Failed to open settings file '" + this.settingsFilename + "'" );
    } catch ( DocumentException e ) {
      logger.error( "Failed to parse settings file '" + this.settingsFilename + "'" );
    }
  }

  private void loadElement( String type, String typeClass, String filePath, boolean adminOnly ) {
    // id = filename in lowercase
    String id = FilenameUtils.getBaseName( filePath ).toLowerCase();

    // skip element if id starts with '_' (private elements)
    if ( id.startsWith( "_" ) ) {
      logger.debug( "Skipped element '" + filePath + "'" );
      return;
    }

    logger.info( "Loading element '" + filePath + "'" );

    // skip element if the id already exists
    if ( this.elementsMap.containsKey( id ) ) {
      logger.warn( "Failed: an element '" + id + "' already exists" );
      return;
    }

    // skip element if the id is a reserved word
    if ( this.environment.getReservedWords().contains( id ) ) {
      logger.warn( "Failed: '" + id + "' is a reserved word" );
      return;
    }

    try {
      // create element wrapper
      Element element = (Element) Class.forName( typeClass ).newInstance();
      // TODO: using plugin name as id. Should a plugin also have an Id and not just a name?
      String pluginId = this.getEnvironment().getPluginName();
      if ( element.init( pluginId, id, type, filePath, adminOnly ) ) {
        // add element to elements map
        this.elementsMap.put( id, element );
        logger.info( "Done " + element.toString() );
      }
      // TODO: check if setting the cache should be done in init, passing the cache as an argument.
      if ( element instanceof IDataSourceProvider ) {
        ( (IDataSourceProvider) element ).setCache( this.getKettleResultCache() );
      }
    } catch ( Exception e ) {
      logger.error( "Failed: missing '" + typeClass + "'" );
    }
  }

  private IElement findDefaultElement( String defaultElementId ) {
    // check if the default element exists
    if ( this.elementsMap.containsKey( defaultElementId ) ) {
      logger.info( "Found default element '" + defaultElementId + "'" );
      return this.elementsMap.get( defaultElementId );
    } else {
      logger.info( "Didn't find default element '" + defaultElementId + "'" );
    }

    // try to find a suitable default element
    for ( IElement element : this.elementsMap.values() ) {
      if ( element.isRenderable() ) {
        logger.info( "Will use '" + element.getId() + "' as default element" );
        return element;
      }
    }

    // no suitable element found
    logger.error( "There isn't a default element" );
    return null;
  }

  // CpkEngineHolder is loaded on the first execution of CpkEngine.getInstance(), not before
  private static class CpkEngineHolder {
    public static final CpkEngine INSTANCE = new CpkEngine();
  }

  /**
   * Loads a configuration from a settings element of cpl.xml in sparkl
   */
  private void loadSettings() {
    try ( InputStream is = this.environment.getContentAccessFactory().getPluginSystemReader( null ).
      getFileInputStream( this.settingsFilename ) ) {

      SAXReader reader = XmlParserFactoryProducer.getSAXReader( null );
      Document doc = reader.read( is );

      final Node flagNode = doc.selectSingleNode( "/cpk/settings/allow-cross-domain-resources" );
      if ( flagNode != null ) {
        this.corsAllowed = flagNode.getText();
      }
      final Node whitelistNode = doc.selectSingleNode( "/cpk/settings/cross-domain-resources-whitelist" );
      if ( whitelistNode != null ) {
        this.corsAllowed = whitelistNode.getText();
      }
    } catch ( IOException e ) {
      logger.error( "Failed to open settings file '" + this.settingsFilename + "'" );
    } catch ( DocumentException e ) {
      logger.error( "Failed to parse settings file '" + this.settingsFilename + "'" );
    }
  }
}
