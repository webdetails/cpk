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

import pt.webdetails.cpk.elements.impl.KettleElementHelper;
import pt.webdetails.cpk.elements.impl.KettleResult;

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
  public void processResult( KettleResult result ) {
    super.processResult( result );

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

    if ( result.getFiles().size() > 0 ) {
      this.resultFilesKettleOutput.processResult( result );

    } else if ( getKettleType() == KettleElementHelper.KettleType.JOB ) {
      this.resultOnlyKettleOutput.processResult( result );

    } else if ( result.getRows().size() == 1
      && result.getRows().get( 0 ).getRowMeta().getValueMetaList().size() == 1 ) {
      this.singleCellKettleOutput.processResult( result );

    } else {
      this.jsonKettleOutput.processResult( result );
    }
  }

  @Override
  public void setKettleType( KettleElementHelper.KettleType kettleType ) {
    super.setKettleType( kettleType );
    this.jsonKettleOutput.setKettleType( kettleType );
    this.resultFilesKettleOutput.setKettleType( kettleType );
    this.singleCellKettleOutput.setKettleType( kettleType );
    this.resultOnlyKettleOutput.setKettleType( kettleType );
  }

  @Override
  public void setOutputStepName( String stepName ) {
    super.setOutputStepName( stepName );
    this.jsonKettleOutput.setOutputStepName( stepName );
    this.resultFilesKettleOutput.setOutputStepName( stepName );
    this.singleCellKettleOutput.setOutputStepName( stepName );
    this.resultOnlyKettleOutput.setOutputStepName( stepName );
  }

}
