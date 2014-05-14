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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.Window;


/**
 * A class that check if a scene2d-ui widget style field is optional or not.
 * I think I could store it in a JSON file instead. It will do for now.
 * 
 * @author Yanick Bourbeau
 *
 */
public class OptionalChecker {

	public boolean isFieldOptional(Class objClass, String fieldName) {
		
		if (objClass == Label.LabelStyle.class) {
			
			if (fieldName.equals("font")) {
				return false;
			} else {
				return true;
			}
			
		} else if (objClass == Button.ButtonStyle.class) {
			// They are all optional			
			return true;
			
		} else if (objClass == TextButton.TextButtonStyle.class) {
			
			if (fieldName.equals("font")) {
				return false;
			} else {
				return true;
			}
			
		} else if (objClass == ImageButton.ImageButtonStyle.class) {
			// They are all optional			
			return true;
			
		} else if (objClass == CheckBox.CheckBoxStyle.class) {
			
			if (fieldName.equals("checkboxOff") || fieldName.equals("checkboxOn")) {
				return false;
			} else {
				return true;
			}
			
		} else if (objClass == TextField.TextFieldStyle.class) {
			
			if (fieldName.equals("font") || fieldName.equals("fontColor")) {
				return false;
			} else {
				return true;
			}
			
		} else if (objClass == List.ListStyle.class) {
			
			if (fieldName.equals("background")) {
				return true;
			} else {
				return false;
			}
			
		} else if (objClass == SelectBox.SelectBoxStyle.class) {
			
			if (fieldName.equals("background") || fieldName.equals("font") || fieldName.equals("fontColor") || 
					fieldName.equals("listStyle") || fieldName.equals("scrollStyle")) {
				return false;
			} else {
				return true;
			}
			
		} else if (objClass == ProgressBar.ProgressBarStyle.class) {
			
			if (fieldName.equals("background")) {
				return false;
			} else {
				return true;
			}
			
		} else if (objClass == Slider.SliderStyle.class) {
			
			if (fieldName.equals("background")) {
				return false;
			} else {
				return true;
			}
			
		} else if (objClass == ScrollPane.ScrollPaneStyle.class) {
			
			// Everything is optional
			return true;
			
		} else if (objClass == SplitPane.SplitPaneStyle.class) {
			
			// Nothing is optional
			return false;
			
		} else if (objClass == Window.WindowStyle.class) {
			
			if (fieldName.equals("titleFont")) {
				return false;
			} else {
				return true;
			}
			
		} else if (objClass == Touchpad.TouchpadStyle.class) {
			
			if (fieldName.equals("background")) {
				return false;
			} else {
				return true;
			}
			
		} else if (objClass == Tree.TreeStyle.class) {
			if (fieldName.equals("plus") || fieldName.equals("minus")) {
				return false;
			} else {
				return true;
			}
		}
		

		return false;
	}
}
