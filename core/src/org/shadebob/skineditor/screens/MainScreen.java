package org.shadebob.skineditor.screens;

import org.shadebob.skineditor.SkinEditorGame;
import org.shadebob.skineditor.actors.MenuBar;
import org.shadebob.skineditor.actors.OptionsPane;
import org.shadebob.skineditor.actors.PreviewPane;
import org.shadebob.skineditor.actors.WidgetsBar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MainScreen implements Screen {

	private SkinEditorGame game;
	public MenuBar barMenu;
	public WidgetsBar barWidgets;
	public PreviewPane panePreview;
	public OptionsPane paneOptions;
	public Stage stage;
	private String currentProject = "";
	
	public MainScreen(SkinEditorGame game) {
		this.game = game;
		
		barMenu = new MenuBar(game);
		barWidgets = new WidgetsBar(game);
		panePreview = new PreviewPane(game);
		paneOptions = new OptionsPane(game);
		stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		
		Table table = new Table();
		table.setFillParent(true);
		
		table.top().left().add(barMenu).expandX().fillX().colspan(2).row();
		table.top().left().add(barWidgets).expandX().fillX().colspan(2).row();
		table.add(paneOptions).width(420).fill().expandY();
		table.add(new ScrollPane(panePreview)).fill().expand();
		stage.addActor(table);
		barWidgets.initializeButtons();
		

		
	}
	
	@Override
	public void render(float delta) {
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();	
		
//		Table.drawDebug(stage);
		
	}
	
	/**
	 * 
	 * @param project
	 */
	public void setCurrentProject(String project) {
		currentProject = project;
	}
	
	public String getcurrentProject() {
		return currentProject;
	}

	/**
	 * 
	 */
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
		
	}

	/**
	 * 
	 */
	@Override
	public void show() {

		if  (currentProject == "") {
			Gdx.app.error("MainScreen", "Current project not set!");
			Gdx.app.exit();
		}
		refreshResources();
		
		barMenu.update(currentProject);
		
		Gdx.input.setInputProcessor(stage);
		
		panePreview.refresh();
		paneOptions.refresh();
		
		barWidgets.resetButtonSelection();
	}
	

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
		// Do nothing
		
	}

	@Override
	public void resume() {
		// Do nothing
		
	}

	@Override
	public void dispose() {
		// Do nothing
		
	}

	/**
	 * Save everything to skin
	 */
	public void saveToSkin() {

		FileHandle projectFolder = Gdx.files.local("projects").child(currentProject);
		
		FileHandle[] items = projectFolder.child("backups").list();
		Array<String> sortedItems = new Array<String>();
		for(FileHandle item: items) {
			sortedItems.add(item.name());
		}
		sortedItems.sort();
		
		// Keep only last ten files
		int count = 0;
		for (String item : sortedItems) {
			if (count++ > 8) {
				// Remove file 
				projectFolder.child("backups").child(item).delete();
			}
		}
		
		FileHandle projectFile = projectFolder.child("uiskin.json");
		FileHandle backupFile = projectFolder.child("backups").child("uiskin_" +(TimeUtils.millis()/1000) +".json");
		projectFile.copyTo(backupFile);
		game.skinProject.save(projectFile);
		
	}
	
	/**
	 * 
	 */
	public void refreshResources() {
		
		TexturePacker.Settings settings = new TexturePacker.Settings();
		settings.combineSubdirectories = true;
		settings.maxWidth = 2048;
		settings.maxHeight = 2048;
		TexturePacker.process(settings, "projects/" + currentProject +"/assets/", "projects/" + currentProject, "uiskin");
		

		// Load project skin
		if (game.skinProject != null) {
			game.skinProject.dispose();
		}
		
		game.skinProject = new Skin();
		game.skinProject.addRegions(new TextureAtlas(Gdx.files.local("projects/" + currentProject +"/uiskin.atlas")));
		game.skinProject.load(Gdx.files.local("projects/" + currentProject +"/uiskin.json"));

		
	}
}
