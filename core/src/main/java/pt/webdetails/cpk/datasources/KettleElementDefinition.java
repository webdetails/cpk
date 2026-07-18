/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package pt.webdetails.cpk.datasources;


public class KettleElementDefinition extends DataSourceDefinition {

  protected Parameter outputStepName;
  protected Parameter outputType;

  public KettleElementDefinition() {
    this.outputStepName = new Parameter( "STRING", "ATTRIB" );
    this.outputType = new Parameter( "STRING", "ATTRIB" );

    this.dataAccessParameters.put( "stepName", this.outputStepName );
    this.dataAccessParameters.put( "kettleOutput", this.outputType );
  }

}
