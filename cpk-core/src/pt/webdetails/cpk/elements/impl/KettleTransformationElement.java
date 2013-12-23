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
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.CpkEngine;
import pt.webdetails.cpk.elements.Element;
import pt.webdetails.cpk.elements.impl.kettleOutputs.IKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleOutputs.KettleOutput;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Map;

public class KettleTransformationElement extends Element {

  private static final String DEFAULT_STEP = "OUTPUT";
  private TransMeta transMeta = null;
  private Trans transformation = null;

  public KettleTransformationElement() {
  }

  @Override
  public boolean init( final String id, final String type, final String filePath, boolean adminOnly ) {
    logger.debug( "Creating Kettle Transformation from '" + filePath + "'." );

    // base init
    if ( !super.init( id, type, filePath, adminOnly ) ) {
      return false;
    }

    // load transformation meta info
    try {
      this.transMeta = new TransMeta( this.getLocation() );
    } catch ( Exception e ) {
      logger.error( "Failed to retrieve '" + this.getLocation() + "'." );
      return false;
    }

    // init default parameters
    enforceMetaParameterSet( KettleElementHelper.getDefaultParameters() );

    // execute at start
    if ( getParameterDefaultBoolean( KettleElementHelper.getExecuteAtStartParameter() ) ) {
      execute();
    }

    return true;
  }

  private void enforceMetaParameterSet( Map<String, String> parameterSet ) {
    for ( Map.Entry<String, String> entry : parameterSet.entrySet() ) {
      enforceMetaParameter( entry.getKey(), entry.getValue() );
    }
  }

  private void enforceMetaParameter( String name, String value ) {
    logger.debug( "Parameter '" + name + "' = '" + value + "'" );
    try {
      transMeta.setParameterValue( name, value );
    } catch ( UnknownParamException e ) {
      // ensure that a variable replaces the parameter if it wasn't defined
      logger.debug( "Didn't find parameter: using a replacement variable" );
    }
    transMeta.setVariable( name, value );
  }

  private void enforceExecutionParameterSet( Map<String, String> parameterSet ) {
    for ( Map.Entry<String, String> entry : parameterSet.entrySet() ) {
      enforceExecutionParameter( entry.getKey(), entry.getValue() );
    }
  }

  private void enforceExecutionParameter( String name, String value ) {
    logger.debug( "Parameter '" + name + "' = '" + value + "'" );
    try {
      transformation.setParameterValue( name, value );
    } catch ( UnknownParamException e ) {
      // ensure that a variable replaces the parameter if it wasn't defined
      logger.debug( "Didn't find parameter: using a replacement variable" );
    }
    transformation.setVariable( name, value );
  }

  private void addMetaParameterSet( Map<String, String> parameterSet ) {
    for ( Map.Entry<String, String> entry : parameterSet.entrySet() ) {
      addMetaParameter( entry.getKey(), entry.getValue() );
    }
  }

  private void addMetaParameter( String name, String value ) {
    try {
      transMeta.setParameterValue( name, value );
      logger.debug( "Parameter '" + name + "' = '" + value + "'." );
    } catch ( UnknownParamException e ) {
      // ignore unknown parameters
    }
  }

  private boolean getParameterDefaultBoolean( String name ) {
    try {
      return Boolean.parseBoolean( transMeta.getParameterDefault( name ) );
    } catch ( UnknownParamException e ) {
      logger.debug( "Unknown parameter '" + name + "'. Using default value 'false'." );
      return false;
    }
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

  private void execute() {
    logger.info( "Starting transformation '" + this.getName() + "' [" + this.transMeta.getName() + "]" );
    long start = System.currentTimeMillis();

    try {
      // create a new transformation using the meta info
      transformation = new Trans( transMeta );

      transformation.prepareExecution( null ); // get the step threads after this line
      StepInterface step = transformation.findRunThread( DEFAULT_STEP );
      if ( step != null ) {
        transformation.startThreads(); // all the operations to get step names need to be placed above this line
        transformation.waitUntilFinished();
      } else {
        logger.error( "Couldn't find step '" + DEFAULT_STEP + "'." );
      }
    } catch ( KettleException e ) {
      logger.debug( e );
    }

    // clear previous results
    transformation = null;
    transMeta.setResultRows( new ArrayList<RowMetaAndData>() );
    transMeta.setResultFiles( new ArrayList<ResultFile>() );

    long end = System.currentTimeMillis();
    logger.info( "Finished transformation '" + this.getName() + "' [" + this.transMeta.getName() + "] in " + ( end - start ) + " ms" );
  }

  @Override
  public void processRequest( Map<String, Map<String, Object>> bloatedMap ) {
    logger.info( "Starting transformation '" + this.getName() + "' [" + this.transMeta.getName() + "]" );
    long start = System.currentTimeMillis();

    // TODO: refactor / optimize results processing
    Result result = null;
    final IKettleOutput kettleOutput = inferResult( bloatedMap );

    try {
      transMeta.setResultRows( new ArrayList<RowMetaAndData>() );
      transMeta.setResultFiles( new ArrayList<ResultFile>() );

      // create a new transformation using the meta info
      transformation = new Trans( transMeta );

      transformation.initializeVariablesFrom( null );
      transformation.getTransMeta().setInternalKettleVariables( transformation );

      // add runtime parameters
      enforceMetaParameterSet( KettleElementHelper.getDefaultParameters() );
      enforceExecutionParameterSet( KettleElementHelper.getUserSessionParameters() );
      addMetaParameterSet( KettleElementHelper.getUserDefinedParameters( bloatedMap.get( "request" ) ) );

      transformation.copyParametersFrom( transMeta );
      transformation.copyVariablesFrom( transMeta );
      transformation.activateParameters();

      transformation.prepareExecution( null ); // get the step threads after this line

      StepInterface step = transformation.findRunThread( kettleOutput.getOutputStepName() );

      if ( step != null ) {

        // TODO: do it inside the kettle output
        if ( kettleOutput.needsRowListener() ) {

          step.addRowListener( new RowAdapter() {
            @Override
            public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
              kettleOutput.storeRow( row, rowMeta );
            }
          } );

          transformation.startThreads(); // all the operations to get step names need to be placed above this line
          transformation.waitUntilFinished();

          result = step.getTrans().getResult();
        }
      } else {
        logger.error( "Couldn't find step '" + kettleOutput.getOutputStepName() + "'." );
      }

      kettleOutput.setResult( result );
    } catch ( KettleException e ) {
      logger.debug( e );
    }

    kettleOutput.processResult();

    // clear previous results
    transformation = null;

    long end = System.currentTimeMillis();
    logger.info( "Finished transformation '" + this.getName() + "' [" + this.transMeta.getName() + "] in " + ( end - start ) + " ms" );
    logger.info( "[ " + kettleOutput.getResult() + " ]" );
  }
}
