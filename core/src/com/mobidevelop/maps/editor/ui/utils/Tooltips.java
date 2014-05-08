/*******************************************************************************
 * Copyright 2013 See AUTHORS File
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

package com.mobidevelop.maps.editor.ui.utils;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

/**
 * Manages showing and hiding tooltips registered for actors on a {@link Stage}.
 * 
 * @author Justin Shapcott
 */
public class Tooltips {

	private ObjectMap <Actor, String> tooltips;

	private InputListener listener;

	private Stage stage;

	private Task show;

	private Task hide;

	private Label tooltip;

	private TooltipStyle tooltipStyle;

	public Tooltips(Skin skin, Stage stage) {
		this(skin.get(TooltipStyle.class), stage);
	}

	public Tooltips(Skin skin, String styleName, Stage stage) {
		this(skin.get(styleName, TooltipStyle.class), stage);
	}

	public Tooltips(TooltipStyle style, Stage stage) {
		this.tooltips =new ObjectMap<Actor, String>(); 
		this.stage = stage;
		this.tooltipStyle = style;
		this.listener = new InputListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				Actor actor = event.getTarget();
				while (actor.getParent() != null) {
					if (tooltips.containsKey(actor)) {
						if (hide != null && hide.isScheduled()) {
							hide.cancel();
						}
						if (tooltip == null) {
							final Actor finalActor = actor;
							show = new Task() {
								@Override
								public void run() {
									showTooltip(finalActor);									
								}				
							};
							Timer.schedule(show, 1);
						} else {
							showTooltip(actor);	
						}
						break;
					}
					actor = actor.getParent();
				}
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				if (show != null && show.isScheduled()) {
					show.cancel();
				}
				hideTooltip();
			}

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (show != null && show.isScheduled()) {
					show.cancel();
				}
				hideTooltipImmediate();
				return false;
			}
		};
		stage.addListener(listener);
	}

	public void registerTooltip(Actor actor, String tip) {
		tooltips.put(actor, tip);
	}

	public void unregisterToolip(Actor actor) {
		tooltips.remove(actor);
	}

	public void showTooltip(Actor actor) {
		Vector2 v = new Vector2();
		actor.localToStageCoordinates(v);
		if (tooltip == null) {
			LabelStyle style = new LabelStyle();
			style.font = tooltipStyle.font;
			style.background = tooltipStyle.background;
			style.fontColor = tooltipStyle.fontColor;
			tooltip = new Label(tooltips.get(actor), style);
			tooltip.setStyle(style);
			tooltip.pack();
			tooltip.setPosition(v.x+7.5f, v.y - tooltip.getPrefHeight() - 15);
			
			tooltip.setOriginY(tooltip.getPrefHeight());
			tooltip.setColor(1, 1, 1, 0);
			tooltip.setScale(1,0);
			tooltip.addAction(parallel(fadeIn(0.15f), scaleTo(1, 1, 0.15f)));
		} else {
			tooltip.setText(tooltips.get(actor));
			tooltip.pack();
			tooltip.setPosition(v.x+7.5f, v.y - tooltip.getPrefHeight() - 15);
		}
		stage.addActor(tooltip);
	}

	public void hideTooltip() {
		if (tooltip != null) {
			tooltip.remove();
			if (hide == null || !hide.isScheduled()) {
				hide = new Task() {
					@Override
					public void run() {
						if (tooltip != null) {
							tooltip = null;
						}
					}
				};
				Timer.schedule(hide, 1);
			}
		}
	}

	private void hideTooltipImmediate() {
		if (tooltip != null) {
			tooltip.remove();
			tooltip = null;
		}
	}

	static public class TooltipStyle {
		
		public BitmapFont font;
		public Drawable background;
		public Color fontColor;

		public TooltipStyle() {
		}

		public TooltipStyle(BitmapFont font, Drawable background, Color fontColor) {
			this.font = font;
			this.background = background;
			this.fontColor = fontColor;
		}

		public TooltipStyle (TooltipStyle style) {
			this.font = style.font;
			if (style.fontColor != null) fontColor = new Color(style.fontColor);
			background = style.background;
		}

	}

}