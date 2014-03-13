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
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.JobMeta;
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
import java.util.List;
import java.util.Map;

public class KettleJobElement extends Element {

  private static final String DEFAULT_STEP = "OUTPUT";
  private JobMeta jobMeta = null;

  public KettleJobElement() {
  }

  @Override
  public boolean init( final String id, final String type, final String filePath, boolean adminOnly ) {
    logger.debug( "Creating Kettle Job from '" + filePath + "'" );

    // call base init
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

    // add base parameters to ensure they exist
    KettleElementHelper.addBaseParameters( this.jobMeta );

    // execute at start?
    if ( KettleElementHelper.isExecuteAtStart( this.jobMeta ) ) {
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

    kettleOutput.setKettleType( KettleElementHelper.KettleType.JOB );

    return kettleOutput;
  }
  private void processResult( Job job, IKettleOutput output ) {
    // TODO: refactor / optimize results processing
    Result result = job.getResult();

    // TODO: refactor as part of kettleOutput variable
    JobEntryResult entryResult = null;
    List<JobEntryResult> jobEntryResultList = job.getJobEntryResults();
    if ( jobEntryResultList.size() > 0 ) {
      for ( int i = 0; i < jobEntryResultList.size(); i++ ) {
        entryResult = jobEntryResultList.get( i );
        if ( entryResult != null ) {
          if ( entryResult.getJobEntryName().equals( output.getOutputStepName() ) ) {
            result = entryResult.getResult();
            break;
          }
        }
      }
    }

    // what is this for?
    result.setRows( new ArrayList<RowMetaAndData>() );

    output.setResult( result );

    output.processResult();
    logger.info( "[ " + output.getResult() + " ]" );
  }

  @Override public void processRequest( Map<String, Map<String, Object>> bloatedMap ) {
    logger.info( "Starting job '" + this.getName() + "' (" + this.jobMeta.getName() + ")" );
    long start = System.currentTimeMillis();

    // update parameters
    KettleElementHelper.updateParameters( this.jobMeta );

    // add request parameters
    Collection<String> requestParameters = null;
    if ( bloatedMap != null ) {
      requestParameters = KettleElementHelper.addRequestParameters( this.jobMeta, bloatedMap.get( "request" ) );
    }

    // create a new job
    Job job = new Job( null, jobMeta );

    // nothing of this is needed:
    //job.initializeVariablesFrom( null );
    //job.getJobMeta().setInternalKettleVariables( job );
    //job.copyParametersFrom( job.getJobMeta() );
    //job.copyVariablesFrom( job.getJobMeta() );
    //job.activateParameters();

    // start job thread and wait until it finishes
    job.start();
    job.waitUntilFinished();

    // process result
    if ( bloatedMap != null ) {
      processResult( job, inferResult( bloatedMap ) );
    }

    // clear request parameters
    KettleElementHelper.clearRequestParameters( jobMeta, requestParameters );

    long end = System.currentTimeMillis();
    logger.info( "Finished job '" + this.getName() + "' (" + this.jobMeta.getName() + ") in " + ( end - start ) + " ms" );
  }

  public static void execute( String kettleJobPath ) {
    try {
      // load transformation meta info
      JobMeta jobMeta = new JobMeta( kettleJobPath, null );
      // add base parameters to ensure they exist
      KettleElementHelper.addBaseParameters( jobMeta );
      // update parameters
      KettleElementHelper.updateParameters( jobMeta );
      // create a new job
      Job job = new Job( null, jobMeta );
      // start job thread and wait until it finishes
      job.start();
      job.waitUntilFinished();
    } catch ( Exception e ) {
      // do nothing
    }
  }
}
