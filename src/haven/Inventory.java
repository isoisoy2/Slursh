/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import haven.res.ui.tt.q.qbuff.QBuff;

import java.util.*;

public class Inventory extends Widget implements DTarget {
    public static final Tex invsq = Resource.loadtex("gfx/hud/invsq");
    public static final Coord sqsz = new Coord(33, 33);
    public boolean dropul = true;
    public Coord isz;
    Map<GItem, WItem> wmap = new HashMap<GItem, WItem>();

    @RName("inv")
    public static class $_ implements Factory {
	public Widget create(UI ui, Object[] args) {
	    return(new Inventory((Coord)args[0]));
	}
    }

    public void draw(GOut g) {
	Coord c = new Coord();
	for(c.y = 0; c.y < isz.y; c.y++) {
	    for(c.x = 0; c.x < isz.x; c.x++) {
		g.image(invsq, c.mul(sqsz));
	    }
	}
	super.draw(g);
    }

    public Inventory(Coord sz) {
	super(invsq.sz().add(new Coord(-1, -1)).mul(sz).add(new Coord(1, 1)));
	isz = sz;
    }

    public boolean mousewheel(Coord c, int amount) {
	if(ui.modshift) {
	    Inventory minv = getparent(GameUI.class).maininv;
	    if(minv != this) {
		if(amount < 0)
		    wdgmsg("invxf", minv.wdgid(), 1);
		else if(amount > 0)
		    minv.wdgmsg("invxf", this.wdgid(), 1);
	    }
	}
	return(true);
    }

    public void addchild(Widget child, Object... args) {
	add(child);
	Coord c = (Coord)args[0];
	if(child instanceof GItem) {
	    GItem i = (GItem)child;
	    wmap.put(i, add(new WItem(i), c.mul(sqsz).add(1, 1)));
	}
    }

    public void cdestroy(Widget w) {
	super.cdestroy(w);
	if(w instanceof GItem) {
	    GItem i = (GItem)w;
	    ui.destroy(wmap.remove(i));
	}
    }

    public boolean drop(Coord cc, Coord ul) {
    	Coord dc;
    	if(dropul)
    	    dc = ul.add(sqsz.div(2)).div(sqsz);
    	else
    	    dc = cc.div(sqsz);
    	wdgmsg("drop", dc);
    	return(true);
    }

    public boolean iteminteract(Coord cc, Coord ul) {
	return(false);
    }

    public void uimsg(String msg, Object... args) {
    	if(msg == "sz") {
    	    isz = (Coord)args[0];
    	    resize(invsq.sz().add(new Coord(-1, -1)).mul(isz).add(new Coord(1, 1)));
    	} else if(msg == "mode") {
    	    dropul = (((Integer)args[0]) == 0);
    	} else {
    	    super.uimsg(msg, args);
    	}
    }
    /* Following getItem* methods do partial matching of the name *on purpose*.
       Because when localization is turned on, original English name will be in the brackets
       next to the translation
    */
    public List<WItem> getItemsPartial(String... names) {
        List<WItem> items = new ArrayList<WItem>();
        for (Widget wdg = child; wdg != null; wdg = wdg.next) {
            if (wdg instanceof WItem) {
                String wdgname = ((WItem)wdg).item.getname();
                for (String name : names) {
                    if (wdgname.contains(name)) {
                        items.add((WItem) wdg);
                        break;
                    }
                }
            }
        }
        return items;
    }

    public WItem getItemPartial(String name) {
        for (Widget wdg = child; wdg != null; wdg = wdg.next) {
            if (wdg instanceof WItem) {
                String wdgname = ((WItem)wdg).item.getname();
                if (wdgname.contains(name))
                    return (WItem) wdg;
            }
        }
        return null;
    }

    public int getItemPartialCount(String name) {
        int count = 0;
        for (Widget wdg = child; wdg != null; wdg = wdg.next) {
            if (wdg instanceof WItem) {
                String wdgname = ((WItem)wdg).item.getname();
                if (wdgname.contains(name))
                    count++;
            }
        }
        return count;
    }

    public int getFreeSpace() {
        int feespace = isz.x * isz.y;
        for (Widget wdg = child; wdg != null; wdg = wdg.next) {
            if (wdg instanceof WItem)
                feespace -= (wdg.sz.x * wdg.sz.y) / (sqsz.x * sqsz.y);
        }
        return feespace;
    }

    public boolean drink(int threshold) {
        IMeter.Meter stam = gameui().getmeter("stam", 0);
        if (stam == null || stam.a > threshold)
            return false;

        List<WItem> containers = getItemsPartial("Waterskin", "Waterflask", "Kuksa","Cup");
        for (WItem wi : containers) {
            ItemInfo.Contents cont = wi.item.getcontents();
            if (cont != null) {
                FlowerMenu.setNextSelection("Drink");
                ui.lcc = wi.rootpos();
                wi.item.wdgmsg("iact", wi.c, 0);
                return true;
            }
        }

        return false;
    }
}
