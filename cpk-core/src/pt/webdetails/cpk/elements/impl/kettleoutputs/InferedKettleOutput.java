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

import pt.webdetails.cpk.elements.impl.KettleResult;

import javax.servlet.http.HttpServletResponse;

public class InferedKettleOutput extends KettleOutput {

  public InferedKettleOutput( HttpServletResponse response, boolean download ) {
    super( response, download );
  }

  /**
   * Chooses one processing method according to the information in the Result.
   * Results from a job / transformation with filenames: use ResultFilesKettleOuput
   *   Without filenames:
   *    * If it is a Job result: use ResultOnlyKettleOutput
   *    * If it is a Transformation result:
   *      * If result has just one cell: us SingleCellKettleOutput
   * Otherwise: use JsonKettleOutput
   * @param result The kettle job/transformation Result to process.
   */
  @Override
  public void processResult( KettleResult result ) {
    logger.debug( "Process Inferred" );

    IKettleOutput kettleOutput;
    if ( result.getFiles().size() > 0 ) {
      kettleOutput = new ResultFilesKettleOutput( this.getResponse(), this.getDownload() );

    } else if ( result.getKettleType() == KettleResult.KettleType.JOB ) {
      kettleOutput = new ResultOnlyKettleOutput( this.getResponse(), this.getDownload() );

    } else if ( result.getRows().size() == 1
      && result.getRows().get( 0 ).getRowMeta().getValueMetaList().size() == 1 ) {
      kettleOutput = new SingleCellKettleOutput( this.getResponse(), this.getDownload() );

    } else {
      kettleOutput = new JsonKettleOutput( this.getResponse(), this.getDownload() );
    }

    kettleOutput.processResult( result );
  }
}
