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


package pt.webdetails.cpk.elements.impl.kettleoutputs;

import pt.webdetails.cpk.elements.impl.KettleResult;


public class InferedKettleOutput extends KettleOutput {

  /**
   * Chooses one processing method according to the information in the Result.
   * Results from a job / transformation with filenames: use ResultFilesKettleOuput
   *   Without filenames:
   *    * If it is a Job result:
   *      * If result has rows: use JsonKettleOutput
   *      * Else: use ResultOnlyKettleOutput
   *    * If it is a Transformation result:
   *      * If result has just one cell: us SingleCellKettleOutput
   * Otherwise: use JsonKettleOutput
   * @param result The kettle job/transformation Result to process.
   */
  @Override
  public void processResult( KettleResult result ) {
    logger.debug( "Process Inferred" );

    KettleOutput kettleOutput;
    if ( result.getFiles().size() > 0 ) {
      kettleOutput = new ResultFilesKettleOutput();

    } else if ( result.getKettleType() == KettleResult.KettleType.JOB ) {
      if ( !result.getRows().isEmpty() ) {
        kettleOutput = new JsonKettleOutput();
      }
      else {
        kettleOutput = new ResultOnlyKettleOutput();
      }

    } else if ( result.getRows().size() == 1
      && result.getRows().get( 0 ).getRowMeta().getValueMetaList().size() == 1 ) {
      kettleOutput = new SingleCellKettleOutput();

    } else {
      kettleOutput = new JsonKettleOutput();
    }

    kettleOutput
      .setResponse( this.getResponse() )
      .setConfiguration( this.getConfiguration() );
    kettleOutput.processResult( result );
  }
}
