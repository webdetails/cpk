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

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import pt.webdetails.cpk.cache.ICache;
import pt.webdetails.cpk.datasources.DataSource;
import pt.webdetails.cpk.datasources.DataSourceMetadata;
import pt.webdetails.cpk.datasources.KettleElementDefinition;
import pt.webdetails.cpk.datasources.KettleElementMetadata;
import pt.webdetails.cpk.elements.IDataSourceProvider;
import pt.webdetails.cpk.elements.impl.kettleoutputs.IKettleOutput;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class KettleTransformationElement extends KettleElement implements IDataSourceProvider {

  private TransMeta transMeta = null;

  public KettleTransformationElement() {
  }

  @Override
  public boolean init( final String id, final String type, final String filePath, boolean adminOnly ) {
    logger.debug( "Creating Kettle Transformation from '" + filePath + "'" );

    // base init
    if ( !super.init( id, type, filePath, adminOnly ) ) {
      return false;
    }

    // load transformation meta info
    try {
      this.transMeta = new TransMeta( this.getLocation() );
    } catch ( Exception e ) {
      logger.error( "Failed to retrieve '" + this.getLocation() + "'" );
      return false;
    }

    // add base parameters to ensure they exist
    KettleElementHelper.addBaseParameters( this.transMeta );

    // execute at start?
    if ( KettleElementHelper.isExecuteAtStart( this.transMeta ) ) {
      KettleTransformationElement.execute( filePath );
    }

    // init was successful
    return true;
  }

  protected DataSourceMetadata getMetadata() {
    Iterable<StepMeta> steps = this.transMeta.getSteps();
    Collection<String> stepNames = new ArrayList<String>();
    for ( StepMeta step : steps ) {
      stepNames.add( step.getName() );
    }

    return new KettleElementMetadata()
      .setKettleStepNames( Collections.unmodifiableCollection( stepNames ) )
      .setEndpointName( this.getName() );
  }

  public DataSource getDataSource() {
    DataSource dataSource = new DataSource();
    dataSource.setDefinition( new KettleElementDefinition() )
              .setMetadata( this.getMetadata() );

    return dataSource;
  }


  @Override
  protected IKettleOutput inferResult( Map<String, Map<String, Object>> bloatedMap ) {
    IKettleOutput kettleOutput = super.inferResult( bloatedMap );
    kettleOutput.setKettleType( KettleElementHelper.KettleType.TRANSFORMATION );
    return kettleOutput;
  }

  // TODO this should be in the REST service layer
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
  public void processRequest( Map<String, String> kettleParams, String outputType, String outputStepName,
                              boolean download, HttpServletResponse httpResponse ) {

    KettleResult result = this.processRequestGetResult( kettleParams, outputStepName );

    // Infer kettle output type and process result with it
    if ( result.getResult() != null ) {
      IKettleOutput kettleOutput = this.inferResult( outputType, outputStepName, download, httpResponse );
      kettleOutput.processResult( result );
      logger.info( "[ " + result.getResult() + " ]" );
    }
  }

  // TODO: this method should replace processRequest eventually
  private KettleResult processRequestGetResult( Map<String, String> kettleParameters, String outputStepName ) {
    logger.info( "Starting transformation '" + this.getName() + "' (" + this.transMeta.getName() + ")" );
    long start = System.currentTimeMillis();

    // If no step name is defined use default step name.
    String stepName = !( outputStepName == null || outputStepName.isEmpty() ) ? outputStepName : DEFAULT_STEP;

    KettleResultKey cacheKey = new KettleResultKey( this.getId(), stepName, kettleParameters );

    try {
      KettleResult cachedResult = this.getCache().get( cacheKey );
      if ( cachedResult != null ) {
        return cachedResult;
      }
    } catch ( Exception e ) {
      this.logger.error( "Error getting cache for kettle transform with id " + this.getId(), e );
    }

    final KettleResult result = new KettleResult();

    try {
      // clean?
      this.transMeta.setResultRows( new ArrayList<RowMetaAndData>() );
      this.transMeta.setResultFiles( new ArrayList<ResultFile>() );

      // update parameters
      KettleElementHelper.updateParameters( this.transMeta );

      // add request parameters
      Collection<String> addedParameters = null;
      if ( kettleParameters != null ) {
        addedParameters = KettleElementHelper.addKettleParameters( this.transMeta, kettleParameters );
      }

      // create a new transformation
      Trans transformation = new Trans( this.transMeta );

      transformation.prepareExecution( null ); // get the step threads after this line

      StepInterface step = transformation.findRunThread( stepName );
      if ( step != null ) {
        // Store the written rows for later processing
        step.addRowListener( new RowAdapter() {
          @Override
          public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
            result.getRows().add( new KettleResult.Row( rowMeta, row ) );
          }
        } );

        // start transformation threads and wait until they finish
        transformation.startThreads(); // all the operations to get step names need to be placed above this line
        transformation.waitUntilFinished();

        result.setResult( transformation.getResult() );

        try {
          this.getCache().put( cacheKey, result );
        } catch ( Exception e ) {
          this.logger.error( "Error getting cache for kettle transform with id " + this.getId(), e );
        }

      } else {
        logger.error( "Couldn't find step '" + stepName + "'" );
      }

      // clear request parameters
      KettleElementHelper.clearParameters( transMeta, addedParameters );

    } catch ( KettleException e ) {
      logger.debug( "KETTLE EXCEPTION: " + e );
    }

    long end = System.currentTimeMillis();
    logger.info( "Finished transformation '" + this.getName()
      + "' (" + this.transMeta.getName() + ") in " + ( end - start ) + " ms" );

    return result;
  }


  public static void execute( String kettleTransformationPath ) {
    try {
      // load transformation meta info
      TransMeta transMeta = new TransMeta( kettleTransformationPath );
      // add base parameters to ensure they exist
      KettleElementHelper.addBaseParameters( transMeta );
      // update parameters
      KettleElementHelper.updateParameters( transMeta );
      // create a new transformation
      Trans transformation = new Trans( transMeta );
      // prepare execution
      transformation.prepareExecution( null );
      StepInterface step = transformation.findRunThread( "OUTPUT" );
      if ( step != null ) {
        // start transformation threads and wait until they finish
        transformation.startThreads();
        transformation.waitUntilFinished();
      }
    } catch ( KettleException e ) {
      // do nothing?
    }
  }
}
