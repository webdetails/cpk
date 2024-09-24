/*!
* Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company.  All rights reserved.
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

import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import pt.webdetails.cpk.datasources.DataSource;
import pt.webdetails.cpk.datasources.DataSourceMetadata;
import pt.webdetails.cpk.datasources.KettleElementDefinition;
import pt.webdetails.cpk.datasources.KettleElementMetadata;
import pt.webdetails.cpk.elements.IDataSourceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KettleTransformationElement extends KettleElement<TransMeta> implements IDataSourceProvider {

  public KettleTransformationElement() {
  }

  @Override
  protected TransMeta loadMeta( String filePath ) {
    // load transformation meta info
    try {
      return new TransMeta( this.getLocation() );
    } catch ( Exception e ) {
      return null;
    }
  }

  protected DataSourceMetadata getMetadata() {
    Iterable<StepMeta> steps = this.meta.getSteps();
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
  protected KettleResult processRequest( Map<String, String> kettleParameters, String outputStepName ) {
    logger.info( "Starting transformation '" + this.getName() + "' (" + this.meta.getName() + ")" );
    long start = System.currentTimeMillis();

    KettleResult result = null;
    final List<RowMetaAndData> rows = new ArrayList<RowMetaAndData>(  );

    try {
      // Clone meta so that parameters and any other state are isolated.
      TransMeta executionMeta = (TransMeta) this.meta.realClone( false );

      // add parameters
      KettleElementHelper.setKettleParameterValues( executionMeta, kettleParameters );

      // create a new transformation
      Trans transformation = new Trans( executionMeta );
      transformation.prepareExecution( null ); // get the step threads after this line

      // get step to listen to written rows
      StepInterface step = this.getRunThread(transformation, outputStepName );
      if ( step != null ) {
        // Store the written rows for later processing
        step.addRowListener( new RowAdapter() {
          @Override
          public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] data ) throws KettleStepException {
            rows.add( new RowMetaAndData( rowMeta, data ) );
          }
        } );
      } else {
        logger.error( "Couldn't find step '" + outputStepName
          + "' nor default output step '" + this.getDefaultOutputName() + "'." );
      }

      // start transformation threads and wait until they finish
      transformation.startThreads(); // all the operations to get step names need to be placed above this line
      transformation.waitUntilFinished();

      // assemble kettle result
      Result transformationResult = transformation.getResult();
      transformationResult.setRows( rows );
      result = new KettleResult( transformationResult );
      result.setKettleType( KettleResult.KettleType.TRANSFORMATION );
    } catch ( KettleException e ) {
      logger.debug( "KETTLE EXCEPTION: " + e, e );
    }

    long end = System.currentTimeMillis();
    logger.info( "Finished transformation '" + this.getName()
      + "' (" + this.meta.getName() + ") in " + ( end - start ) + " ms" );

    return result;
  }

  /**
   *
   * @param transformation
   * @param stepName
   * @return
   */
  protected StepInterface getRunThread( Trans transformation, String stepName ) {
    Collection<String> outputStepNames = this.getOutputNames();
    StepInterface step;

    // if stepName is not a valid output step name, then fallback to default value
    if ( outputStepNames.contains( stepName ) ) {
      step = transformation.findRunThread( stepName );
    } else {
      step = transformation.findRunThread( this.getDefaultOutputName() );
    }

    return step;
  }

  /**
   * Gets the names of the transformation steps which can be used for output.
   * @return
   */
  @Override
  protected Collection<String> getOutputNames() {
    List<String> validOutputStepNames = new ArrayList<String>();
    for ( String name : this.meta.getStepNames() ) {
      if ( this.isValidOutputName( name ) ) {
        validOutputStepNames.add( name );
      }
    }
    return validOutputStepNames;
  }
}
