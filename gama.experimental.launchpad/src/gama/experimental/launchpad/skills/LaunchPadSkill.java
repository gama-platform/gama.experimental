package gama.experimental.launchpad.skills;

import gama.core.metamodel.shape.GamaPoint;
import gama.annotations.precompiler.GamlAnnotations.action;
import gama.annotations.precompiler.GamlAnnotations.arg;
import gama.annotations.precompiler.GamlAnnotations.doc;
import gama.annotations.precompiler.GamlAnnotations.example;
import gama.annotations.precompiler.GamlAnnotations.getter;
import gama.annotations.precompiler.GamlAnnotations.skill;
import gama.annotations.precompiler.GamlAnnotations.variable;
import gama.annotations.precompiler.GamlAnnotations.vars;
import gama.annotations.precompiler.IConcept;
import gama.core.runtime.GAMA;
import gama.core.runtime.IScope;
import gama.core.runtime.exceptions.GamaRuntimeException;
import gama.core.util.IList;
import gama.gaml.skills.Skill;
import gama.gaml.types.IType;
import net.thecodersbreakfast.lp4j.api.BackBufferOperation;
import net.thecodersbreakfast.lp4j.api.Button;
import net.thecodersbreakfast.lp4j.api.Color;

@vars ({ @variable (
		name = "buttonPressed",
		type = IType.STRING,
		doc = @doc ("get button pressed name")),
		@variable (
				name = "padPressed",
				type = IType.POINT,
				doc = @doc ("get pad pressed name")) })

@doc ("The launchpad skill is intended to interact with a simulation using a launchpad interface")
@skill (
		name = "launchpadskill",
		concept = { IConcept.LAYER })
public class LaunchPadSkill extends Skill {

	@getter ("buttonPressed")
	public String getButtonPressed() {
		String name;
		if (LaunchPadEventLayer.pressedButton != null) {
			name = LaunchPadEventLayer.pressedButton.name();
		} else {
			name = "NULL";
		}
		return name;
	}

	@getter ("padPressed")
	public GamaPoint getPadPressed() {
		final GamaPoint p = new GamaPoint(LaunchPadEventLayer.pressedPad.getX(), LaunchPadEventLayer.pressedPad.getY());
		return p;
	}

	@action (
			name = "resetPad",
			doc = @doc (
					examples = { @example ("do resetPad;") },
					value = "reset the pad"))
	public void resetPad(final IScope scope) throws GamaRuntimeException {
		if (LaunchPadEventLayer.client != null) {
			LaunchPadEventLayer.client.reset();
		}
		return;
	}

	@action (
			name = "updateDisplay",
			doc = @doc (
					examples = { @example ("do updateDisplay;") },
					value = "update GAMA Display"))
	public void updateDisplay(final IScope scope) throws GamaRuntimeException {
		GAMA.getExperiment().refreshAllOutputs();
		return;
	}

	@action (
			name = "setPadLight",
			args = { @arg (
					name = "color",
					type = IType.STRING,
					optional = false,
					doc = @doc ("Color of the pad (as a string)"))

			},
			doc = @doc (
					examples = { @example ("do setPadLight color: #red;") },
					value = "set the color of the pad pressed"))

	public void setPadLight(final IScope scope) throws GamaRuntimeException {
		final String colorString = scope.hasArg("color") ? (String) scope.getArg("color", IType.STRING) : "black";
		if (LaunchPadEventLayer.pressedPad != null) {
			if (colorString.equals("brown")) {
				LaunchPadEventLayer.client.setPadLight(LaunchPadEventLayer.pressedPad, Color.BROWN,
						BackBufferOperation.NONE);
			}
			if (colorString.equals("black")) {
				LaunchPadEventLayer.client.setPadLight(LaunchPadEventLayer.pressedPad, Color.BLACK,
						BackBufferOperation.NONE);
			}
			if (colorString.equals("green")) {
				LaunchPadEventLayer.client.setPadLight(LaunchPadEventLayer.pressedPad, Color.GREEN,
						BackBufferOperation.NONE);
			}
			if (colorString.equals("darkgreen")) {
				LaunchPadEventLayer.client.setPadLight(LaunchPadEventLayer.pressedPad, Color.DARKGREEN,
						BackBufferOperation.NONE);
			}
			if (colorString.equals("orange")) {
				LaunchPadEventLayer.client.setPadLight(LaunchPadEventLayer.pressedPad, Color.ORANGE,
						BackBufferOperation.NONE);
			}
			if (colorString.equals("red")) {
				LaunchPadEventLayer.client.setPadLight(LaunchPadEventLayer.pressedPad, Color.RED,
						BackBufferOperation.NONE);
			}
			if (colorString.equals("darkred")) {
				LaunchPadEventLayer.client.setPadLight(LaunchPadEventLayer.pressedPad, Color.DARKRED,
						BackBufferOperation.NONE);
			}
			if (colorString.equals("yellow")) {
				LaunchPadEventLayer.client.setPadLight(LaunchPadEventLayer.pressedPad, Color.YELLOW,
						BackBufferOperation.NONE);
			}
			if (colorString.equals("lightyellow")) {
				LaunchPadEventLayer.client.setPadLight(LaunchPadEventLayer.pressedPad, Color.LIGHTYELLOW,
						BackBufferOperation.NONE);
			}
		}
		return;
	}

	@action (
			name = "setButtonLight",
			args = { @arg (
					name = "colors",
					type = IType.LIST,
					optional = false,
					doc = @doc ("Color of the top button ")) },
			doc = @doc (
					examples = { @example ("do setButtonLight colors: colors;") },
					value = "set the color of the top buttons"))

	public void setButtonLight(final IScope scope) throws GamaRuntimeException {
		final IList color_Map = (IList) scope.getArg("colors", IType.LIST);
		if (LaunchPadEventLayer.client != null) {
			LaunchPadEventLayer.client.setButtonLight(Button.UP, LaunchPadEventLayer.colorMap.get(color_Map.get(0)),
					BackBufferOperation.NONE);
			LaunchPadEventLayer.client.setButtonLight(Button.DOWN, LaunchPadEventLayer.colorMap.get(color_Map.get(1)),
					BackBufferOperation.NONE);
			LaunchPadEventLayer.client.setButtonLight(Button.LEFT, LaunchPadEventLayer.colorMap.get(color_Map.get(2)),
					BackBufferOperation.NONE);
			LaunchPadEventLayer.client.setButtonLight(Button.RIGHT, LaunchPadEventLayer.colorMap.get(color_Map.get(3)),
					BackBufferOperation.NONE);
			LaunchPadEventLayer.client.setButtonLight(Button.SESSION,
					LaunchPadEventLayer.colorMap.get(color_Map.get(4)), BackBufferOperation.NONE);
			LaunchPadEventLayer.client.setButtonLight(Button.USER_1, LaunchPadEventLayer.colorMap.get(color_Map.get(5)),
					BackBufferOperation.NONE);
			LaunchPadEventLayer.client.setButtonLight(Button.USER_2, LaunchPadEventLayer.colorMap.get(color_Map.get(6)),
					BackBufferOperation.NONE);
			LaunchPadEventLayer.client.setButtonLight(Button.MIXER, LaunchPadEventLayer.colorMap.get(color_Map.get(7)),
					BackBufferOperation.NONE);
		}
		return;
	}
}
