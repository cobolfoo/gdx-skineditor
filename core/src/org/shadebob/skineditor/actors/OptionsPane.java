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

import java.util.Iterator;

import org.shadebob.skineditor.ColorPickerDialog;
import org.shadebob.skineditor.DrawablePickerDialog;
import org.shadebob.skineditor.FontPickerDialog;
import org.shadebob.skineditor.SkinEditorGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Keys;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;

/**
 * A table representing the left portion of the interface
 * 
 * @author Yanick Bourbeau
 *
 */
public class OptionsPane extends Table {

	private SkinEditorGame game;
	public List<String> listStyles;
	private Array<String> listItems = new Array<String>();
	private Table tableFields;
	public Object currentStyle;
	private ObjectMap<String, ?> styles;

	/**
	 * 
	 */
	public OptionsPane(final SkinEditorGame game) {
		super();

		this.game = game;

		left();
		top();
		setBackground(game.skin.getDrawable("default-pane"));

		add(new Label("Styles", game.skin, "title")).pad(5).row();
		listStyles = new List<String>(game.skin, "dimmed");
		listStyles.setItems(listItems);
		ScrollPane scroll = new ScrollPane(listStyles, game.skin);
		scroll.setFlickScroll(false);
		scroll.setFadeScrollBars(false);
		scroll.setScrollbarsOnTop(true);
		scroll.setScrollBarPositions(false, true);
		scroll.setScrollingDisabled(true, false);
		add(scroll).height(200).expandX().fillX().pad(5).row();

		// Add buttons
		Table tableStylesButtons = new Table();
		TextButton buttonNewStyle = new TextButton("New Style", game.skin);
		TextButton buttonDeleteStyle = new TextButton("Delete Style", game.skin);
		tableStylesButtons.add(buttonNewStyle).pad(5);
		tableStylesButtons.add(buttonDeleteStyle).pad(5);
		add(tableStylesButtons).row();

		// Callbacks

		listStyles.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {

				String key = (String) listStyles.getSelected();
				if (key != null) {
					Gdx.app.log("OptionsPane", "Selected style: " + key);
					currentStyle = styles.get(key);
					updateTableFields(key);
				}
			}

		});

		buttonNewStyle.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				createNewStyle();
			}

		});

		buttonDeleteStyle.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				showDeleteDialog();
			}

		});

		// Initialize table

		add(new Label("Fields", game.skin, "title")).pad(5).padTop(10).row();

		tableFields = new Table(game.skin);
		tableFields.setBackground(game.skin.getDrawable("dialogDim"));
		tableFields.left().top();

		ScrollPane scroll2 = new ScrollPane(tableFields, game.skin);
		scroll2.setFlickScroll(false);
		scroll2.setFadeScrollBars(false);
		scroll2.setScrollbarsOnTop(true);
		scroll2.setScrollBarPositions(false, true);
		scroll2.setScrollingDisabled(true, false);
		add(scroll2).pad(5).expand().fill();

	}

	/**
	 * 
	 */
	protected void showDeleteDialog() {

		// Check if it used by other style prior to delete it
		// FIXME: TODO

		Dialog dlgStyle = new Dialog("Delete Style", game.skin) {

			@Override
			protected void result(Object object) {
				if ((Boolean) object == false) {
					return;
				}

				// Now we really add it!
				game.skinProject.remove((String) listStyles.getSelected(), currentStyle.getClass());
				refresh();
				game.screenMain.saveToSkin();
				game.screenMain.panePreview.refresh();

			}

		};

		dlgStyle.pad(20);
		dlgStyle.getContentTable().add("You are sure you want to delete this style?");
		dlgStyle.button("OK", true);
		dlgStyle.button("Cancel", false);
		dlgStyle.key(com.badlogic.gdx.Input.Keys.ENTER, true);
		dlgStyle.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
		dlgStyle.show(getStage());

	}

	/**
	 * 
	 */
	protected void createNewStyle() {

		final TextField textStyleName = new TextField("", game.skin);
		Dialog dlgStyle = new Dialog("New Style", game.skin) {

			@Override
			protected void result(Object object) {
				if ((Boolean) object == false) {
					return;
				}

				String styleName = textStyleName.getText();
				if (styleName.length() == 0) {

					game.showNotice("Warning", "No style name entered!", game.screenMain.stage);
					return;
				}

				// Check if the style name is already in use
				if (listItems.contains(styleName, false)) {
					game.showNotice("Warning", "Style name already in use!", game.screenMain.stage);
					return;
				}
				
				
				try {
					game.skinProject.add(styleName,  currentStyle.getClass().newInstance());
				} catch(Exception e) {
					e.printStackTrace();
				}
				//game.skinProject.add(text, game.skin.get("default", currentStyle.getClass()), currentStyle.getClass());
				game.screenMain.saveToSkin();
				refresh();

				game.screenMain.panePreview.refresh();

			}

		};

		dlgStyle.pad(20);
		dlgStyle.getContentTable().add("Style Name:");
		dlgStyle.getContentTable().add(textStyleName).pad(20);
		dlgStyle.button("OK", true);
		dlgStyle.button("Cancel", false);
		dlgStyle.key(com.badlogic.gdx.Input.Keys.ENTER, true);
		dlgStyle.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
		dlgStyle.show(getStage());
		getStage().setKeyboardFocus(textStyleName);

	}

	/**
	 * 
	 */
	public void refreshSelection() {

		String key = listStyles.getSelected();
		
		ImageButton button = (ImageButton) game.screenMain.barWidgets.group.getChecked();
		String widget = button.getUserObject().toString();
		String widgetStyle = "com.badlogic.gdx.scenes.scene2d.ui." + widget + "$" + widget + "Style";
		Gdx.app.log("OptionsPane", "Fetching style:" + widgetStyle);
		
		listItems.clear();

		try {
			Class<?> style = Class.forName(widgetStyle);

			styles = game.skinProject.getAll(style);
			if (styles == null) {
				Gdx.app.error("OptionsPane", "No styles defined for this widget type");

				tableFields.clear();
			} else {
				Keys<String> keys = styles.keys();

				for (String k : keys) {
					listItems.add(k);

				}
				
			}
			listItems.sort();
			listStyles.setItems(listItems);

		} catch (Exception e) {
			e.printStackTrace();
		}

		
		currentStyle = styles.get(key);
		updateTableFields(key);

	}
	
	/**
	 * 
	 */
	public void refresh() {

		Gdx.app.log("OptionsPane", "Refresh");

		ImageButton button = (ImageButton) game.screenMain.barWidgets.group.getChecked();
		String widget = button.getUserObject().toString();
		String widgetStyle = "com.badlogic.gdx.scenes.scene2d.ui." + widget + "$" + widget + "Style";
		Gdx.app.log("OptionsPane", "Fetching style:" + widgetStyle);
		
		listItems.clear();
		int selection = -1;

		try {
			Class<?> style = Class.forName(widgetStyle);

			styles = game.skinProject.getAll(style);
			if (styles == null) {
				Gdx.app.error("OptionsPane", "No styles defined for this widget type");

				tableFields.clear();
			} else {
				Keys<String> keys = styles.keys();
				boolean first = true;

				for (String key : keys) {
					listItems.add(key);

					if (first == true) {

						currentStyle = styles.get(key);
						updateTableFields(key);
						selection = listItems.size-1;
						first = false;
					}

				}

			}
			listItems.sort();
			listStyles.setItems(listItems);
			
			if (selection != -1) {
				listStyles.setSelectedIndex(selection);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	public void updateSelectedTableFields() {

		String key = (String) listStyles.getSelected();
		if (key != null) {
			Gdx.app.log("OptionsPane", "Selected style: " + key);
			currentStyle = styles.get(key);
			updateTableFields(key);
		}
	}

	/**
	 * 
	 */
	private void updateTableFields(final String style) {

		ImageButton button = (ImageButton) game.screenMain.barWidgets.group.getChecked();
		String widget = button.getUserObject().toString();

		Gdx.app.log("OptionsPane", "Update fields table for widget: " + widget + ", style: " + style);
		tableFields.clear();
		tableFields.add(new Label("Name", game.skin, "title")).left().width(170);
		tableFields.add(new Label("Value", game.skin, "title")).left().width(60).padRight(50);
		tableFields.row();

		Field[] fields = ClassReflection.getFields(currentStyle.getClass());
		for (final Field field : fields) {
			try {

				// field name

				// White required
				// Grey optional
				if (game.opt.isFieldOptional(currentStyle.getClass(), field.getName())) {

					tableFields.add(new Label(field.getName(),game.skin,"optional")).left();
					
				} else {
					tableFields.add(new Label(field.getName(), game.skin, "default")).left();
					
				}

				Actor actor;

				// field type
				String name = field.getType().getSimpleName();
				Object obj = field.get(currentStyle);

				if (name.equals("Drawable")) {

					/**
					 * Handle Drawable object
					 */

					Drawable drawable = (Drawable) field.get(currentStyle);
					String resourceName = "";
					ImageTextButton.ImageTextButtonStyle buttonStyle = new ImageTextButton.ImageTextButtonStyle(game.skin.getDrawable("default-round"),
							game.skin.getDrawable("default-round-down"), game.skin.getDrawable("default-round"), game.skin.getFont("default-font"));

					if (drawable != null) {
						resourceName = game.skinProject.resolveObjectName(Drawable.class, drawable);
						buttonStyle.imageUp = drawable;
					} else {
						buttonStyle.up = game.skinProject.getDrawable("default-rect");
						buttonStyle.checked = game.skinProject.getDrawable("default-rect");
					}

					actor = new ImageTextButton(resourceName, buttonStyle);
					((ImageTextButton) actor).setClip(true);
					actor.addListener(new ChangeListener() {

						@Override
						public void changed(ChangeEvent event, Actor actor) {
							showDrawableDialog(field);

						}

					});

				} else if (name.equals("Color")) {

					/**
					 * Handle Color object
					 */
					Color color = (Color) field.get(currentStyle);
					ImageTextButton.ImageTextButtonStyle buttonStyle = new ImageTextButton.ImageTextButtonStyle(game.skin.getDrawable("default-round"),
							game.skin.getDrawable("default-round-down"), game.skin.getDrawable("default-round"), game.skin.getFont("default-font"));

					String resourceName = "";
					if (color != null) {
						resourceName = game.skinProject.resolveObjectName(Color.class, color);
						resourceName += " (" + color.toString() + ")";

						// Create drawable on the fly
						Pixmap pixmap = new Pixmap(18, 18, Pixmap.Format.RGBA8888);
						pixmap.setColor(color);
						pixmap.fill();
						pixmap.setColor(Color.BLACK);
						pixmap.drawRectangle(0, 0, 18, 18);
						Texture texture = new Texture(pixmap);
						buttonStyle.imageUp = new SpriteDrawable(new Sprite(texture));
						pixmap.dispose();
					} else {
						buttonStyle.up = game.skinProject.getDrawable("default-rect");
						buttonStyle.checked = game.skinProject.getDrawable("default-rect");
					}

					actor = new ImageTextButton(resourceName, buttonStyle);
					((ImageTextButton) actor).setClip(true);
					actor.addListener(new ChangeListener() {

						@Override
						public void changed(ChangeEvent event, Actor actor) {
							showColorPickerDialog(field);

						}

					});

				} else if (name.equals("BitmapFont")) {

					/**
					 * Handle BitmapFont object
					 */

					BitmapFont font = (BitmapFont) field.get(currentStyle);
					String resourceName = "";
					ImageTextButton.ImageTextButtonStyle buttonStyle = new ImageTextButton.ImageTextButtonStyle(game.skin.getDrawable("default-round"),
							game.skin.getDrawable("default-round-down"), game.skin.getDrawable("default-round"), game.skin.getFont("default-font"));

					if (font != null) {
						resourceName = game.skinProject.resolveObjectName(BitmapFont.class, font);
						buttonStyle.font = font;
					} else {
						buttonStyle.up = game.skinProject.getDrawable("default-rect");
						buttonStyle.checked = game.skinProject.getDrawable("default-rect");
					}

					actor = new ImageTextButton(resourceName, buttonStyle);
					((ImageTextButton) actor).setClip(true);
					
					actor.addListener(new ChangeListener() {

						@Override
						public void changed(ChangeEvent event, Actor actor) {
							showFontPickerDialog(field);

						}

					});

				} else if (name.equals("float")) {

					/**
					 * Handle Float object
					 */

					Float value = (Float) field.get(currentStyle);
					String resourceName = "";

					ImageTextButton.ImageTextButtonStyle buttonStyle = new ImageTextButton.ImageTextButtonStyle(game.skin.getDrawable("default-round"),
							game.skin.getDrawable("default-round-down"), game.skin.getDrawable("default-round"), game.skin.getFont("default-font"));

					if ((value != null) && (value != 0)) {
						resourceName = String.valueOf(value);
					} else {
						buttonStyle.up = game.skinProject.getDrawable("default-rect");
						buttonStyle.checked = game.skinProject.getDrawable("default-rect");
					}

					actor = new ImageTextButton(resourceName, buttonStyle);
					((ImageTextButton) actor).setClip(true);
					actor.addListener(new ChangeListener() {

						@Override
						public void changed(ChangeEvent event, Actor actor) {
							showFloatInputDialog(field);

						}

					});
					
				} else if (name.equals("ListStyle")) {
					
					/**
					 * Handle ListStyle object
					 */
					ListStyle listStyle = (ListStyle) field.get(currentStyle);
					
					actor = new SelectBox<String>(game.skin, "default");
					Array<String> items = new Array<String>();
					
					final ObjectMap<String, ListStyle>  values = game.skinProject.getAll(ListStyle.class);
					Iterator<String> it = values.keys().iterator();
					String selection = null;
					
					while(it.hasNext()) {
						String key = it.next();
						items.add(key);
						
						if (listStyle == values.get(key)) {
							selection = key;
						}
					}

					((SelectBox) actor).setItems(items);
					
					if (selection != null) {
						((SelectBox) actor).setSelected(selection);
					}

					actor.addListener(new ChangeListener() {

						@Override
						public void changed(ChangeEvent event, Actor actor) {
							
							String selection = (String) ((SelectBox) actor).getSelected();
							try {
								field.set(currentStyle, values.get(selection));
							} catch(Exception e) {
								e.printStackTrace();
							}
							
							game.screenMain.saveToSkin();
							refresh();	
							game.screenMain.paneOptions.updateSelectedTableFields();
							game.screenMain.panePreview.refresh();
						}
						
					});
					
					
					} else if (name.equals("ScrollPaneStyle")) {
					
					/**
					 * Handle ListStyle object
					 */
					ScrollPaneStyle scrollStyle = (ScrollPaneStyle) field.get(currentStyle);
					
					actor = new SelectBox<String>(game.skin, "default");
					Array<String> items = new Array<String>();
					
					final ObjectMap<String, ScrollPaneStyle>  values = game.skinProject.getAll(ScrollPaneStyle.class);
					Iterator<String> it = values.keys().iterator();
					String selection = null;
					
					while(it.hasNext()) {
						String key = it.next();
						items.add(key);
						
						if (scrollStyle == values.get(key)) {
							selection = key;
						}
					}

					((SelectBox) actor).setItems(items);
					
					if (selection != null) {
						((SelectBox) actor).setSelected(selection);
					}

					actor.addListener(new ChangeListener() {

						@Override
						public void changed(ChangeEvent event, Actor actor) {
							
							String selection = (String) ((SelectBox) actor).getSelected();
							try {
								field.set(currentStyle, values.get(selection));
							} catch(Exception e) {
								e.printStackTrace();
							}
							
							game.screenMain.saveToSkin();
							refresh();	
							game.screenMain.paneOptions.updateSelectedTableFields();
							game.screenMain.panePreview.refresh();
						}
						
					});

				} else {

					Gdx.app.log("OptionsPane", "Unknown type: " + name);
					actor = new Label("Unknown Type", game.skin);
				}

				tableFields.add(actor).left().height(32).padRight(24).expandX().fillX();

				tableFields.row();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 
	 * Display a input dialog asking for a float value
	 * 
	 */
	public void showFloatInputDialog(final Field field) {

		try {
			final TextField textValue = new TextField(String.valueOf((Float) field.get(currentStyle)), game.skin);
			Dialog dlg = new Dialog("Change Value", game.skin) {

				@Override
				protected void result(Object object) {
					if ((Boolean) object == false) {
						return;
					}

					float value = 0;
					String text = textValue.getText();
					if (text.isEmpty() == false) {
						value = Float.valueOf(text);
					}

					try {
						field.set(currentStyle, value);
					} catch (Exception e) {
						e.printStackTrace();
					}

					game.screenMain.saveToSkin();
					refresh();	
					game.screenMain.paneOptions.updateSelectedTableFields();
					game.screenMain.panePreview.refresh();

				}

			};

			dlg.pad(20);
			dlg.getContentTable().add("Float Value:");
			dlg.getContentTable().add(textValue).pad(20);
			dlg.button("OK", true);
			dlg.button("Cancel", false);
			dlg.key(com.badlogic.gdx.Input.Keys.ENTER, true);
			dlg.key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
			dlg.show(getStage());
			getStage().setKeyboardFocus(textValue);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Show color picker dialog
	 */
	public void showColorPickerDialog(final Field field) {

		ColorPickerDialog dlg = new ColorPickerDialog(game, field);
		dlg.show(getStage());
	}
	
	/**
	 * Show font picker dialog
	 */
	public void showFontPickerDialog(final Field field) {

		FontPickerDialog dlg = new FontPickerDialog(game, field);
		dlg.show(getStage());
	}
	
	
	/**
	 * Show drawable picker dialog
	 * @param field
	 */
	public void showDrawableDialog(final Field field) {

		DrawablePickerDialog dlg = new DrawablePickerDialog(game, field);
		dlg.show(getStage());
	}
	
}
