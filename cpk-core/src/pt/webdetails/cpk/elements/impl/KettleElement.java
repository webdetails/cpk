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

package pt.webdetails.cpk.elements.impl;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.pentaho.di.core.parameters.NamedParams;
import pt.webdetails.cpk.cache.ICache;
import pt.webdetails.cpk.elements.Element;
import pt.webdetails.cpk.elements.IDataSourceProvider;
import pt.webdetails.cpk.elements.impl.kettleoutputs.IKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleoutputs.InferedKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleoutputs.JsonKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleoutputs.ResultFilesKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleoutputs.ResultOnlyKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleoutputs.SingleCellKettleOutput;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

public abstract class KettleElement<TMeta extends NamedParams> extends Element implements IDataSourceProvider {

  protected static final String DEFAULT_STEP = "OUTPUT";
  protected static final String KETTLEOUTPUT_CLASSES_NAMESPACE = "pt.webdetails.cpk.elements.impl.kettleOutputs";
  

  private ICache<KettleResultKey, KettleResult> cache;
  protected TMeta meta;

  private boolean isCacheEnabled;

  public boolean isCacheEnabled() {
    return this.isCacheEnabled;
  }

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

    // add base parameters to ensure they exist
    KettleElementHelper.addBaseParameters( this.meta );

    // execute at start?
    if ( KettleElementHelper.isExecuteAtStart( this.meta ) ) {
      this.processRequestGetResult( Collections.<String, String>emptyMap(), DEFAULT_STEP );
    }

    // init was successful
    return true;
  }

  protected abstract TMeta loadMeta( String filePath );


  @Override
  @JsonIgnore // TODO: this is required due to direct serialization in cpkCoreService.getElementsList() => Refactor getElementsList() to use DTOs
  public ICache<KettleResultKey, KettleResult> getCache() {
    return this.cache;
  }

  @Override
  public KettleElement setCache( ICache<KettleResultKey, KettleResult> cache ) {
    this.cache = cache;
    return this;
  }


  protected IKettleOutput inferResult( String kettleOutputType, String stepName, boolean download
    , HttpServletResponse httpResponse ) {

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
     *  By complexity:
     *      These don't require rowListener:
     *  1. ResultOnly
     *  2. ResultFiles
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

    if ( stepName == null || stepName.isEmpty() ) {
      stepName = DEFAULT_STEP;
    }

    IKettleOutput kettleOutput;
    if ( kettleOutputType.equalsIgnoreCase( "Json" ) ) {
      kettleOutput = new JsonKettleOutput( httpResponse, download );
    } else if ( kettleOutputType.equalsIgnoreCase( "ResultFiles" ) ) {
      kettleOutput = new ResultFilesKettleOutput( httpResponse, download );
    } else if ( kettleOutputType.equalsIgnoreCase( "ResultOnly" ) ) {
      kettleOutput = new ResultOnlyKettleOutput( httpResponse, download );
    } else if ( kettleOutputType.equalsIgnoreCase( "SingleCell" ) ) {
      kettleOutput = new SingleCellKettleOutput( httpResponse, download );
    } else {
      kettleOutput = new InferedKettleOutput( httpResponse, download );
    }

    kettleOutput.setOutputStepName( stepName );

    return kettleOutput;
  }

  // TODO this should be in the REST service layer. This method basically "parses" the bloated map.
  @Override
  public void processRequest( Map<String, Map<String, Object>> bloatedMap ) {

    // "Parse" bloated map
    Map<String, Object> request = bloatedMap.get( "request" );
    String stepName = (String) request.get( "stepName" );
    String kettleOutputType = (String) request.get( "kettleOutput" );

    String downloadStr = (String) request.get( "download" );
    boolean download = Boolean.parseBoolean( downloadStr != null ? downloadStr : "false" );

    HttpServletResponse httpResponse = (HttpServletResponse) bloatedMap.get( "path" ).get( "httpresponse" );

    Map<String, String> kettleParameters = KettleElementHelper.getKettleParameters( request );

    this.processRequest( kettleParameters, kettleOutputType , stepName, download, httpResponse );
  }


  // TODO: kettleoutput processing should be in the REST service layer?
  protected void processRequest( Map<String, String> kettleParameters, String outputType, String outputStepName,
                              boolean download, HttpServletResponse httpResponse ) {

    // TODO: do cache selection logic
    KettleResult result = this.processRequestCached( kettleParameters, outputStepName );

    // Infer kettle output type and process result with it
    if ( result.getResult() != null ) {
      IKettleOutput kettleOutput = this.inferResult( outputType, outputStepName, download, httpResponse );
      kettleOutput.processResult( result );
      logger.info( "[ " + result.getResult() + " ]" );
    }
  }

  protected KettleResult processRequestCached( Map<String, String> kettleParameters, String outputStepName ) {
    // If no step name is defined use default step name.
    String stepName = !( outputStepName == null || outputStepName.isEmpty() ) ? outputStepName : DEFAULT_STEP;

    KettleResultKey cacheKey = new KettleResultKey( this.getPluginId(), this.getId(), stepName, kettleParameters );

    KettleResult result = this.getCache().get( cacheKey );
    if ( result != null ) {
      return result;
    } else {
      result = this.processRequestGetResult( kettleParameters, stepName );
      this.getCache().put( cacheKey, result );
    }

    return result;
  }

  protected abstract KettleResult processRequestGetResult( Map<String, String> kettleParams, String outputStepName );

}
