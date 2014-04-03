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
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.JobMeta;
import pt.webdetails.cpk.datasources.DataSource;
import pt.webdetails.cpk.datasources.DataSourceMetadata;
import pt.webdetails.cpk.datasources.KettleElementDefinition;
import pt.webdetails.cpk.datasources.KettleElementMetadata;
import pt.webdetails.cpk.elements.IDataSourceProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KettleJobElement extends KettleElement<JobMeta> implements IDataSourceProvider {

  public KettleJobElement() {
  }

  @Override
  protected JobMeta loadMeta( String filePath ) {
    // load transformation meta info
    try {
      return new JobMeta( this.getLocation(), null );
    } catch ( Exception e ) {
      return null;
    }
  }

  protected DataSourceMetadata getMetadata() {
    return new KettleElementMetadata()
      .setEndpointName( this.getName() );
  }

  public DataSource getDataSource() {
    return new DataSource()
      .setMetadata( this.getMetadata() )
      .setDefinition( new KettleElementDefinition() );
  }


  private Result getResult( Job job, String outputStepName ) {
    // TODO: refactor / optimize results processing
    Result result = job.getResult();

    // TODO: refactor as part of kettleOutput variable
    JobEntryResult entryResult = null;
    List<JobEntryResult> jobEntryResultList = job.getJobEntryResults();
    if ( jobEntryResultList.size() > 0 ) {
      for ( int i = 0; i < jobEntryResultList.size(); i++ ) {
        entryResult = jobEntryResultList.get( i );
        if ( entryResult != null ) {
          if ( entryResult.getJobEntryName().equals( outputStepName ) ) {
            result = entryResult.getResult();
            break;
          }
        }
      }
    }
    return result;
  }


  @Override
  public KettleResult processRequestGetResult( Map<String, String> kettleParameters, String outputStepName ) {
    logger.info( "Starting job '" + this.getName() + "' (" + this.meta.getName() + ")" );
    long start = System.currentTimeMillis();

    // If no step name is defined use default step name.
    String stepName = !( outputStepName == null || outputStepName.isEmpty() ) ? outputStepName : DEFAULT_STEP;

    // update parameters
    KettleElementHelper.updateParameters( this.meta );

    // add request parameters
    Collection<String> addedParameters = Collections.emptyList();
    if ( kettleParameters != null ) {
      addedParameters = KettleElementHelper.addKettleParameters( this.meta, kettleParameters );
    }

    // create a new job
    Job job = new Job( null, this.meta );

    // start job thread and wait until it finishes
    job.start();
    job.waitUntilFinished();

    Result jobResult = this.getResult( job, stepName );
    KettleResult result = new KettleResult( jobResult );
    result.setKettleType( KettleElementHelper.KettleType.JOB );

    // clear request parameters
    KettleElementHelper.clearParameters( this.meta, addedParameters );

    long end = System.currentTimeMillis();
    this.logger.info( "Finished job '" + this.getName()
      + "' (" + this.meta.getName() + ") in " + ( end - start ) + " ms" );

    return result;
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
