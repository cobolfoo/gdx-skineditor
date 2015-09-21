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

import java.awt.Font;
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.tools.hiero.BMFontUtil;
import com.badlogic.gdx.tools.hiero.unicodefont.UnicodeFont;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.ColorEffect;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.ShadowEffect;
import com.badlogic.gdx.utils.Array;


/**
 * Create new font using Hiero classes
 * 
 * @author Yanick Bourbeau
 */
public class NewFontDialog extends Dialog {

	private SkinEditorGame game;
	private TextField textFontName;
	private TextField textFontPreview;
	private SelectBox<String> selectFonts;
	private SelectBox<String> selectSize;
	private CheckBox checkBold;
	private CheckBox checkItalic;
	private CheckBox checkShadow;

	/**
	 * 
	 */
	public NewFontDialog(final SkinEditorGame game) {
		
		super("New Font", game.skin);
		this.game = game;
		
		Table table = new Table(game.skin);
		table.debug();
		table.defaults().pad(10);

		table.add("Bitmap font name:");
		
		textFontName = new TextField("", game.skin);
		table.add(textFontName).width(300).left().colspan(4);
		table.row();
		
		
		table.add("Source font (TTF):").padRight(10);

		selectFonts = new SelectBox<String>(game.skin);
		table.add(selectFonts).left().colspan(4);

		Map<String, File> mapFonts = new TreeMap<String, File>(game.fm.fonts);
		Array<String> items = new Array<String>();
		Iterator<String> it = mapFonts.keySet().iterator();
		
		boolean arialFound = false;
		
		while(it.hasNext()) {
			String key = it.next();
			if (key.equals("Arial") == true) {
				arialFound = true;
			}
			items.add(key);
		}
		selectFonts.setItems(items);

		// Select arial if found
		if (arialFound == true) {
			selectFonts.setSelected("Arial");
		}
		
		
		table.row();
		table.add("Font size:");
		selectSize = new SelectBox<String>(game.skin);
		selectSize.setItems("4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30", "32", "34", "36", "38", "40", "42","44","46","48","50","52","54","56");
		selectSize.setSelected("16");
		table.add(selectSize).left().width(50);

		checkBold = new CheckBox("Bold", game.skin);
		table.add(checkBold).left();

		checkItalic = new CheckBox("Italic", game.skin);
		table.add(checkItalic).left();

		checkShadow = new CheckBox("Shadow", game.skin);
		table.add(checkShadow).left().expandX();

		table.row();
		
		
		
		
		TextField.TextFieldStyle textStyle = new TextField.TextFieldStyle();
		textStyle.cursor = game.skin.getDrawable("cursor");
    	textStyle.selection = game.skin.getDrawable("selection");
		textStyle.background = game.skin.getDrawable("textfield");
		textStyle.fontColor = Color.YELLOW;
		textStyle.font = game.skin.getFont("default-font");

		textFontPreview = new TextField("This is a preview text", textStyle);
		table.add(textFontPreview).pad(20).colspan(5).expand().fill().left();
		
		selectFonts.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {

				refreshFontPreview();

			}

		});

		selectSize.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {

				refreshFontPreview();

			}

		});

		checkBold.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {

				refreshFontPreview();

			}

		});
		
		checkItalic.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {

				refreshFontPreview();

			}

		});

		checkShadow.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {

				refreshFontPreview();

			}

		});
		
		refreshFontPreview();

		getContentTable().add(table).width(520).height(320).pad(20);
		getButtonTable().padBottom(15);
		
		TextButton buttonCreate = new TextButton("Create Font", game.skin);
		buttonCreate.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				
				// First check if the name is already in use
				if (game.skinProject.has(textFontName.getText(), BitmapFont.class)) {
					game.showNotice("Error", "A font with the same name already exists!", getStage());
					return;
				}
				
				
				FileHandle handleFont = new FileHandle(System.getProperty("java.io.tmpdir")).child(textFontName.getText() + ".fnt");
				FileHandle handleImage = new FileHandle(System.getProperty("java.io.tmpdir")).child(textFontName.getText() + ".png");
				
				FileHandle targetFont = Gdx.files.local("projects/" + game.screenMain.getcurrentProject() + "/" + textFontName.getText() + ".fnt");
				FileHandle targetImage = Gdx.files.local("projects/" + game.screenMain.getcurrentProject() + "/assets/" + textFontName.getText() + ".png");
				
				if ((targetFont.exists() == true) || (targetImage.exists() == true)) {
					
					game.showNotice("Error", "A file with the same name already exists!", getStage());
					return;
				}
				
				handleFont.copyTo(targetFont);
				handleImage.copyTo(targetImage);
				
				game.skinProject.add(textFontName.getText(), new BitmapFont(targetFont, targetImage, false));
				game.screenMain.saveToSkin();
				game.screenMain.refreshResources();
				
				
				hide();
				
			}
			
		});
		
		getButtonTable().add(buttonCreate);
		button("Cancel", false);
		key(com.badlogic.gdx.Input.Keys.ESCAPE, false);
		
	}
	
	
	/**
	 * 
	 */
	@Override
	protected void result(Object object) {
		if ((Boolean) object == false) {
			return;
		}

	}


	/**
	 * 
	 */
	public void refreshFontPreview() {

		try {
			String fontName = selectFonts.getSelected();
			Gdx.app.log("FontPickerDialog", "Refreshing preview for font: " + fontName);

			File fontPath = game.fm.fonts.get(selectFonts.getSelected());
			Gdx.app.log("FontPickerDialog","Loading font from file:" + fontPath);
			
			Font font = Font.createFont(Font.TRUETYPE_FONT, fontPath);
			UnicodeFont unicodeFont = new UnicodeFont(font, Integer.valueOf(selectSize.getSelected()), checkBold.isChecked(), checkItalic.isChecked());
						
			if (checkShadow.isChecked() == true) {
				
				ColorEffect colorEffect = new ColorEffect();
				colorEffect.setColor(java.awt.Color.BLACK);
				unicodeFont.getEffects().add(colorEffect);
					
				ShadowEffect shadow = new ShadowEffect();
				shadow.setOpacity(1.0f);
				shadow.setXDistance(1);
				shadow.setYDistance(1);
				shadow.setColor(java.awt.Color.WHITE);
				unicodeFont.getEffects().add(shadow);

			} else {
				ColorEffect colorEffect = new ColorEffect();
				colorEffect.setColor(java.awt.Color.WHITE);
				unicodeFont.getEffects().add(colorEffect);
				
			}
			
			unicodeFont.addAsciiGlyphs();
			

			// Generate a temporary name for your font (Do not end with a number, it will be removed in the atlas)
			String newFontName = "font_"+fontName.toLowerCase().replace(" ", "_") +"_"+selectSize.getSelected()+"pt";
			if (checkBold.isChecked() == true) {
				newFontName += "_bold";
			}
			
			if (checkItalic.isChecked() == true) {
				newFontName += "_italic";
			}
			
			textFontName.setText(newFontName);
	
			
			// Create bitmap font
			BMFontUtil bfu = new BMFontUtil(unicodeFont);
			
			
			FileHandle handle = new FileHandle(System.getProperty("java.io.tmpdir")).child(newFontName);
			FileHandle handleFont = new FileHandle(handle.file().getAbsolutePath() + ".fnt");
			bfu.save(handle.file());
		
			FileHandle handleImage = new FileHandle(System.getProperty("java.io.tmpdir")).child(newFontName + ".png");
			
			TextField.TextFieldStyle textStyle = new TextField.TextFieldStyle();
			textStyle.cursor = game.skin.getDrawable("cursor");
	    	textStyle.selection = game.skin.getDrawable("selection");
			textStyle.background = game.skin.getDrawable("textfield");
			textStyle.fontColor = Color.YELLOW;
			textStyle.font = new BitmapFont(handleFont, handleImage, false);
			
			textFontPreview.setStyle(textStyle);
			
			// Have to do this to force clipping of font
			textFontPreview.setText(textFontPreview.getText());
			
		} catch (Exception e) {
			e.printStackTrace();
			textFontPreview.getStyle().font = game.skin.getFont("default-font");
			// Have to do this to force clipping of font
			textFontPreview.setText(textFontPreview.getText());
		}
	}
}
