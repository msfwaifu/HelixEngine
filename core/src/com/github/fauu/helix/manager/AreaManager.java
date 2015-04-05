/*
 * Copyright (C) 2014, 2015 Helix Engine Developers 
 * (http://github.com/fauu/HelixEngine)
 *
 * This software is licensed under the GNU General Public License
 * (version 3 or later). See the COPYING file in this distribution.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 *
 * Authored by: Piotr Grabowski <fau999@gmail.com>
 */

package com.github.fauu.helix.manager;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Manager;
import com.artemis.annotations.Wire;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.fauu.helix.PassageAction;
import com.github.fauu.helix.TilePermission;
import com.github.fauu.helix.component.*;
import com.github.fauu.helix.datum.Tile;
import com.github.fauu.helix.displayable.AreaDisplayable;
import com.github.fauu.helix.json.wrapper.AreaWrapper;
import com.github.fauu.helix.json.wrapper.TileWrapper;
import com.github.fauu.helix.util.IntVector2;

import java.io.FileWriter;
import java.io.IOException;

public class AreaManager extends Manager {

  @Wire
  private ComponentMapper<DimensionsComponent> dimensionsMapper;

  @Wire
  private ComponentMapper<DisplayableComponent> displayableMapper;

  @Wire
  private ComponentMapper<NameComponent> nameMapper;

  @Wire
  private ComponentMapper<TilesComponent> tilesMapper;

  @Wire
  private AssetManager assetManager;

  private Entity area;

  public void create(String name, int width, int length) {
    Json json = new Json();

    IntVector2 dimensions = new IntVector2(width, length);

    FileHandle file = Gdx.files.internal("area/" + name + ".json");

    try {
      json.setWriter(new JsonWriter(new FileWriter(file.file())));
    } catch (IOException e) {
      e.printStackTrace();
    }

    json.writeObjectStart();
    json.writeValue("width", dimensions.x);
    json.writeValue("length", dimensions.y);
    json.writeArrayStart("tiles");
    for (int y = 0; y < dimensions.y; y++) {
      for (int x = 0; x < dimensions.x; x++) {
        json.writeObjectStart();
        json.writeValue("permissions", TilePermission.LEVEL0.toString());
        json.writeObjectEnd();
      }
    }
    json.writeArrayEnd();
    json.writeObjectEnd();

    try {
      json.getWriter().close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void load(String name) {
    loadFromFile(Gdx.files.internal("area/" + name + ".json"), name);
  }
  
  public void loadFromFile(FileHandle file, String name) {
    Json json = new Json();

    AreaWrapper areaWrapper = json.fromJson(AreaWrapper.class, file);

    Tile[][] tiles = new Tile[areaWrapper.length][areaWrapper.width];
    int i = 0;
    for (TileWrapper wrapper : areaWrapper.tiles) {
      Tile tile = new Tile();

      tile.setPermissions(wrapper.permissions);

      tiles[i / areaWrapper.width][i % areaWrapper.width] = tile;

      i++;
    }

    String modelPath = "model/" + name + ".g3db";

    assetManager.load(modelPath, Model.class);
    assetManager.finishLoading();

    Model model = assetManager.get(modelPath, Model.class);
    for (Material m : model.materials) {
      m.set(new FloatAttribute(FloatAttribute.AlphaTest, 0.1f));
    }

    Entity area = world.createEntity()
                       .edit()
                       .add(new TilesComponent(tiles))
                       .add(new DimensionsComponent(
                           new IntVector2(areaWrapper.width,
                                          areaWrapper.length)))
                       .add(new NameComponent(name))
                       .add(new DisplayableComponent(new AreaDisplayable(model)))
                       .add(new VisibilityComponent())
                       .getEntity();
    world.getManager(TagManager.class).register("area", area);

    this.area = area;
  }

  public void save() {
    Json json = new Json();

    String name = nameMapper.get(area).get();

    FileHandle file = Gdx.files.internal("area/" + name + ".json");

    IntVector2 dimensions = dimensionsMapper.get(area).get();

    try {
      json.setWriter(new JsonWriter(new FileWriter(file.file())));
    } catch (IOException e) {
      e.printStackTrace();
    }

    json.writeObjectStart();
    json.writeValue("width", dimensions.x);
    json.writeValue("length", dimensions.y);
    json.writeArrayStart("tiles");
    Tile[][] tiles = tilesMapper.get(area).get();
    for (int y = 0; y < dimensions.y; y++) {
      for (int x = 0; x < dimensions.x; x++) {
        json.writeObjectStart();
        json.writeValue("permissions", tiles[y][x].getPermissions().toString());
        json.writeObjectEnd();
      }
    }
    json.writeArrayEnd();
    json.writeObjectEnd();

    try {
      json.getWriter().close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void unloadCurrent() {
    if (area != null) {
      area.deleteFromWorld();

      area = null;
    }
  }

  public static String constructPassageAnimationId(IntVector2 passageCoords,
                                                   PassageAction action) {
    return "passage-" +
           action.toString().toLowerCase() +
           "-" + String.valueOf(passageCoords.x) +
           "-" + String.valueOf(passageCoords.y);
  }

  public boolean isAreaLoaded() {
    return area != null;
  }

  public Entity getArea() {
    return area;
  }

}
