/*!
* Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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

package pt.webdetails.cpk.elements.impl.kettleoutputs;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.row.RowMetaInterface;
import pt.webdetails.cpk.elements.impl.KettleElementHelper;

import javax.servlet.http.HttpServletResponse;

public class InferedKettleOutput extends KettleOutput {

  private JsonKettleOutput jsonKettleOutput;
  private ResultFilesKettleOutput resultFilesKettleOutput;
  private ResultOnlyKettleOutput resultOnlyKettleOutput;
  private SingleCellKettleOutput singleCellKettleOutput;

  public InferedKettleOutput( HttpServletResponse response, boolean download ) {
    super( response, download );

    this.jsonKettleOutput = new JsonKettleOutput( response, download );
    this.resultFilesKettleOutput = new ResultFilesKettleOutput( response, download );
    this.resultOnlyKettleOutput = new ResultOnlyKettleOutput( response, download );
    this.singleCellKettleOutput = new SingleCellKettleOutput( response, download );
  }

  @Override
  public void processResult() {

    logger.debug( "Process Infered" );

        /*
         *  If nothing specified, the behavior will be:
         *  Jobs and Transformations with result filenames: ResultFiles
         *   Without filenames:
         *    * Jobs: ResultOnly
         *    * Transformations:
         *      * Just one cell: SingleCell
         *      * Regular resultset: Json
         */
    Result result = getResult();

    if ( result.getResultFilesList().size() > 0 ) {
      this.resultFilesKettleOutput.processResult();

    } else if ( getKettleType() == KettleElementHelper.KettleType.JOB ) {
      this.resultOnlyKettleOutput.processResult();

    } else if ( getRows().size() == 1 && getRowMeta().getValueMetaList().size() == 1 ) {
      this.singleCellKettleOutput.processResult();

    } else {
      this.jsonKettleOutput.processResult();
    }
  }

  @Override
  public boolean needsRowListener() { return true; }

  @Override
  public void storeRow( Object[] row, RowMetaInterface rowMeta ) {
    super.storeRow( row, rowMeta );
    // add rows to kettle outputs that required rowListener
    this.jsonKettleOutput.storeRow( row, rowMeta );
    this.singleCellKettleOutput.storeRow( row, rowMeta );
  }

  @Override
  public void setResult( Result result ) {
    super.setResult( result );
    this.jsonKettleOutput.setResult( result );
    this.resultFilesKettleOutput.setResult( result );
    this.resultOnlyKettleOutput.setResult( result );
    this.singleCellKettleOutput.setResult( result );
  }

  public void setKettleType( KettleElementHelper.KettleType kettleType ) {
    super.setKettleType( kettleType );
    this.jsonKettleOutput.setKettleType( kettleType );
    this.resultFilesKettleOutput.setKettleType( kettleType );
    this.singleCellKettleOutput.setKettleType( kettleType );
    this.resultOnlyKettleOutput.setKettleType( kettleType );
  }

  public void setOutputStepName( String stepName ) {
    super.setOutputStepName( stepName );
    this.jsonKettleOutput.setOutputStepName( stepName );
    this.resultFilesKettleOutput.setOutputStepName( stepName );
    this.singleCellKettleOutput.setOutputStepName( stepName );
    this.resultOnlyKettleOutput.setOutputStepName( stepName );
  }

  public void setRowMeta( RowMetaInterface rowMeta ) {
    super.setRowMeta( rowMeta );
    this.jsonKettleOutput.setRowMeta( rowMeta );
    this.resultFilesKettleOutput.setRowMeta( rowMeta );
    this.singleCellKettleOutput.setRowMeta( rowMeta );
    this.resultOnlyKettleOutput.setRowMeta( rowMeta );
  }


}
