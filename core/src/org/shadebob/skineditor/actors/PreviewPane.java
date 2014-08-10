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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Sort;
import com.badlogic.gdx.utils.ObjectMap.Keys;


/**
 * A table representing the right part of the interface
 * 
 * @author Yanick Bourbeau
 *
 */
public class PreviewPane extends Table {

	// private Table tab
	private SkinEditorGame game;
	// An input listener to use on items inside a scroll pane, thanks to Tomski for the hint.
	private InputListener stopTouchDown = new InputListener() {
		
		@Override
		public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
			event.stop();
			return false;
		}
	};
	
	/**
	 * 
	 */
	public PreviewPane(SkinEditorGame game) {

		super(game.skin);
		this.game = game;
		top();
		left();

	}

	/**
	 * 
	 */
	public void refresh() {

		Gdx.app.log("PreviewPane", "Refresh pane!");
		clear();

		ImageButton button = (ImageButton) game.screenMain.barWidgets.group.getChecked();
		String widget = button.getUserObject().toString();
		String widgetStyle = "com.badlogic.gdx.scenes.scene2d.ui." + widget + "$" + widget + "Style";

		

		try {
			Class<?> style = Class.forName(widgetStyle);

			ObjectMap<String, ?> styles = game.skinProject.getAll(style);
			if (styles == null) {
				Label label = new Label("No styles defined for this widget type", game.skin, "error");
				add(label).row().pad(10);

			} else {

				Keys<String> keys = styles.keys();
				Array<String> sortedKeys = new Array<String>();
				for (String key : keys) {
					sortedKeys.add(key);
				}
				sortedKeys.sort();

				for (String key : sortedKeys) {

					// We render one per key
					add(new Label(key, game.skin, "title")).left().pad(10).expandX().row();

					try {
					if (widget.equals("Label")) {

						Label w = new Label("This is a Label widget", game.skinProject, key);
						add(w).pad(10).padBottom(20).row();

					} else if (widget.equals("Button")) { // Button

						Button w = new Button(game.skinProject, key);
						add(w).width(120).height(32).pad(10).padBottom(20).row();

					} else if (widget.equals("TextButton")) { // TextButton

						TextButton w = new TextButton("This is a TextButton widget", game.skinProject, key);
						
						add(w).pad(10).padBottom(20).row();

					} else if (widget.equals("ImageButton")) { // ImageButton

						ImageButton w = new ImageButton(game.skinProject, key);
						add(w).pad(10).padBottom(20).row();

					} else if (widget.equals("CheckBox")) { // CheckBox

						CheckBox w = new CheckBox("This is a CheckBox widget", game.skinProject, key);
						w.setChecked(true);
						add(w).pad(10).padBottom(20).row();


					} else if (widget.equals("TextField")) { // TextField

						TextField w = new TextField("This is a TextField widget", game.skinProject, key);
						if (w.getStyle().fontColor == null) {
							throw new Exception("Textfield style requires a font color!");
						}
						
						w.addListener(stopTouchDown);

						add(w).pad(10).width(220).padBottom(20).row();

					} else if (widget.equals("List")) { // List

						List w = new List(game.skinProject, key);
						Array<String> items = new Array<String>();
						items.add("This is");
						items.add("a");
						items.add("List widget!");
						w.setItems(items);

						add(w).pad(10).width(220).height(120).padBottom(20).expandX().fillX().row();

					} else if (widget.equals("SelectBox")) { // SelectBox
						SelectBox<String> w = new SelectBox<String>(game.skinProject, key);
						Array<String> items = new Array<String>();
						items.add("This is");
						items.add("a");
						items.add("SelectBox widget!");
						w.setItems(items);

						add(w).pad(10).width(220).padBottom(20).expandX().fillX().row();

					} else if (widget.equals("ProgressBar")) { // ProgressBar

						ProgressBar w = new ProgressBar(0, 100, 5, false, game.skinProject, key);
						w.setValue(50);
						w.addListener(stopTouchDown);

						add(w).pad(10).width(220).padBottom(20).expandX().fillX().row();

					} else if (widget.equals("Slider")) { // Slider

						Slider w = new Slider(0, 100, 5, false, game.skinProject, key);
						add(w).pad(10).width(220).padBottom(20).expandX().fillX().row();
						w.addListener(stopTouchDown);

						Slider w2 = new Slider(0, 100, 5, true, game.skinProject, key);
						add(w2).pad(10).padBottom(20).expandX().fillX().row();
						w2.addListener(stopTouchDown);


					} else if (widget.equals("ScrollPane")) { // ScrollPane

						Table t = new Table(game.skin);
						for (int i = 0; i < 20; i++) {
							t.add("This is a ScrollPane Widget").padRight(10);
							t.add("This is a ScrollPane Widget").padRight(10);
							t.add("This is a ScrollPane Widget").row();
						}
						ScrollPane w = new ScrollPane(t, game.skinProject, key);
						w.addListener(stopTouchDown);
						w.setFlickScroll(true);
						w.setScrollbarsOnTop(true);
						w.setScrollBarPositions(true, true);
						w.setFadeScrollBars(false);
						add(w).pad(10).width(420).height(240).padBottom(20).expandX().fillX().row();

					} else if (widget.equals("SplitPane")) { // SplitPane

						for (int j = 0; j < 2; j++) {
							Table t = new Table(game.skin);
							t.setBackground(game.skin.getDrawable("default-rect"));
							Table t2 = new Table(game.skin);
							t2.setBackground(game.skin.getDrawable("default-rect"));
							for (int i = 0; i < 20; i++) {
								t.add("This is a SplitPane Widget").pad(10).row();
								t2.add("This is a SplitPane Widget").pad(10).row();
							}

							SplitPane w = new SplitPane(t, t2, (j % 2 == 0), game.skinProject, key);
							w.addListener(stopTouchDown);
							add(w).pad(10).width(220).height(160).padBottom(20).expandX().fillX();
						}
						row();

					} else if (widget.equals("Window")) { // Window

						Table t = new Table(game.skin);
						for (int i = 0; i < 5; i++) {
							t.add("This is a Window Widget").row();
						}
						Window w = new Window("This is a Window Widget", game.skinProject, key);
						w.addListener(stopTouchDown);
						w.add(t);
						add(w).pad(10).width(420).height(240).padBottom(20).expandX().fillX().row();

					} else if (widget.equals("Touchpad")) { // Touchpad

						Touchpad w = new Touchpad(0, game.skinProject, key);
						w.addListener(stopTouchDown);

						add(w).pad(10).width(200).height(200).padBottom(20).expandX().fillX().row();

					} else if (widget.equals("Tree")) { // Tree

						Tree w = new Tree(game.skinProject, key);
						Tree.Node node = new Tree.Node(new Label("This", game.skin));
						Tree.Node node1 = new Tree.Node(new Label("is", game.skin));
						Tree.Node node2 = new Tree.Node(new Label("a", game.skin));
						Tree.Node node3 = new Tree.Node(new Label("Tree", game.skin));
						Tree.Node node4 = new Tree.Node(new Label("Widget", game.skin));
						node3.add(node4);
						node2.add(node3);
						node1.add(node2);
						node.add(node1);
						w.add(node);

						w.expandAll();
						add(w).pad(10).width(200).height(200).padBottom(20).expandX().fillX().row();
					} else {
						add(new Label("Unknown widget type!", game.skin, "error")).pad(10).padBottom(20).row();
					}
					} catch(Exception e) {
						add(new Label("Please fill all required fields", game.skin, "error")).pad(10).padBottom(20).row();
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
