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
import pt.webdetails.cpk.datasources.DataSource;
import pt.webdetails.cpk.datasources.DataSourceDefinition;
import pt.webdetails.cpk.datasources.KettleElementMetadata;
import pt.webdetails.cpk.elements.IDataSourceProvider;
import pt.webdetails.cpk.elements.IMetadata;
import pt.webdetails.cpk.elements.impl.kettleOutputs.IKettleOutput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class KettleJobElement extends KettleElement implements IDataSourceProvider {

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

  protected IMetadata getMetadata() {
    return new KettleElementMetadata()
      .setEndpointName( this.getName() );
  }

  public DataSource getDataSource() {
    return new DataSource()
      .setMetadata( this.getMetadata() )
      .setDefinition( new DataSourceDefinition() );
  }

  @Override
  protected IKettleOutput inferResult( Map<String, Map<String, Object>> bloatedMap ) {
    IKettleOutput kettleOutput = super.inferResult( bloatedMap );
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
    this.logger.info( "Finished job '" + this.getName()
      + "' (" + this.jobMeta.getName() + ") in " + ( end - start ) + " ms" );
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
