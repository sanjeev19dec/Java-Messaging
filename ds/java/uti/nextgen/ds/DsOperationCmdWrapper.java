/*
 * $Id: $
 *
 * $RCSfile: $ $Revision: $ $Date: $
 *
 * Description:
 *
 */

package uti.nextgen.ds;

import java.io.*;

/**
 * Command line utility for DsOperation.
 * <p>
 * Takes two files as paramaters: DsOperation config file and a file containing
 * a dsoperation instance document.
 *
 * @author  Sanjeev Sharma
 */

public class DsOperationCmdWrapper extends Object
{
  /**
   * Application starting point.
   *
   * @param  args  Command line arguments.
   */

  public static void main(String args[])
  {
    try
    {
      if(args.length < 2)
      {
        System.out.println("Usage: dscmd <ds_config> <dsoperation document>");
      }
      else
      {
        DsOperation ds = new DsOperation(args[0]);

        File file = new File(args[1]);
        FileInputStream fis = new FileInputStream(file);
        byte data[] = new byte[(int)file.length()];

 
        fis.read(data);
        fis.close();
 
        String result = ds.doOperation(new String(data));
        System.out.println(result);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
