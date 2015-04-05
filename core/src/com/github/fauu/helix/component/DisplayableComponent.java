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

package com.github.fauu.helix.component;

import com.artemis.Component;
import com.github.fauu.helix.datum.DisplayableUpdateRequest;
import com.github.fauu.helix.displayable.Displayable;

import java.util.LinkedList;

public class DisplayableComponent extends Component {

  private Displayable displayable;
  
  private LinkedList<DisplayableUpdateRequest> updateRequestQueue;
  
  public DisplayableComponent() {
    this.updateRequestQueue = new LinkedList<DisplayableUpdateRequest>();
  }
  
  public DisplayableComponent(Displayable displayable) {
    this();

    set(displayable);
  }
  
  public Displayable get() {
    return displayable;
  }
  
  public void set(Displayable displayable) {
    this.displayable = displayable;
  }

  public void requestUpdate(DisplayableUpdateRequest request) {
    updateRequestQueue.add(request);
  }
  
  public DisplayableUpdateRequest pollUpdateRequest() {
    return updateRequestQueue.poll();
  }

}
