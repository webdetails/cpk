/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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
import org.pentaho.di.job.JobMeta;
import pt.webdetails.cpk.datasources.DataSource;
import pt.webdetails.cpk.datasources.DataSourceMetadata;
import pt.webdetails.cpk.datasources.KettleElementDefinition;
import pt.webdetails.cpk.datasources.KettleElementMetadata;
import pt.webdetails.cpk.elements.IDataSourceProvider;

import java.util.Collection;
import java.util.Collections;
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

  private Result getResult( Job job ) {
    // Always return the result of the Job and not of a specific jobEntry [SPARKL-66]
    return job.getResult();
  }

  /**
   * Gets the names of the job entries where the result can be fetched to return in a cpk endpoint.
   * @return
   */
  @Override
  protected Collection<String> getOutputNames() {
    // [SPARKL-66] You can only get the result from a job and not a job entry
    return Collections.<String>emptyList();
  }

  @Override
  public KettleResult processRequest( Map<String, String> kettleParameters, String outputJobEntryName ) {
    logger.info( "Starting job '" + this.getName() + "' (" + this.meta.getName() + ")" );
    long start = System.currentTimeMillis();

    // add request parameters
    Collection<String> setParameters = Collections.emptyList();
    if ( kettleParameters != null ) {
      setParameters = KettleElementHelper.setKettleParameterValues( this.meta, kettleParameters );
    }

    // create a new job
    Job job = new Job( null, this.meta );

    // start job thread and wait until it finishes
    job.start();
    job.waitUntilFinished();

    // assemble kettle result
    Result jobResult = this.getResult( job );
    KettleResult result = new KettleResult( jobResult );
    result.setKettleType( KettleResult.KettleType.JOB );

    // clear request parameters
    KettleElementHelper.clearParameters( this.meta, setParameters );

    long end = System.currentTimeMillis();
    this.logger.info( "Finished job '" + this.getName()
      + "' (" + this.meta.getName() + ") in " + ( end - start ) + " ms" );

    return result;
  }
}
