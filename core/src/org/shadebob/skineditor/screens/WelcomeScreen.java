package org.shadebob.skineditor.screens;

import org.shadebob.skineditor.SkinEditorGame;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class WelcomeScreen implements Screen {

	private SkinEditorGame game;
	public Stage stage;
	private List<String> listProjects;

	public WelcomeScreen(SkinEditorGame game) {
		this.game = game;
		stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
	}

	@Override
	public void render(float delta) {

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();
		Table.drawDebug(stage);

	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);

	}

	@Override
	public void show() {

		Table table = new Table(game.skin);
		table.setFillParent(true);
		table.setBackground(game.skin.getDrawable("dialogDim"));
		stage.addActor(table);

		Table tableContent = new Table(game.skin);
		tableContent.left();
		tableContent.add(new Label("Project List", game.skin, "title")).left().row();
		listProjects = new List<String>(game.skin);
		ScrollPane scrollPane = new ScrollPane(listProjects, game.skin);
		tableContent.add(scrollPane).width(320).height(200).row();

		Table tableButtons = new Table(game.skin);

		TextButton buttonNewProject = new TextButton("New Project", game.skin);
		final TextButton buttonOpen = new TextButton("Open", game.skin);
		final TextButton buttonDelete = new TextButton("Delete", game.skin);
		buttonOpen.setDisabled(true);
		buttonDelete.setDisabled(true);

		tableButtons.add(buttonNewProject).pad(5).expandX().fillX();
		tableButtons.add(buttonOpen).pad(5).width(92);
		tableButtons.add(buttonDelete).pad(5).width(92);

		tableContent.add(tableButtons).expandX().fillX();

		table.add(tableContent);

		Gdx.input.setInputProcessor(stage);

		listProjects.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (listProjects.getSelected() != null) {
					
					buttonOpen.setDisabled(false);
					buttonDelete.setDisabled(false);
					
				} else {
					buttonOpen.setDisabled(true);
					buttonDelete.setDisabled(true);
					
				}
			}
			
		});
		
		
		buttonNewProject.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {

			
				showNewProjectDialog();

			}

		});

		
		buttonOpen.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				game.screenMain.setCurrentProject((String) listProjects.getSelected());
				game.setScreen(game.screenMain);

			}
		});

		buttonDelete.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				showDeleteDialog();

			}


		});

		refreshProjects();

	}

	/**
	 * 
	 */
	private void showNewProjectDialog() {

		final TextField textProject = new TextField("", game.skin);
		Dialog dlg = new Dialog("New Project", game.skin) {

			@Override
			protected void result(Object object) {
				if ((Boolean) object == false) {
					return;
				}

				String projectName = textProject.getText();
				projectName = projectName.replace(".", "_");
				projectName = projectName.replace("/", "_");
				projectName = projectName.replace("\\", "_");
				projectName = projectName.replace("-", "_");
				if (projectName.isEmpty() == true)
					return;

				createProject(projectName);

			}

		};

		dlg.pad(20);
		dlg.getContentTable().add("Project Name:");
		dlg.getContentTable().add(textProject).pad(20);
		dlg.button("OK", true);
		dlg.button("Cancel", false);
		dlg.key(com.badlogic.gdx.Input.Keys.ENTER, true);
		dlg.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
		dlg.setWidth(480);
		dlg.show(stage);
		stage.setKeyboardFocus(textProject);
	}

	@Override
	public void hide() {
		// Do nothing
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
	 * 
	 */
	public void refreshProjects() {

		Array<String> items = new Array<String>();

		FileHandle[] projects = Gdx.files.local("projects").list();
		for (FileHandle project : projects) {
			if (project.child("uiskin.json").exists() == true) {
				items.add(project.name());
			}
		}
		items.sort();
		listProjects.setItems(items);
	}

	/**
	 * 
	 * @param projectName
	 */
	public void createProject(String projectName) {

		FileHandle projectFolder = Gdx.files.local("projects").child(projectName);
		if (projectFolder.exists() == true) {
			game.showNotice("Error", "Project name already in use!", stage);
			return;
		}

		projectFolder.mkdirs();
		projectFolder.child("assets").mkdirs();
		projectFolder.child("backups").mkdirs();
		game.skin.save(projectFolder.child("uiskin.json"));

		// Copy assets
		FileHandle assetsFolder = Gdx.files.local("resources/raw");
		assetsFolder.copyTo(projectFolder.child("assets"));
		// Rebuild from raw resources
		TexturePacker.Settings settings = new TexturePacker.Settings();
		settings.combineSubdirectories = true;
		TexturePacker.process(settings, "projects/" + projectName +"/assets/", "projects/" + projectName, "uiskin");

		game.showNotice("Operation completed", "New project successfully created.", stage);
		
		refreshProjects();
	}

	

	/**
	 * 
	 */
	private void showDeleteDialog() {
		
		Dialog dlgStyle = new Dialog("Delete Project", game.skin) {

			@Override
			protected void result(Object object) {
				if ((Boolean) object == false) {
					return;
				}

				// We delete it
				FileHandle projectFolder = Gdx.files.local("projects/" + (String) listProjects.getSelected());
				projectFolder.deleteDirectory();
				
				refreshProjects();
			}

		};

		dlgStyle.pad(20);
		dlgStyle.getContentTable().add(
				"You are sure you want to delete this project?");
		dlgStyle.button("OK", true);
		dlgStyle.button("Cancel", false);
		dlgStyle.key(com.badlogic.gdx.Input.Keys.ENTER, true);
		dlgStyle.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
		dlgStyle.show(stage);
		
	}
}
