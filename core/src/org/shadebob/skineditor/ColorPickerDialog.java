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
import java.util.Iterator;

import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;

/**
 * A color picker class that allow to create and re-use colors across
 * the skin. It uses Swing color picker.
 * 
 * @author Yanick Bourbeau
 * 
 */
public class ColorPickerDialog extends Dialog {

	private SkinEditorGame game;
	private Table tableColors;
	ObjectMap<String, Color> colors;
	private Field field;

	/**
	 * 
	 */
	public ColorPickerDialog(final SkinEditorGame game, final Field field) {
	
		super("Color Picker", game.skin);
		
		this.game = game;
		this.field = field;

		tableColors = new Table(game.skin);
		tableColors.left().top().pad(5);
		tableColors.defaults().pad(5);
		colors = game.skinProject.getAll(Color.class);

		updateTable();

		
		TextButton buttonNewColor = new TextButton("New Color", game.skin);
		buttonNewColor.addListener(new ChangeListener() {

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
				
				// Call swing color picker
				java.awt.Color color = JColorChooser.showDialog(null, "Pick your color", java.awt.Color.WHITE);
				if (color != null) {
					
					String colorName = JOptionPane.showInputDialog("Name your color");

					if ((colorName != null) && (colorName.isEmpty() == false)) {
						// Verify if the color name is already in use
						if (colors.containsKey(colorName) == true) {
							game.showNotice("Error", "Color name already in use!", game.screenMain.stage);
						} else {
							// Add the color (asuming RGBA)
							float[] components = color.getComponents(null);
							Color newColor = new Color(components[0], components[1], components[2], components[3]);
							if (isColorInUse(newColor)) {
								game.showNotice("Error","Same color value (" + newColor.toString() + ") is already defined with a different name!", game.screenMain.stage);
								return;
							}
								

							colors.put(colorName, newColor);
							game.screenMain.saveToSkin();

							// update table
							updateTable();
						}
					}
				}

			}

		});

		TextButton buttonNoColor = new TextButton("Empty Color", game.skin);
		buttonNoColor.addListener(new ChangeListener() {

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

		ScrollPane scrollPane = new ScrollPane(tableColors, game.skin);
		scrollPane.setFlickScroll(false);
		scrollPane.setFadeScrollBars(false);
		scrollPane.setScrollbarsOnTop(true);

		getContentTable().add(scrollPane).width(640).height(320).pad(20);
		getButtonTable().add(buttonNewColor);
		getButtonTable().add(buttonNoColor);
		getButtonTable().padBottom(15);
		button("Cancel", false);
		key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
		

	}

	/**
	 * Refresh table content with colors from the skin
	 */
	public void updateTable() {

		tableColors.clear();
		tableColors.add(new Label("Color Name", game.skin, "title")).left().width(170);
		tableColors.add(new Label("Value", game.skin, "title")).colspan(2).left().width(60).padRight(50);

		tableColors.row();

		Iterator<String> it = colors.keys().iterator();
		while (it.hasNext()) {
			final String key = it.next();
			final Color color = colors.get(key);

			tableColors.add(key).left();

			// Create drawable on the fly
			Pixmap pixmap = new Pixmap(18, 18, Pixmap.Format.RGBA8888);
			pixmap.setColor(color);
			pixmap.fill();
			pixmap.setColor(Color.BLACK);
			pixmap.drawRectangle(0, 0, 18, 18);
			Texture texture = new Texture(pixmap);
			pixmap.dispose();
			tableColors.add(new Image(texture));
			tableColors.add(color.toString()).left();

			TextButton buttonSelect = new TextButton("Select", game.skin);
			buttonSelect.addListener(new ChangeListener() {

				@Override
				public void changed(ChangeEvent event, Actor actor) {
					try {
						field.set(game.screenMain.paneOptions.currentStyle, color);
					} catch (Exception e) {
						e.printStackTrace();
					}

					hide();
					game.screenMain.panePreview.refresh();
					game.screenMain.paneOptions.updateSelectedTableFields();
					game.screenMain.saveToSkin();


				}

			});

			TextButton buttonRemove = new TextButton("Remove", game.skin);
			buttonRemove.addListener(new ChangeListener() {

				@Override
				public void changed(ChangeEvent event, Actor actor) {

					Dialog dlg = new Dialog("Delete Style", game.skin) {

						@Override
						protected void result(Object object) {
							if ((Boolean) object == false) {
								return;
							}

							if (isColorInUse(color) == true) {

								game.showNotice("Error", "Color already in use!", game.screenMain.stage);

							} else {

								colors.remove(key);
								// update table
								updateTable();
								game.screenMain.saveToSkin();

							}
						}

					};

					dlg.pad(20);
					dlg.getContentTable().add("You are sure you want to delete this color?");
					dlg.button("OK", true);
					dlg.button("Cancel", false);
					dlg.key(com.badlogic.gdx.Input.Keys.ENTER, true);
					dlg.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
					dlg.show(game.screenMain.stage);
					

				}

			});

			tableColors.add(buttonSelect).padRight(5);
			tableColors.add(buttonRemove);
			tableColors.row();
		}

	}

	
	/**
	 * Check if the color is already in use somewhere else in the skin
	 */
	public boolean isColorInUse(Color color) {

		try {
			// Check if it is already in use somewhere!
			for (String widget : SkinEditorGame.widgets) {
				String widgetStyle = "com.badlogic.gdx.scenes.scene2d.ui." + widget + "$" + widget + "Style";
				Class<?> style = Class.forName(widgetStyle);
				ObjectMap<String, ?> styles = game.skinProject.getAll(style);
				Iterator<String> it = styles.keys().iterator();
				while (it.hasNext()) {
					Object item = styles.get((String) it.next());
					Field[] fields = ClassReflection.getFields(item.getClass());
					for (Field field : fields) {

						if (field.getType() == Color.class) {

							Color c = (Color) field.get(item);
							if (color.equals(c)) {
								return true;
							}

						}

					}

				}

			}
		} catch (Exception e) {
			e.printStackTrace();

		}

		return false;
	}
}
