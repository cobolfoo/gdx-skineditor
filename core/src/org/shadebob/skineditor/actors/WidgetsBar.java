/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.shadebob.skineditor.actors;

import org.shadebob.skineditor.SkinEditorGame;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.mobidevelop.maps.editor.ui.utils.Tooltips;

/**
 * 
 * A table representing the buttons panel at the top
 * 
 * @author Yanick Bourbeau
 *
 */
public class WidgetsBar extends Table {

	private SkinEditorGame game;
	public ButtonGroup group;

	/**
	 * 
	 */
	public WidgetsBar(final SkinEditorGame game) {
		super();
		this.game = game;

		left();
		setBackground(game.skin.getDrawable("default-pane"));

	}

	/**
	 * 
	 */
	public void initializeButtons() {
		
		group = new ButtonGroup();

		Tooltips.TooltipStyle styleTooltip = new Tooltips.TooltipStyle(game.skin.getFont("default-font"), game.skin.getDrawable("default-round"), game.skin.getColor("white"));		

		String[] widgets = SkinEditorGame.widgets;
		for (String widget : widgets) {

			ImageButtonStyle style = new ImageButtonStyle();
			style.checked = game.skin.getDrawable("default-round-down");
			style.down = game.skin.getDrawable("default-round-down");
			style.up = game.skin.getDrawable("default-round");
			style.imageUp = game.skin.getDrawable("widgets/" + widget);
			ImageButton button = new ImageButton(style);
			button.setUserObject(widget);

			Tooltips tooltip = new Tooltips(styleTooltip, getStage());
			tooltip.registerTooltip(button, (String) button.getUserObject()); 
			
			button.addListener(new ClickListener() {

				@Override
				public void clicked(InputEvent event, float x, float y) {
					game.screenMain.panePreview.refresh();
					game.screenMain.paneOptions.refresh();

				}

			});

			group.add(button);
			add(button).pad(5);
		}
		
		
	}

	/**
	 * 
	 */
	public void resetButtonSelection() {
		group.getButtons().get(0).setChecked(true);
	}

}
