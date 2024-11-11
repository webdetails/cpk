/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cpk.datasources;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KettleElementMetadata extends CpkDataSourceMetadata {

  private Iterable<String> kettleStepNames;

  @JsonProperty( "kettleStepNames" )
  public Iterable<String> getKettleStepNames() { return this.kettleStepNames; }

  public KettleElementMetadata setKettleStepNames( Iterable<String> kettleStepNames ) {
    this.kettleStepNames = kettleStepNames;
    return this;
  }

}
