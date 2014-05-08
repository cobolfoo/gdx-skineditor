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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;


/**
 * 
 * A table representing the menu bar at the top of the interface
 * 
 * @author Yanick Bourbeau
 *
 */
public class MenuBar extends Table {

	private SkinEditorGame game;
	private Label labelProjectName;
	
	/**
	 * 
	 */
	public MenuBar(final SkinEditorGame game) {
		
		super();

		this.game = game;
		
		left();
		setBackground(game.skin.getDrawable("default-pane"));
		
		TextButton buttonRefresh = new TextButton("Refresh Resources", game.skin);
		add(buttonRefresh).pad(5);
		
		TextButton buttonClose = new TextButton("Close Project", game.skin);
		add(buttonClose).pad(5).expandX().left();

		buttonRefresh.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				game.screenMain.refreshResources();
				
			}
			
		});
		

		
		buttonClose.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				game.screenMain.setCurrentProject("");
				game.setScreen(game.screenWelcome);
				
			}
			
		});
		
		
		Label labelCurrentProject = new Label("Current Project:", game.skin,"title");
		add(labelCurrentProject).pad(5).padRight(20);

		labelProjectName = new Label("---", game.skin);
		add(labelProjectName).pad(5).padRight(20);
	}
	
	
	/**
	 * Update project name field 
	 */
	public void update(String projectName) {
		labelProjectName.setText(projectName);
	}
	
}
