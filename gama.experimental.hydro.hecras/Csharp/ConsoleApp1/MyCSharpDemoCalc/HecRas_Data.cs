using RAS506;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks; 

namespace HecRas_Gama_Coupling
{
    /*
    public interface ICalc
    {
        string MySuperSmartFunctionIDontHaveInJava(string filename, string gate_name);
    }
    */


    public class HecRas_Data //: ICalc
    {
        public String ReadData()
        {
            HECRASController hrc = new HECRASController();
            hrc.Project_Open(@"C:\Users\hqngh\OneDrive\Documents\Hello World Coupling\HelloWorldCoupling.prj");

            int nmsg = 0;
            bool block = true;
            Array sa = null;
            String result = "";
            try
            {
                hrc.Compute_HideComputationWindow();

                hrc.Compute_CurrentPlan(ref nmsg, ref sa);

            }
            catch (Exception ex)
            {
                result = ex.ToString();
            }
            hrc.Project_Close();
            hrc.QuitRas();
            return result;
        }
    }
}
