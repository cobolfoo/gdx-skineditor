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

import java.awt.Frame;
import java.io.File;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.Field;

/**
 * Display a dialog allowing to pick a drawable resource such as a ninepatch
 * or a texture region. You can also add resource from file (PNG only for now)
 * 
 * @author Yanick Bourbeau
 * 
 */
public class DrawablePickerDialog extends Dialog {

	private SkinEditorGame game;
	private Field field;
	private Table tableDrawables;
	private boolean zoom = false;
	private ObjectMap<String, Object> items = new ObjectMap<String,Object>();
	private ScrollPane scrollPane;

	public DrawablePickerDialog(final SkinEditorGame game, final Field field) {

		super("Drawable Picker", game.skin);
		this.game = game;
		this.field = field;

		tableDrawables = new Table(game.skin);
		scrollPane = new ScrollPane(tableDrawables, game.skin);
		getContentTable().add(scrollPane);
		scrollPane.setFlickScroll(false);
		scrollPane.setFadeScrollBars(false);
		scrollPane.setScrollbarsOnTop(true);

		TextButton buttonNewNinePatch = new TextButton("Create NinePatch", game.skin);
		buttonNewNinePatch.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {

				NinePatchEditorDialog dlg = new NinePatchEditorDialog(game) {
					@Override
					public void hide() {
						super.hide();

						updateTable();
					}
				};
				
				dlg.show(game.screenMain.stage);
			}

		});
		
		TextButton buttonNewDrawable = new TextButton("Import Image", game.skin);
		buttonNewDrawable.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {

				// Need to steal focus first with this hack (Thanks to Z-Man)
				Frame frame = new Frame();
				frame.setUndecorated(true);
				frame.setOpacity(0);
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
				frame.toFront();
				frame.setVisible(false);
				frame.dispose();
				
				
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "png");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(null);
				if (returnVal != JFileChooser.APPROVE_OPTION) {
					return;
				}
				File selectedFile = chooser.getSelectedFile();
				if (selectedFile == null) {
					return;
				}
				// Loop until the file is not found
				while (true) {
					String resourceName = selectedFile.getName();
					String ext = resourceName.substring(resourceName.lastIndexOf(".") + 1);
					resourceName = resourceName.substring(0, resourceName.lastIndexOf("."));
					resourceName = JOptionPane.showInputDialog("Please choose the name of your resource", resourceName);
					if (resourceName == null) {
						return;
					}

					// Lower case everything ! I sound like someone on
					// libgdx channel ;]
					resourceName = resourceName.toLowerCase();

					// Check for duplicate resources
					FileHandle[] assetsFolder = new FileHandle("projects/" + game.screenMain.getcurrentProject() + "/assets/").list();
					boolean foundSomething = false;
					for (FileHandle file : assetsFolder) {

						if (file.nameWithoutExtension().toLowerCase().equals(resourceName)) {
							foundSomething = true;
							break;
						}
					}
					if (foundSomething == true) {
						JOptionPane.showMessageDialog(null, "Sorry but this resource name is already in use!");
					} else {

						// Copy the file
						FileHandle orig = new FileHandle(selectedFile);
						FileHandle dest = new FileHandle("projects/" + game.screenMain.getcurrentProject() + "/assets/" + resourceName + "." + ext);
						orig.copyTo(dest);

						
						game.screenMain.refreshResources();
						refresh();
						JOptionPane.showMessageDialog(null, "File successfully added to your project.");
						return;
					}


				}

			}

		});


		TextButton buttonZoom = new TextButton("Toggle Zoom", game.skin);
		buttonZoom.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				zoom = !zoom;
				updateTable();

			}

		});
		
		TextButton buttonNoDrawable = new TextButton("Empty Drawable", game.skin);

		buttonNoDrawable.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {

				try {
					field.set(game.screenMain.paneOptions.currentStyle, null);
				} catch (Exception e) {
					e.printStackTrace();
				}

				game.screenMain.saveToSkin();

				hide();
				game.screenMain.panePreview.refresh();
				game.screenMain.paneOptions.updateSelectedTableFields();

			}

		});

		getContentTable().add(scrollPane).width(960).height(640).pad(20);
		getButtonTable().add(buttonNewNinePatch);
		getButtonTable().add(buttonNewDrawable);
		getButtonTable().add(buttonZoom);
		if (field != null) {
			getButtonTable().add(buttonNoDrawable);
		}
		getButtonTable().padBottom(15);
		button("Cancel", false);
		key(com.badlogic.gdx.Input.Keys.ESCAPE, false);

	}

	@Override
	public Dialog show(Stage stage) {

		refresh();

		Dialog d = super.show(stage);
		getStage().setScrollFocus(scrollPane);
		return d;
	}

	private void refresh() {

		ObjectMap<String, Drawable> itemsDrawables = game.skinProject.getAll(Drawable.class); 
		ObjectMap<String, TextureRegion> itemsRegions = game.skinProject.getAll(TextureRegion.class); 

		items.clear();
		
		Iterator<String> it = itemsDrawables.keys().iterator();
		while (it.hasNext()) {
			String key = it.next();
			items.put(key,  itemsDrawables.get(key));
		}

		it = itemsRegions.keys().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (itemsDrawables.containsKey(key)) {
				continue;
			}
			items.put(key,  itemsRegions.get(key));
		}
		
		updateTable();

	}

	/**
	 * 
	 */
	public void updateTable() {

		tableDrawables.clear();

		Iterator<String> keys = items.keys().iterator();
		int count = 0;

		while (keys.hasNext()) {

			final String key = keys.next();
			if (key.startsWith("widgets/")) {
				continue;
			}

			Button buttonItem = new Button(game.skin);

			Image img = null;
			if (items.get(key) instanceof Drawable) {
				img = new Image((Drawable) items.get(key));
			} else {
				img = new Image((TextureRegion) items.get(key));
				
			}
			
			if (zoom == true) {
				buttonItem.add(img).expand().fill().pad(5);
			} else {
				buttonItem.add(img).expand().pad(5);
			}

			buttonItem.addListener(new ChangeListener() {

				@Override
				public void changed(ChangeEvent event, Actor actor) {
					
					if (field == null) {
						return;
					}
					
					try {
						// Since we have reloaded everything we have to get
						// field back

						// game.screenMain.paneOptions.refreshSelection();
						if (items.get(key) instanceof Drawable) {
							field.set(game.screenMain.paneOptions.currentStyle, items.get(key));
						} else {
							
							boolean ninepatch = false;
							FileHandle test = new FileHandle("projects/" + game.screenMain.getcurrentProject() + "/assets/" + key + ".9.png");
							if (test.exists() == true) {
								ninepatch = true;
							}
							
							if  (ninepatch == true) {
								game.skinProject.add(key, new NinePatchDrawable(new NinePatch((TextureRegion) items.get(key))));
								field.set(game.screenMain.paneOptions.currentStyle, game.skinProject.getDrawable(key));							
								
							} else {
								game.skinProject.add(key, new SpriteDrawable(new Sprite((TextureRegion) items.get(key))));
								field.set(game.screenMain.paneOptions.currentStyle, game.skinProject.getDrawable(key));							
								
							}
						}
						
						game.screenMain.saveToSkin();
						hide();
						game.screenMain.panePreview.refresh();
						game.screenMain.paneOptions.updateSelectedTableFields();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			});

			String objectType = items.get(key).getClass().getSimpleName();
			objectType = objectType.replace("Drawable", "");

			buttonItem.row();
			buttonItem.add(new Label(key, game.skin));
			buttonItem.row();
			buttonItem.add(new Label(objectType, game.skin, "title"));
			buttonItem.row();
			buttonItem.setClip(true);
			tableDrawables.add(buttonItem).width(160).height(184).pad(5);

			if (count == 4) {
				count = 0;
				tableDrawables.row();
				continue;
			}

			count++;
		}

	}

}
