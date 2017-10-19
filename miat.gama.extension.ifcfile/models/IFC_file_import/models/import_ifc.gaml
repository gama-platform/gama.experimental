/**
* Name: importifc
* Author: Patrick Taillandier
* Description: Shows how to load a IFC file
* Tags: load_file, ifc, 3d
*/

model importifc

global {
	file<geometry> ifcfile <- ifc_file("../includes/Projet AUGC 2017.ifc");
	geometry shape <- envelope(ifcfile);
	
	init {
		//loop on the geometrues of the ifc file
		loop g over: ifcfile {
			//according to the type of elements, build the right agent
			string t <- g get "type";
			switch t {
				match "IfcWallStandardCase" {
					create wall with:[shape::g,name::g get "name", ifc_attributes::g.attributes];	
				}
				match "IfcSlab" {
					create slab with:[shape::g, name::g get "name", ifc_attributes::g.attributes];
				}
				match "IfcSpace" {
					create space with:[shape::g,name::g get "Reference", ifc_attributes::g.attributes] ;
				}
				match "IfcWindow" {
					create window with:[shape::g, name:: g get "name", ifc_attributes::g.attributes];
				}
				match "IfcDoor" {
					create door with:[shape::g, name:: g get "name", ifc_attributes::g.attributes];
				}
			}
		}
	}
}

species wall {
	map ifc_attributes;
	aspect default {
		draw shape color: #pink;
	}
}

species space {
	map ifc_attributes;
	aspect default {
		draw shape color: #yellow;
	}
}

species window {
	map ifc_attributes;
	aspect default {
		draw shape color: #lightblue;
	}
}

species door {
	map ifc_attributes;
	aspect default {
		draw shape color: #brown;
	}
}

species slab {
	map ifc_attributes;
	aspect default {
		draw shape color: #gray;
	}
}


experiment importifc type: gui {
	output {
		display view type: opengl{
			species wall;
			species slab;
			species space;
			species window;
			species door;
		}
	}
}
