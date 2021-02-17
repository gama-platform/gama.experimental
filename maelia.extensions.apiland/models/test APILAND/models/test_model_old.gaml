/**
* Name: new
* Author: bgaudou
* Description: Describe here the model and its experiments
* Tags: Tag1, Tag2, TagN
*/

model new
global {

       file parcelles_file <- shape_file("../includes/parcelles.shp"); // Donner plutot des géométries que le shemin du fichier

       file covers_file <- file("../includes/csp/covers.txt");

       file next_covers_file <- file("../includes/csp/next_covers.txt");

       file constraints_file <- csv_file("../includes/csp/contraintes.csv",";"); //C:\Users\rmisslin\git\gama.experimental\maelia.extensions.apiland\models\     "../includes/csp/system.csv"

       file proba_times_folder <- folder("../includes/proba_times");

       file groups_file <- file("../includes/csp/groups.txt");

       file historic_file <- file("../includes/csp/historic.txt");

       geometry shape <- envelope(parcelles_file);

      

       init {

             create parcelle from:  parcelles_file;

             create happyAPI number: 1;

            

//           save building to:"../results/buildings.shp" type:"shp" attributes:["farm","id","facility","type","area"];

            

            

             write covers_file;

            

             ask happyAPI {

                    map m <- map(self.firstCAPFarm(

                                 shapefile:parcelles_file.path,

                                 constraints: constraints_file.path, //récupération des contraintes

                                 covers:covers_file.path,

                                 groups:groups_file.path,

                                 historic:historic_file.path,

//                               next_covers:next_covers_file.path,

                                 proba_times_folder:proba_times_folder.path,

                                 farm:"4")

                    );          

                    write m;

                    write length(m);

             }

            

       }

      

//     reflex saveSHP {

//           save building to:"../results/buildings.shp" type:"shp" with:[farm::"farm", id::"id",facility::"facility",type::"type",area::"area"];

//          

//          

//     }

}

 

species happyAPI skills: [APILandExtension] {

      

}

 

species parcelle {

       //int id -> {int(self)};

       //string farm -> {"f0"};

       //string facility -> {nil};

       //string type -> {"parcel"};

       //string area -> {"AA"};

      

}

 

experiment new type: gui {

       /** Insert here the definition of the input and output of the model */

       output {

             display d {

                    species parcelle;

             }

       }

}