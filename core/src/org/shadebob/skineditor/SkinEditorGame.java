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
package org.shadebob.skineditor;

import org.shadebob.skineditor.screens.MainScreen;
import org.shadebob.skineditor.screens.WelcomeScreen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;


/**
 * Main game class I re-use everywhere in this program.
 * 
 * @author Yanick Bourbeau
 */
public class SkinEditorGame extends Game {
	
	
	public final static String[] widgets = { "Label","Button","TextButton", "ImageButton", "CheckBox", "TextField",  "List", "SelectBox", "ProgressBar", "Slider", "ScrollPane", "SplitPane", "Window", "Touchpad", "Tree" };

	public SpriteBatch batch;
	public Skin skin;
	public TextureAtlas atlas;

	public MainScreen screenMain;
	public WelcomeScreen screenWelcome;
	
	// Project related
	public Skin skinProject;
	
	// System fonts
	public SystemFonts fm;
	
	@Override
	public void create() {
		
		fm = new SystemFonts();
		fm.refreshFonts();
		
		// Create projects folder if not already here
		FileHandle dirProjects = new FileHandle("projects");
		
		if (dirProjects.isDirectory() == false) {
			dirProjects.mkdirs();
		}
		
		// Rebuild from raw resources, kind of overkill, might disable it for production
		TexturePacker.Settings settings = new TexturePacker.Settings();
		settings.combineSubdirectories = true;
		TexturePacker.process(settings, "resources/raw/", ".", "resources/uiskin");

		batch = new SpriteBatch();
		skin = new Skin();
		atlas = new TextureAtlas(Gdx.files.internal("resources/uiskin.atlas"));
		
		
		skin.addRegions(new TextureAtlas(Gdx.files.local("resources/uiskin.atlas")));
		skin.load(Gdx.files.local("resources/uiskin.json"));
		
		screenMain = new MainScreen(this);
		screenWelcome = new WelcomeScreen(this);
		setScreen(screenWelcome);
		
	}
	

	
	/**
	 * Display a dialog with a notice
	 */
	public void showNotice(String title, String message, Stage stage) {
		Dialog dlg = new Dialog(title, skin);
		dlg.pad(20);
		dlg.getContentTable().add(message).pad(20);
		dlg.button("OK", true);
		dlg.key(com.badlogic.gdx.Input.Keys.ENTER, true);
		dlg.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
		dlg.show(stage);
	}
	
}
