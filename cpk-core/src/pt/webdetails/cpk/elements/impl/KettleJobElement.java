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
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.JobMeta;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.CpkEngine;
import pt.webdetails.cpk.elements.Element;
import pt.webdetails.cpk.elements.impl.kettleOutputs.IKettleOutput;
import pt.webdetails.cpk.elements.impl.kettleOutputs.KettleOutput;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KettleJobElement extends Element {

  private static final String DEFAULT_STEP = "OUTPUT";
  private JobMeta jobMeta = null;
  private Job job = null;

  public KettleJobElement() {
  }

  @Override
  public boolean init( final String id, final String type, final String filePath, boolean adminOnly ) {
    logger.debug( "Creating Kettle Job from '" + filePath + "'" );

    // base init
    if ( !super.init( id, type, filePath, adminOnly ) ) {
      return false;
    }

    // load transformation meta info
    try {
      this.jobMeta = new JobMeta( this.getLocation(), null );
    } catch ( Exception e ) {
      logger.error( "Failed to retrieve '" + this.getLocation() + "'" );
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
    try {
      jobMeta.setParameterValue( name, value );
    } catch ( UnknownParamException e ) {
      // ensure that a variable replaces the parameter if it wasn't defined
      jobMeta.setVariable( name, value );
    }
    logger.debug( "Parameter '" + name + "' = '" + value + "'." );
  }

  private void enforceExecutionParameterSet( Map<String, String> parameterSet ) {
    for ( Map.Entry<String, String> entry : parameterSet.entrySet() ) {
      enforceExecutionParameter( entry.getKey(), entry.getValue() );
    }
  }

  private void enforceExecutionParameter( String name, String value ) {
    try {
      job.setParameterValue( name, value );
    } catch ( UnknownParamException e ) {
      // ensure that a variable replaces the parameter if it wasn't defined
      job.setVariable( name, value );
    }
    logger.debug( "Parameter '" + name + "' = '" + value + "'." );
  }

  private void addExecutionParameterSet( Map<String, String> parameterSet ) {
    for ( Map.Entry<String, String> entry : parameterSet.entrySet() ) {
      addExecutionParameter( entry.getKey(), entry.getValue() );
    }
  }

  private void addExecutionParameter( String name, String value ) {
    try {
      job.setParameterValue( name, value );
      logger.debug( "Parameter '" + name + "' = '" + value + "'." );
    } catch ( UnknownParamException e ) {
      // ignore unknown parameters
    }
  }

  protected boolean getParameterDefaultBoolean( final String name ) {
    try {
      return Boolean.parseBoolean( jobMeta.getParameterDefault( name ) );
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

    kettleOutput.setKettleType( KettleElementHelper.KettleType.JOB );

    return kettleOutput;
  }

  private void execute() {
    logger.info( "Starting job '" + this.getName() + "' [" + this.jobMeta.getName() + "]" );
    long start = System.currentTimeMillis(); 
    this.job = new Job( null, this.jobMeta );
    this.job.start();
    this.job.waitUntilFinished();
    this.job = null;

    long end = System.currentTimeMillis();
    logger.info( "Finished job '" + this.getName() + "' [" + this.jobMeta.getName() + "] in " + ( end - start ) + " ms" );
  }

  @Override public void processRequest( Map<String, Map<String, Object>> bloatedMap ) {
    logger.info( "Starting job '" + this.getName() + "' [" + this.jobMeta.getName() + "]" );
    long start = System.currentTimeMillis();


    // TODO: refactor / optimize results processing
    Result result = null;
    final IKettleOutput kettleOutput = inferResult( bloatedMap );

    this.job = new Job( null, jobMeta );

    // do we need this?
    //job.initializeVariablesFrom( null );
    //job.getJobMeta().setInternalKettleVariables( job );
    //job.copyParametersFrom( job.getJobMeta() );
    //job.copyVariablesFrom( job.getJobMeta() );
    //job.activateParameters();

    // add runtime parameters
    enforceExecutionParameterSet( KettleElementHelper.getUserSessionParameters() );
    addExecutionParameterSet( KettleElementHelper.getUserDefinedParameters( bloatedMap.get( "request" ) ) );

    this.job.start();
    this.job.waitUntilFinished();
    result = this.job.getResult();

    // TODO: refactor as part of kettleOutput variable
    JobEntryResult entryResult = null;
    List<JobEntryResult> jobEntryResultList = job.getJobEntryResults();
    if ( jobEntryResultList.size() > 0 ) {
      for ( int i = 0; i < jobEntryResultList.size(); i++ ) {
        entryResult = jobEntryResultList.get( i );
        if ( entryResult != null ) {
          if ( entryResult.getJobEntryName().equals( kettleOutput.getOutputStepName() ) ) {
            result = entryResult.getResult();
            break;
          }
        }
      }
    }

    // what does this do?
    result.setRows( new ArrayList<RowMetaAndData>() );

    kettleOutput.setResult( result );

    kettleOutput.processResult();

    this.job = null;

    long end = System.currentTimeMillis();
    logger.info( "Finished job '" + this.getName() + "' [" + this.jobMeta.getName() + "] in " + ( end - start ) + " ms" );
    logger.info( "[ " + kettleOutput.getResult() + " ]" );
  }
}
