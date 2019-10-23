using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Collections;
using System.Threading;

namespace ConsoleApp1
{
    class Program
    {
        static HecRas_Data hrc;
        static void Main(string[] args)
        {
            //ReadDfs0File("C:\\git\\HydraulicTools\\RESULT2015.res11");
            //DfsuFile dfs1file = DfsFileFactory.DfsuFileOpen("C:\\git\\HydraulicTools\\RESULT2015.res11");
            hrc = new HecRas_Data();
            int nmsg = 0;
            bool block = true;
            Array sa = null;
            hrc.Init_hecras();
            hrc.Project_Open(@"C:\Users\hqngh\OneDrive\Documents\Hello World Coupling\HelloWorldCoupling.prj");
            //hrc.Compute_ShowComputationWindow();

            try
            {
                Thread thread = new Thread(new ThreadStart(WorkThreadFunction));
                thread.Start();

                while (!hrc.isComplete())
                {
                    Console.Write(".");
                    Thread.Sleep(10);
                }
                Console.WriteLine("Done");
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.ToString());
            }
            Console.ReadLine();
        }

        private static void WorkThreadFunction()
        {
            hrc.Compute_CurrentPlan(); 
        }
    }
}
