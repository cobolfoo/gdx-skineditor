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

import java.util.Iterator;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * Display a dialog that let you pick up a font.
 * I re-use some of the Hiero code here to generate bitmap fonts
 * from TTF files.
 * 
 * @author Yanick Bourbeau
 * 
 */
public class FontPickerDialog extends Dialog {

	private SkinEditorGame game;
	private Table tableFonts;
	ObjectMap<String, BitmapFont> fonts;
	private Field field;

	/**
	 * 
	 */
	public FontPickerDialog(final SkinEditorGame game, Field field) {

		super("Bitmap Font Picker", game.skin);

		this.game = game;
		this.field = field;

		tableFonts = new Table(game.skin);
		tableFonts.left().top().pad(5);
		tableFonts.defaults().pad(5);

		fonts = game.skinProject.getAll(BitmapFont.class);

		updateTable();

		TextButton buttonNewFont = new TextButton("New Font", game.skin);
		buttonNewFont.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {

				showNewFontDialog();

			}

		});


		ScrollPane scrollPane = new ScrollPane(tableFonts, game.skin);
		scrollPane.setFlickScroll(false);
		scrollPane.setFadeScrollBars(false);
		scrollPane.setScrollbarsOnTop(true);

		getContentTable().add(scrollPane).width(720).height(420).pad(20);
		getButtonTable().add(buttonNewFont);
		getButtonTable().padBottom(15);
		button("Cancel", false);
		key(com.badlogic.gdx.Input.Keys.ESCAPE, false);

	}

	/**
	 * 
	 */
	public void updateTable() {

		fonts = game.skinProject.getAll(BitmapFont.class);

		tableFonts.clear();
		tableFonts.add(new Label("Font Name", game.skin, "title")).left().width(170);
		tableFonts.add(new Label("Value", game.skin, "title")).colspan(2).left().width(60).padRight(50).expandX().fillX();

		tableFonts.row();

		Iterator<String> it = fonts.keys().iterator();
		while (it.hasNext()) {
			final String key = it.next();
			final BitmapFont font = fonts.get(key);

			tableFonts.add(key).left();

			Label.LabelStyle labelStyle = new Label.LabelStyle();
			labelStyle.font = font;
			labelStyle.fontColor = Color.WHITE;

			tableFonts.add(new Label("Sample Text", labelStyle));

			TextButton buttonSelect = new TextButton("Select", game.skin);
			buttonSelect.addListener(new ChangeListener() {

				@Override
				public void changed(ChangeEvent event, Actor actor) {
					try {
						// Since we have reloaded everything we have to get
						// field back

						game.screenMain.paneOptions.refreshSelection();
						field.set(game.screenMain.paneOptions.currentStyle, font);

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

					Dialog dlg = new Dialog("Delete Font", game.skin) {

						@Override
						protected void result(Object object) {
							if ((Boolean) object == false) {
								return;
							}

							if (isFontInUse(font) == true) {

								game.showNotice("Error", "Bitmap font already in use!", getStage());

							} else {
								
								// Remove files from disk (fnt and png)
								FileHandle targetFont = new FileHandle("projects/" + game.screenMain.getcurrentProject() + "/" +key + ".fnt");
								FileHandle targetImage = new FileHandle("projects/" + game.screenMain.getcurrentProject() + "/assets/" + key + ".png");
								targetFont.delete();
								targetImage.delete();
								
								fonts.remove(key);
								// update table
								updateTable();
								game.screenMain.saveToSkin();

							}
						}

					};

					dlg.pad(20);
					dlg.getContentTable().add("You are sure you want to delete this bitmap font?");
					dlg.button("OK", true);
					dlg.button("Cancel", false);
					dlg.key(com.badlogic.gdx.Input.Keys.ENTER, true);
					dlg.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
					dlg.show(getStage());

				}

			});

			tableFonts.add(buttonSelect);
			tableFonts.add(buttonRemove);
			tableFonts.row();
		}

	}

	/**
	 * Is font is already in use somewhere else?
	 */
	public boolean isFontInUse(BitmapFont font) {

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

						if (field.getType() == BitmapFont.class) {

							BitmapFont f = (BitmapFont) field.get(item);
							if (font.equals(f)) {
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

	/**
	 * 
	 */
	public void showNewFontDialog() {

		NewFontDialog dlg = new NewFontDialog(game) {

			@Override
			public void hide() {
				super.hide();

				updateTable();
			}
		};
		dlg.show(getStage());

	}

}
