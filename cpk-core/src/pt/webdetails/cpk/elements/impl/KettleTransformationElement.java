/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
*                
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed unde r the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cpk.elements.impl;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.CpkEngine;
import pt.webdetails.cpk.datasources.CpkDataSourceMetadata;
import pt.webdetails.cpk.elements.Element;
import pt.webdetails.cpk.elements.IMetadata;
import pt.webdetails.cpk.elements.impl.kettleOutputs.IKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleOutputs.KettleOutput;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class KettleTransformationElement extends Element {

  private static final String DEFAULT_STEP = "OUTPUT";
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
      processRequest( null );
    }

    // init was successful
    return true;
  }

  public IMetadata getMetadata()
  {
    return new CpkDataSourceMetadata().setEndpointName( this.getName() );
  }

  // TODO: refactor / see what's common between transformations and jobs
  private IKettleOutput inferResult( Map<String, Map<String, Object>> bloatedMap ) {

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
         *  If nothing specified, the behavior will be:
         *  * Jobs and Transformations with result filenames: ResultFiles
         *  * Without filenames:
         *      * Jobs: ResultOnly
         *      * Transformations:
         *          * Just one cell: SingleCell
         *          * Regular resultset: Json
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

    //These conditions will treat the different types of kettle operations

    IKettleOutput kettleOutput;
    String haveOutput = (String) bloatedMap.get( "request" ).get( "kettleOutput" );
    String clazz = ( haveOutput != null ? haveOutput : "Infered" ) + "KettleOutput";

    IPluginUtils pluginUtils = CpkEngine.getInstance().getEnvironment().getPluginUtils();

    try {
      // Get defined kettleOutput class name


      Constructor constructor = Class.forName( "pt.webdetails.cpk.elements.impl.kettleOutputs." + clazz )
        .getConstructor( Map.class, IPluginUtils.class );
      kettleOutput = (IKettleOutput) constructor.newInstance( bloatedMap, pluginUtils );

    } catch ( Exception ex ) {
      logger.error( "Error initializing Kettle output type " + clazz + ", reverting to KettleOutput: " + Util
        .getExceptionDescription( ex ) );
      kettleOutput = new KettleOutput( bloatedMap, pluginUtils );
    }

    // Are we specifying a stepname?
    String hasStepName = (String) bloatedMap.get( "request" ).get( "stepName" );
    kettleOutput.setOutputStepName( hasStepName != null ? hasStepName : DEFAULT_STEP );

    kettleOutput.setKettleType( KettleElementHelper.KettleType.TRANSFORMATION );

    return kettleOutput;
  }
  private void prepareOutput( StepInterface step, final IKettleOutput output ) {
    step.addRowListener( new RowAdapter() {
      @Override
      public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
        output.storeRow( row, rowMeta );
      }
    } );
  }
  private void processResult( StepInterface step, IKettleOutput output ) {
    Result result = step.getTrans().getResult();
    output.setResult( result );
    output.processResult();
    logger.info( "[ " + output.getResult() + " ]" );
  }


  @Override
  public void processRequest( Map<String, Map<String, Object>> bloatedMap ) {
    logger.info( "Starting transformation '" + this.getName() + "' (" + this.transMeta.getName() + ")" );
    long start = System.currentTimeMillis();

    try {
      // clean?
      transMeta.setResultRows( new ArrayList<RowMetaAndData>() );
      transMeta.setResultFiles( new ArrayList<ResultFile>() );

      // update parameters
      KettleElementHelper.updateParameters( this.transMeta );

      // add request parameters
      Collection<String> requestParameters = null;
      if ( bloatedMap != null ) {
        requestParameters = KettleElementHelper.addRequestParameters( this.transMeta, bloatedMap.get( "request" ) );
      }

      // create a new transformation
      Trans transformation = new Trans( transMeta );

      // not necessary
      //transformation.copyParametersFrom( transMeta );
      //transformation.initializeVariablesFrom( null );
      //transformation.getTransMeta().setInternalKettleVariables( transformation );
      //transformation.copyVariablesFrom( transMeta );

      // prepare execution does this!
      // transformation.activateParameters();

      transformation.prepareExecution( null ); // get the step threads after this line

      String stepName = DEFAULT_STEP;
      if ( bloatedMap != null ) {
        String requestStepName = (String) bloatedMap.get( "request" ).get( "stepName" );
        if ( requestStepName != null ) {
          stepName = requestStepName;
        }
      }

      StepInterface step = transformation.findRunThread( stepName );

      if ( step != null ) {

        if ( bloatedMap != null ) {
          IKettleOutput kettleOutput = inferResult( bloatedMap );
          if ( kettleOutput.needsRowListener() ) {
            prepareOutput( step, kettleOutput );

            // start transformation threads and wait until they finish
            transformation.startThreads(); // all the operations to get step names need to be placed above this line
            transformation.waitUntilFinished();

            processResult( step, kettleOutput );
          }
        } else {
          // start transformation threads and wait until they finish
          transformation.startThreads(); // all the operations to get step names need to be placed above this line
          transformation.waitUntilFinished();
        }
      } else {
        logger.error( "Couldn't find step '" + stepName + "'" );
      }

      // clear request parameters
      KettleElementHelper.clearRequestParameters( transMeta, requestParameters );

    } catch ( KettleException e ) {
      logger.debug( "KETTLE EXCEPTION: " + e );
    }

    long end = System.currentTimeMillis();
    logger.info( "Finished transformation '" + this.getName() + "' (" + this.transMeta.getName() + ") in " + ( end - start ) + " ms" );
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
