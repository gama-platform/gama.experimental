using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using DHI.Generic.MikeZero;
using DHI.Generic.MikeZero.DFS;
using DHI.Generic.MikeZero.DFS.dfsu;
using DHI.Generic.MikeZero.DFS.dfs123;
using System.Collections;

namespace ConsoleApp1
{
    class Program
    {
        static void Main(string[] args)
        {
            ReadDfs0File("C:\\git\\HydraulicTools\\RESULT2015.res11");
            //DfsuFile dfs1file = DfsFileFactory.DfsuFileOpen("C:\\git\\HydraulicTools\\RESULT2015.res11");

            Console.ReadLine();
        }
        /// <summary>
        /// Introductory example of how to load a dfs0 file.
        /// <para>
        /// The method assumes that the Rain_stepaccumulated.dfs0 test file
        /// is the input file.
        /// </para>
        /// </summary>
        /// <param name="filename">path and name of Rain_stepaccumulated.dfs0 test file</param>
        public static void ReadDfs0File(string filename)
        {
            // Open the file as a generic dfs file
            IDfsFile dfs0File = DfsFileFactory.DfsGenericOpen(filename);
            /*
            // Header information is contained in the IDfsFileInfo
            IDfsFileInfo fileInfo = dfs0File.FileInfo;
            int steps = fileInfo.TimeAxis.NumberOfTimeSteps;                   // 19

            // Information on each of the dynamic items, here the first one
            IDfsSimpleDynamicItemInfo dynamicItemInfo = dfs0File.ItemInfo[0];
            string nameOfFirstDynamicItem = dynamicItemInfo.Name;              // "Rain"
            DfsSimpleType typeOfFirstDynamicItem = dynamicItemInfo.DataType;   // Double
            ValueType valueType = dynamicItemInfo.ValueType;                   // StepAccumulated

            // Read data of first item, third time step (items start by 1, timesteps by 0),
            // assuming data is of type double.
            IDfsStaticItem  data = dfs0File.ReadStaticItem(0);//.ReadItemTimeStep(1, 2);
            object value1 = data.Data.GetValue(0);                                        // 0.36
            Console.WriteLine(value1);
            // Read data of first item, third time step (items start by 1, timesteps by 0),
            // converting data to double
            // IDfsItemData datag = dfs0File.ReadItemTimeStep(1, 2);
            // double value2 = System.Convert.ToDouble(datag.Data.GetValue(0));     // 0.36
            */

            IDfsFileInfo fileInfo = dfs0File.FileInfo;
            int steps = fileInfo.TimeAxis.NumberOfTimeSteps;
            Console.WriteLine(steps);
            IDfsItemData data;
            for (int ii = 0; ii < steps; ii++)
            {
                IList<IDfsDynamicItemInfo> iill = dfs0File.ItemInfo.Where(i => i.Name.Contains("DINH_DAO")).ToList();
                //IList<IDfsDynamicItemInfo> iill = dfs0File.ItemInfo;
                for (int j = 0; j < iill.Count; j++)
                {
                    data = dfs0File.ReadItemTimeStep(iill[j].ItemNumber, ii);
                    Console.WriteLine(iill[j].ItemNumber);
                    Console.WriteLine(iill[j].Name);
                    Console.WriteLine(data.Data.GetLength(0));
                    
                    for (int i = 0; i < data.Data.GetLength(0); i++)
                    {

                        object value = data.Data.GetValue(i); 

                        Console.Write("\t\t"+value);
                    }

                    Console.WriteLine();
                }
            }
        }

        /// <summary>
        /// Find maximum value and time of maximum for a specified item in dfs0 file
        /// </summary>
        /// <param name="filename">Path and name of file, e.g. data_ndr_roese.dfs0 test file</param>
        /// <param name="itemNumber">Item number to find maximum for</param>
        public static void FindMaxValue(string filename, int itemNumber)
        {
            // Open file
            IDfsFile file = DfsFileFactory.DfsGenericOpen(filename);

            // Extract Start date-time of file - assuming file is equidistant-calendar axis
            IDfsEqCalendarAxis timeAxis = (IDfsEqCalendarAxis)file.FileInfo.TimeAxis;
            DateTime startDateTime = timeAxis.StartDateTime;

            // Empty item data, reused when calling ReadItemTimeStep
            IDfsItemData<float> itemData = (IDfsItemData<float>)file.CreateEmptyItemData(itemNumber);

            // max value and time variables
            double maxValue = double.MinValue;
            double maxTime = -1;
            // Loop over all times in file
            for (int i = 0; i < file.FileInfo.TimeAxis.NumberOfTimeSteps; i++)
            {
                // Read time step for item, and extract value
                file.ReadItemTimeStep(itemData, i);
                double value = itemData.Data[0];
                // Check if value is larger than maxValue
                if (value > maxValue)
                {
                    maxValue = value;
                    maxTime = itemData.TimeInSeconds(timeAxis);
                }
            }
            // Report results
            Console.Out.WriteLine("Max Value      : {0} {1}", maxValue, file.ItemInfo[itemNumber - 1].Quantity.UnitAbbreviation);
            Console.Out.WriteLine("Max Value time : {0}", startDateTime.AddSeconds(maxTime).ToString("yyyy-MM-dd HH:mm:ss"));
        }

        /// <summary>
        /// Update time series with a constant change factor, adding 10% to all values
        /// </summary>
        /// <param name="dfs0File">Path and name of file, e.g. Rain_instantaneous.dfs0 test file</param>
        /// <param name="dfs0FileNew">Name of new updated file</param>
        public static void UpdateDfs0Data(string dfs0File, string dfs0FileNew)
        {
            // Open source file
            IDfsFile source = DfsFileFactory.DfsGenericOpen(dfs0File);

            // Create a new file with updated rain values
            DfsBuilder builder = DfsBuilder.Create(source.FileInfo.FileTitle + "Updated", "MIKE SDK", 13);

            // Copy header info from source file to new file
            builder.SetDataType(source.FileInfo.DataType);
            builder.SetGeographicalProjection(source.FileInfo.Projection);
            builder.SetTemporalAxis(source.FileInfo.TimeAxis);

            // Copy over first item from source file to new file
            builder.AddDynamicItem(source.ItemInfo[0]);

            // Create the new file
            builder.CreateFile(dfs0FileNew);
            IDfsFile target = builder.GetFile();

            // Loop over all timesteps
            for (int i = 0; i < source.FileInfo.TimeAxis.NumberOfTimeSteps; i++)
            {
                // Read time step for item, and extract value
                IDfsItemData<double> itemData = (IDfsItemData<double>)source.ReadItemTimeStep(1, i);
                double value = itemData.Data[0];
                // Write new value to target, adding 10% to its value
                target.WriteItemTimeStepNext(itemData.Time, new double[] { value * 1.1 });
            }

            source.Close();
            target.Close();
        }



        /// <summary>
        /// Creates a dfs0 file, with an equidistant time axis and one dynamic item.
        /// <para>
        /// It uses the generic <see cref="DfsBuilder"/>, since currently no specialized 
        /// builder exists for the dfs0 files.
        /// </para>
        /// </summary>
        /// <param name="filename">Name of new file</param>
        /// <param name="calendarAxis">boolean specifying whether the temporal axis should be a calendar axis or a time axis</param>
        public static void CreateDfs0File(string filename, bool calendarAxis)
        {
            DfsFactory factory = new DfsFactory();
            DfsBuilder builder = DfsBuilder.Create("TemporalAxisTest", "dfs Timeseries Bridge", 10000);

            // Set up file header
            builder.SetDataType(1);
            builder.SetGeographicalProjection(factory.CreateProjectionUndefined());
            if (calendarAxis)
                builder.SetTemporalAxis(factory.CreateTemporalEqCalendarAxis(eumUnit.eumUsec, new DateTime(2010, 01, 04, 12, 34, 00), 4, 10));
            else
                builder.SetTemporalAxis(factory.CreateTemporalEqTimeAxis(eumUnit.eumUsec, 3, 10));
            builder.SetItemStatisticsType(StatType.RegularStat);

            // Set up first item
            DfsDynamicItemBuilder item1 = builder.CreateDynamicItemBuilder();
            item1.Set("WaterLevel item", eumQuantity.Create(eumItem.eumIWaterLevel, eumUnit.eumUmeter),
                      DfsSimpleType.Float);
            item1.SetValueType(DataValueType.Instantaneous);
            item1.SetAxis(factory.CreateAxisEqD0());
            item1.SetReferenceCoordinates(1f, 2f, 3f);
            builder.AddDynamicItem(item1.GetDynamicItemInfo());

            DfsDynamicItemBuilder item2 = builder.CreateDynamicItemBuilder();
            item2.Set("WaterDepth item", eumQuantity.Create(eumItem.eumIWaterDepth, eumUnit.eumUmeter),
                      DfsSimpleType.Float);
            item2.SetValueType(DataValueType.Instantaneous);
            item2.SetAxis(factory.CreateAxisEqD0());
            item2.SetReferenceCoordinates(1f, 2f, 3f);
            builder.AddDynamicItem(item2.GetDynamicItemInfo());

            // Create file
            builder.CreateFile(filename);
            IDfsFile file = builder.GetFile();

            // Write data to file
            file.WriteItemTimeStepNext(0, new float[] { 0f });  // water level
            file.WriteItemTimeStepNext(0, new float[] { 100f });  // water depth
            file.WriteItemTimeStepNext(0, new float[] { 1f });  // water level
            file.WriteItemTimeStepNext(0, new float[] { 101f });  // water depth
            file.WriteItemTimeStepNext(0, new float[] { 2f });  // water level
            file.WriteItemTimeStepNext(0, new float[] { 102f });  // water depth
            file.WriteItemTimeStepNext(0, new float[] { 3f });  // etc...
            file.WriteItemTimeStepNext(0, new float[] { 103f });
            file.WriteItemTimeStepNext(0, new float[] { 4f });
            file.WriteItemTimeStepNext(0, new float[] { 104f });
            file.WriteItemTimeStepNext(0, new float[] { 5f });
            file.WriteItemTimeStepNext(0, new float[] { 105f });
            file.WriteItemTimeStepNext(0, new float[] { 10f });
            file.WriteItemTimeStepNext(0, new float[] { 110f });
            file.WriteItemTimeStepNext(0, new float[] { 11f });
            file.WriteItemTimeStepNext(0, new float[] { 111f });
            file.WriteItemTimeStepNext(0, new float[] { 12f });
            file.WriteItemTimeStepNext(0, new float[] { 112f });
            file.WriteItemTimeStepNext(0, new float[] { 13f });
            file.WriteItemTimeStepNext(0, new float[] { 113f });

            file.Close();
        }
    }
}
