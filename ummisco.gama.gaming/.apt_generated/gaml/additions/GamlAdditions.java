package gaml.additions;
import msi.gaml.extensions.multi_criteria.*;
import msi.gama.outputs.layers.charts.*;
import msi.gama.outputs.layers.*;
import msi.gama.outputs.*;
import msi.gama.kernel.batch.*;
import msi.gama.kernel.root.*;
import msi.gaml.architecture.weighted_tasks.*;
import msi.gaml.architecture.user.*;
import msi.gaml.architecture.reflex.*;
import msi.gaml.architecture.finite_state_machine.*;
import msi.gaml.species.*;
import msi.gama.metamodel.shape.*;
import msi.gaml.expressions.*;
import msi.gama.metamodel.topology.*;
import msi.gaml.statements.test.*;
import msi.gama.metamodel.population.*;
import msi.gama.kernel.simulation.*;
import msi.gama.kernel.model.*;
import java.util.*;
import msi.gaml.statements.draw.*;
import  msi.gama.metamodel.shape.*;
import msi.gama.common.interfaces.*;
import msi.gama.runtime.*;
import java.lang.*;
import msi.gama.metamodel.agent.*;
import msi.gaml.types.*;
import msi.gaml.compilation.*;
import msi.gaml.factories.*;
import msi.gaml.descriptions.*;
import msi.gama.util.tree.*;
import msi.gama.util.file.*;
import msi.gama.util.matrix.*;
import msi.gama.util.graph.*;
import msi.gama.util.path.*;
import msi.gama.util.*;
import msi.gama.runtime.exceptions.*;
import msi.gaml.factories.*;
import msi.gaml.statements.*;
import msi.gaml.skills.*;
import msi.gaml.variables.*;
import msi.gama.kernel.experiment.*;
import msi.gaml.operators.*;
import msi.gama.common.interfaces.*;
import msi.gama.extensions.messaging.*;
import msi.gama.metamodel.population.*;
import msi.gaml.operators.Random;
import msi.gaml.operators.Maths;
import msi.gaml.operators.Points;
import msi.gaml.operators.Spatial.Properties;
import msi.gaml.operators.System;
import static msi.gaml.operators.Cast.*;
import static msi.gaml.operators.Spatial.*;
import static msi.gama.common.interfaces.IKeyword.*;
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })

public class GamlAdditions extends AbstractGamlAdditions {
	public void initialize() throws SecurityException, NoSuchMethodException {
	initializeVars();
	initializeAction();
	initializeSkill();
}public void initializeVars()  {
_var(ummisco.gama.gaming.ui.skills.UILocatedSkill.class,desc(7,S("type","7","name","locked_location")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UILocatedSkill.class,desc(2,S("type","2","name","locked_ui_width")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UILocatedSkill.class,desc(2,S("type","2","name","locked_ui_height")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UILocatedSkill.class,desc(2,S("type","2","name","ui_width")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UILocatedSkill.class,desc(2,S("type","2","name","ui_height")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UILocatedSkill.class,desc(4,S("type","4","name","locked_display")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UITableCell.class,desc(7,S("type","7","name","locked_location")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UITableCell.class,desc(2,S("type","2","name","locked_ui_width")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UITableCell.class,desc(2,S("type","2","name","locked_ui_height")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UITableCell.class,desc(2,S("type","2","name","ui_width")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UITableCell.class,desc(2,S("type","2","name","ui_height")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UITableCell.class,desc(4,S("type","4","name","locked_display")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UITableSkill.class,desc(7,S("type","7","name","locked_location")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UITableSkill.class,desc(2,S("type","2","name","locked_ui_width")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UITableSkill.class,desc(2,S("type","2","name","locked_ui_height")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UITableSkill.class,desc(2,S("type","2","name","ui_width")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UITableSkill.class,desc(2,S("type","2","name","ui_height")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UITableSkill.class,desc(1,S("type","1","name","number_of_lines")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UITableSkill.class,desc(1,S("type","1","name","number_of_columns")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UITableSkill.class,desc(16,S("type","16","name","matrix_data")),null,null,null);
_var(ummisco.gama.gaming.ui.skills.UITableSkill.class,desc(4,S("type","4","name","locked_display")),null,null,null);
}public void initializeAction() throws SecurityException, NoSuchMethodException {
_action((s,a,t,v)->{((ummisco.gama.gaming.ui.skills.UILocatedSkill) t).moveAgentAt(s);return null;},desc(PRIM,new Children(desc(ARG,NAME,"ui_location",TYPE,"7","optional",FALSE)),NAME,"move_agent_at",TYPE,Ti(void.class),VIRTUAL,FALSE),ummisco.gama.gaming.ui.skills.UILocatedSkill.class.getMethod("moveAgentAt",SC));
_action((s,a,t,v)->((ummisco.gama.gaming.ui.skills.UILocatedSkill) t).getUILocation(s),desc(PRIM,new Children(desc(ARG,NAME,"display_name",TYPE,"4","optional",TRUE)),NAME,"ui_location",TYPE,Ti(P),VIRTUAL,FALSE),ummisco.gama.gaming.ui.skills.UILocatedSkill.class.getMethod("getUILocation",SC));
_action((s,a,t,v)->{((ummisco.gama.gaming.ui.skills.UILocatedSkill) t).setAgentLocationInUI(s);return null;},desc(PRIM,new Children(desc(ARG,NAME,"ui_location",TYPE,"7","optional",FALSE),desc(ARG,NAME,"display_name",TYPE,"4","optional",FALSE),desc(ARG,NAME,"ui_height",TYPE,"2","optional",FALSE),desc(ARG,NAME,"ui_width",TYPE,"2","optional",FALSE)),NAME,"lock_agent_at",TYPE,Ti(void.class),VIRTUAL,FALSE),ummisco.gama.gaming.ui.skills.UILocatedSkill.class.getMethod("setAgentLocationInUI",SC));
_action((s,a,t,v)->{((ummisco.gama.gaming.ui.skills.UITableSkill) t).setAgentLocationInUI(s);return null;},desc(PRIM,new Children(desc(ARG,NAME,"ui_location",TYPE,"7","optional",FALSE),desc(ARG,NAME,"display_name",TYPE,"4","optional",FALSE),desc(ARG,NAME,"number_of_lines",TYPE,"1","optional",FALSE),desc(ARG,NAME,"number_of_columns",TYPE,"1","optional",FALSE),desc(ARG,NAME,"ui_height",TYPE,"2","optional",FALSE),desc(ARG,NAME,"ui_width",TYPE,"2","optional",FALSE)),NAME,"lock_agent_at",TYPE,Ti(void.class),VIRTUAL,FALSE),ummisco.gama.gaming.ui.skills.UITableSkill.class.getMethod("setAgentLocationInUI",SC));
_action((s,a,t,v)->{((ummisco.gama.gaming.ui.skills.UITableSkill) t).addLine(s);return null;},desc(PRIM,new Children(desc(ARG,NAME,"elements",TYPE,"16","optional",FALSE)),NAME,"add_line",TYPE,Ti(void.class),VIRTUAL,FALSE),ummisco.gama.gaming.ui.skills.UITableSkill.class.getMethod("addLine",SC));
}public void initializeSkill()  {
_skill("UI_location",ummisco.gama.gaming.ui.skills.UILocatedSkill.class,AS);
_skill("UI_table_cell",ummisco.gama.gaming.ui.skills.UITableCell.class,AS);
_skill("UI_table",ummisco.gama.gaming.ui.skills.UITableSkill.class,AS);
}
}