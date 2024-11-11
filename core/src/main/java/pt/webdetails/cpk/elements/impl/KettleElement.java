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


package pt.webdetails.cpk.elements.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.UnknownParamException;
import pt.webdetails.cpk.cache.ICache;
import pt.webdetails.cpk.elements.Element;
import pt.webdetails.cpk.elements.IDataSourceProvider;
import pt.webdetails.cpk.elements.IKettleElement;
import pt.webdetails.cpk.elements.impl.kettleoutputs.InferedKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleoutputs.JsonKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleoutputs.KettleOutput;
import pt.webdetails.cpk.elements.impl.kettleoutputs.ResultFilesKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleoutputs.ResultOnlyKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleoutputs.SingleCellKettleOutput;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class KettleElement<TMeta extends NamedParams>
  extends Element
  implements IDataSourceProvider, IKettleElement {

  // region Inner Definitions

  /**
   * Kettle parameters that have special handling by cpk plugins.
   */
  public enum KettleParameter {
    CACHE_IS_ENABLED( "cpk.cache.isEnabled", "false" ),
    CACHE_TIME_TO_LIVE_SECONDS( "cpk.cache.timeToLiveSeconds", "0" ),
    RESPONSE_MIME_TYPE( "cpk.response.mimeType", null ),
    RESPONSE_ATTACHMENT_NAME( "cpk.response.attachmentName", null ),
    RESPONSE_DOWNLOAD( "cpk.response.download", "false" ),
    RESPONSE_OUTPUT_TYPE( "cpk.response.kettleOutput", "Infered" ),
    RESULT_STEP_NAME( "cpk.result.stepName", "OUTPUT" ),
    EXECUTE_AT_START( "cpk.executeAtStart", "false" );

    public static final Collection<String> reservedNamePrefixes;

    static {
      Collection<String> prefixes = new ArrayList<String>( Arrays.asList( "_", "cpk." ) );
      reservedNamePrefixes = Collections.unmodifiableCollection( prefixes );
    }

    private final String name;
    private final String defaultValue;

    /**
     *
     * @param name the name to check if is a reserved.
     * @return true if the name is a reserved name that can not be passed into a kettle element for execution.
     */
    public static boolean isReservedName( String name ) {
      for ( String prefix : KettleParameter.reservedNamePrefixes ) {
        if ( name.startsWith( prefix ) ) {
          return true;
        }
      }
      return false;
    }

    /**
     *
     * @return The name of this kettle parameter.
     */
    public String parameterName() { return this.name; }

    /**
     * @return the hard coded default value of this kettle parameter.
     */
    public String hardCodedDefaultValue() { return this.defaultValue; }

    /**
     *
     * @param params the NamedParams where to get the default value from.
     * @return the default value of this kettle parameter as defined in params.
     */
    public String metaDefaultValue( NamedParams params ) {
      String value = null;
      try {
        value = params.getParameterDefault( this.name );
      } catch ( UnknownParamException e ) {
        // ignore it
      }
      return value;
    }

    /**
     *
     * @param params
     * @return Returns the default value defined in params.
     * If this value is null, then returns the hardcoded default value.
     */
    public String defaultValue( NamedParams params ) {
      String metaDefaultValue = this.metaDefaultValue( params );
      return metaDefaultValue != null ? metaDefaultValue : this.hardCodedDefaultValue();
    }

    private KettleParameter( String name, String defaultValue ) {
      this.name = name;
      this.defaultValue = defaultValue;
    }
  }

  // endregion

  // region Constants
  protected static final String OUTPUT_NAME_PREFIX = "OUTPUT";

  // TODO: this class should be in the REST layer
  private static class RequestParameterName {
    public static final String STEP_NAME = "stepName";
    public static final String KETTLE_OUTPUT = "kettleOutput";
    public static final String DOWNLOAD = "download";
    public static final String BYPASS_CACHE = "bypassCache";
  }

  // endregion

  // region Fields
  private ICache<KettleResultKey, KettleResult> cache;
  private boolean isResultsCacheEnabled;
  private int timeToLive;

  protected TMeta meta;

  private KettleOutput.Configuration defaultOutputConfiguration;

  private String defaultOutputName;

  // endregion

  // region Getters / Setters
  public String getDefaultOutputName() { return this.defaultOutputName; }
  public KettleElement<TMeta> setDefaultOutputName( String defaultOutputName ) {
    this.defaultOutputName = defaultOutputName;
    return this;
  }

  public int getTimeToLive() { return this.timeToLive; }
  public KettleElement<TMeta> setTimeToLive( int timeToLive ) {
    this.timeToLive = timeToLive;
    return this;
  }

  public boolean isResultsCacheEnabled() {
    return this.cache != null && this.isResultsCacheEnabled;
  }
  public KettleElement<TMeta> setResultsCacheEnabled( boolean enabled ) {
    this.isResultsCacheEnabled = enabled;
    return this;
  }

  // TODO: check if this makes sense here or somewhere closer to the transport layer
  public KettleOutput.Configuration getDefaultOutputConfiguration() {
    return this.defaultOutputConfiguration;
  }
  public KettleElement<TMeta> setDefaultOutputConfiguration( KettleOutput.Configuration configuration ) {
    this.defaultOutputConfiguration = configuration;
    return this;
  }

  // TODO: this JsonIgnore annotation is required due to direct serialization in
  // cpkCoreService.getElementsList() => Refactor getElementsList() to use DTOs
  @JsonIgnore
  @Override
  public ICache<KettleResultKey, KettleResult> getCache() {
    return this.cache;
  }

  @Override
  public KettleElement setCache( ICache<KettleResultKey, KettleResult> cache ) {
    int timeToLive;
    String timeToLiveStr = null;
    if ( this.meta != null ) {
      logger.debug( "Setting cache while job/transform meta is not set. Unable to load default values from ktr/kjb." );
      timeToLiveStr = KettleParameter.CACHE_TIME_TO_LIVE_SECONDS.metaDefaultValue( this.meta );
    }

    if ( timeToLiveStr != null ) {
      try {
        timeToLive = Integer.parseInt( timeToLiveStr );
      } catch ( NumberFormatException e ) {
        timeToLive = cache.getTimeToLiveSeconds().intValue();
      }
    } else {
      timeToLive = cache.getTimeToLiveSeconds().intValue();
    }

    this.setTimeToLive( timeToLive );

    this.cache = cache;
    return this;
  }

  public boolean isExecuteAtStart() {
    String isExecuteAtStartStr = KettleParameter.EXECUTE_AT_START.metaDefaultValue( this.meta );
    return Boolean.parseBoolean( isExecuteAtStartStr );
  }
  // endregion

  // region Element Initialization
  @Override
  public boolean init( final String pluginId, final String id,
                       final String type, final String filePath, boolean adminOnly ) {
    logger.debug( "Creating Kettle Element from '" + filePath + "'" );

    // call base init
    if ( !super.init( pluginId, id, type, filePath, adminOnly ) ) {
      return false;
    }

    // load  meta info
    this.meta = this.loadMeta( filePath );
    if ( this.meta == null ) {
      logger.error( "Failed to retrieve '" + this.getLocation() + "'" );
      return false;
    }

    this.initializeKettleParameters();

    // execute at start?
    if ( this.isExecuteAtStart() ) {
      this.processRequest( this.getInjectedParameters(), null );
    }

    // init was successful
    return true;
  }

  private void initializeKettleParameters() {
    // init variables
    String isCacheEnabledStr = KettleParameter.CACHE_IS_ENABLED.defaultValue( this.meta );
    boolean isResultsCacheEnabled = Boolean.parseBoolean( isCacheEnabledStr );

    String defaultOutputName = this.getDefaultOutputName(
      KettleParameter.RESULT_STEP_NAME.metaDefaultValue( this.meta ),
      KettleParameter.RESULT_STEP_NAME.hardCodedDefaultValue() );

    KettleOutput.Configuration outputConfiguration = this.getOutputConfiguration();

    this
      .setResultsCacheEnabled( isResultsCacheEnabled )
      .setDefaultOutputName( defaultOutputName )
      .setDefaultOutputConfiguration( outputConfiguration );
  }

  private String getDefaultOutputName( String metaOutputName, String hardCodedDefaultOutputName ) {
    Collection<String> existingOutputNames = this.getOutputNames();

    // use output step/jobEntry name from ktr/kjb as default output name
    // if default was not set from ktr/kjb, try to use default hardcoded parameter
    // if default hardcoded output name is not valid, choose one that is valid to use as default.
    String defaultOutputName = existingOutputNames.contains( metaOutputName ) ? metaOutputName
      : existingOutputNames.contains( hardCodedDefaultOutputName ) ? hardCodedDefaultOutputName
      : !existingOutputNames.isEmpty() ? existingOutputNames.iterator().next() : null;

    return defaultOutputName;
  }

  private KettleOutput.Configuration getOutputConfiguration( ) {
    // init default output configuration
    String mimeType = KettleParameter.RESPONSE_MIME_TYPE.defaultValue( this.meta );
    String attachmentName = KettleParameter.RESPONSE_ATTACHMENT_NAME.defaultValue( this.meta );
    String downloadStr = KettleParameter.RESPONSE_DOWNLOAD.defaultValue( this.meta );
    boolean download = Boolean.parseBoolean( downloadStr );
    String outputType = KettleParameter.RESPONSE_OUTPUT_TYPE.defaultValue( this.meta );

    KettleOutput.Configuration configuration = new KettleOutput.Configuration();
    configuration
      .setMimeType( mimeType )
      .setAttachmentName( attachmentName )
      .setSendResultAsAttachment( download )
      .setOutputType( outputType );

    return configuration;
  }

  // endregion

  // region Element Execution
  protected final KettleOutput inferResult( String kettleOutputType ) {

     /*
     *  There are a few different types of kettle output processing.
     *  They can be infered or specified from a request parameter: kettleOutput
     *
     *  1. ResultOnly - we'll discard the output and print statistics only
     *  2. ResultFiles - Download the files we have as result filenames
     *  3. Json - Json output of the resultset
     *  4. csv - CSV output of the resultset
     *  5. SingleCell - We'll get the first line, first row
     *  6. Infered - Infering
     *
     *      These do:
     *  3. SingleCell
     *  4. Json
     *  5. CSV
     *  6. Infered
     */

    if ( kettleOutputType == null || kettleOutputType.isEmpty() ) {
      kettleOutputType = "Infered";
    }


    KettleOutput kettleOutput;
    if ( kettleOutputType.equalsIgnoreCase( "Json" ) ) {
      kettleOutput = new JsonKettleOutput();
    } else if ( kettleOutputType.equalsIgnoreCase( "ResultFiles" ) ) {
      kettleOutput = new ResultFilesKettleOutput();
    } else if ( kettleOutputType.equalsIgnoreCase( "ResultOnly" ) ) {
      kettleOutput = new ResultOnlyKettleOutput();
    } else if ( kettleOutputType.equalsIgnoreCase( "SingleCell" ) ) {
      kettleOutput = new SingleCellKettleOutput();
    } else {
      kettleOutput = new InferedKettleOutput();
    }

    return kettleOutput;
  }

  // TODO this should be in the REST service layer. This method basically "parses" the bloated map.
  @Override
  public final void processRequest( Map<String, Map<String, Object>> bloatedMap ) {

    // "Parse" bloated map
    Map<String, Object> request = bloatedMap.get( "request" );
    String stepName = (String) request.get( RequestParameterName.STEP_NAME );

    // if output type is not defined in request use value from default config.
    // If not defined in default config use hardcoded default.
    String kettleOutputType = this.getStringParameter( (String) request.get( RequestParameterName.KETTLE_OUTPUT ),
                                                       this.getDefaultOutputConfiguration().getOutputType(),
                                                       KettleParameter.RESPONSE_OUTPUT_TYPE.hardCodedDefaultValue() );

    // if download parameter is not defined use value from default output configuration
    String downloadStr = (String) request.get( RequestParameterName.DOWNLOAD );
    boolean download = downloadStr != null ? Boolean.parseBoolean( downloadStr )
      : this.getDefaultOutputConfiguration().getSendResultAsAttachment();

    String bypassCacheStr = (String) request.get( RequestParameterName.BYPASS_CACHE );
    boolean bypassCache = Boolean.parseBoolean( bypassCacheStr != null ? bypassCacheStr : "false" );


    HttpServletResponse httpResponse = (HttpServletResponse) bloatedMap.get( "path" ).get( "httpresponse" );

    Map<String, String> kettleParameters = KettleElementHelper.getKettleParameters( request );

    this.processRequest( kettleParameters, kettleOutputType , stepName, download, bypassCache, httpResponse );
  }


  // TODO: kettleoutput processing should be in the REST service layer
  private void processRequest( Map<String, String> kettleParameters, String outputType, String outputStepName,
                               boolean download, boolean bypassCache, HttpServletResponse httpResponse ) {

    KettleResult result = this.processRequest( kettleParameters, outputStepName, bypassCache );

    if ( result != null ) {
      // get default configuration and overload download value
      KettleOutput.Configuration configuration = this.getDefaultOutputConfiguration().clone();
      configuration.setSendResultAsAttachment( download );

      // Choose kettle output type and process result with it
      KettleOutput kettleOutput = this.inferResult( outputType );
      kettleOutput
        .setConfiguration( configuration )
        .setResponse( httpResponse );

      // TODO: pass configuration to process result
      kettleOutput.processResult( result );
      logger.info( "[ " + result + " ]" );
    }
  }


  /**
   * Executes Executes the kettle transformation / job.
   * @param kettleParameters Parameters to be passed into the kettle transformation/job.
   * @param outputStepName The step name from where the result will be fetched.
   * @param bypassCache If true, forces the request to be processed even if a value for it already exists in the cache.
   *                    Bypassing the cache also updates the cache with the new obtained result.
   * @return The result of executing the kettle transformation / job.
   */
  @Override
  public final KettleResult processRequest( Map<String, String> kettleParameters, String outputStepName,
                                      boolean bypassCache ) {

    // remove parameters which name is reserved
    Map<String, String> filteredKettleParameters = this.sanitizeKettleParameters( kettleParameters );
    filteredKettleParameters.putAll( this.getInjectedParameters() );

    KettleResult result;
    if ( this.isResultsCacheEnabled() ) {
      result = this.processRequestCached( filteredKettleParameters, outputStepName, bypassCache );
    } else {
      result = this.processRequest( filteredKettleParameters, outputStepName );
    }
    return result;
  }

  /**
   *
   * @param kettleParameters The parameters to sanitize.
   * @return A new Map with only the parameters which name is valid.
   */
  private Map<String, String> sanitizeKettleParameters( Map<String, String> kettleParameters ) {
    Map<String, String> sanitizedParameters = new HashMap<String, String>();
    for ( Map.Entry<String, String> parameter : kettleParameters.entrySet() ) {
      if ( !KettleParameter.isReservedName( parameter.getKey() ) ) {
        sanitizedParameters.put( parameter.getKey(), parameter.getValue() );
      }
    }
    return sanitizedParameters;
  }

  /**
   * Executes the kettle transformation / job if no cached valued is found or cache bypass is specified.
   * @param kettleParameters Parameters to be passed into the kettle transformation/job.
   * @param outputStepName The step name from where the result will be fetched.
   * @param bypassCache If true, forces the request to be processed even if a value for it already exists in the cache.
   *                    Bypassing the cache also updates the cache with the new obtained result.
   * @return The result of executing the kettle transformation / job.
   */
  private KettleResult processRequestCached( Map<String, String> kettleParameters, String outputStepName,
                                             boolean bypassCache ) {
    KettleResultKey cacheKey = new KettleResultKey( this.getPluginId(), this.getId(),
      outputStepName, kettleParameters );

    KettleResult result;
    if ( !bypassCache ) {
      result = this.getCache().get( cacheKey );
      if ( result != null ) {
        return result; // Cached value found, return it.
      }
    }

    result = this.processRequest( kettleParameters, outputStepName );
    // put new, or update current, result in cache.
    this.getCache().put( cacheKey, result, this.getTimeToLive() );
    return result;
  }

  private Map<String, String> getInjectedParameters() {
    return KettleElementHelper.getInjectedParameters( this.meta );
  }

  // endregion

  protected boolean isValidOutputName( String Name ) {
    return Name != null && Name.startsWith( OUTPUT_NAME_PREFIX );
  }

  private String getStringParameter( String requestParameter, String configParameter, String hardcodedParameter ) {
    return requestParameter != null ? requestParameter
      : configParameter != null ? configParameter
      : hardcodedParameter;
  }

  /**
   * Executes the kettle transformation / job.
   * @param kettleParameters Parameters to be passed into the kettle transformation/job.
   * @param outputStepName The step name from where the result will be fetched.
   * @return The result of executing the kettle transformation / job.
   */
  protected abstract KettleResult processRequest( Map<String, String> kettleParameters, String outputStepName );

  /**
   * Loads kettle element meta information from file.
   * @param filePath the file path from where to load meta information.
   * @return the loaded meta information.
   */
  protected abstract TMeta loadMeta( String filePath );

  /**
   * Gets the valid output names for the kettle element.
   * @return the valid output names for the kettle element.
   */
  protected abstract Collection<String> getOutputNames();


}
