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

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.prefs.BackingStoreException;
import java.util.stream.Collectors;

public class OptWnd extends Window {
    private static final Text.Foundry sectionfndr = new Text.Foundry(Text.dfont.deriveFont(Font.BOLD, Text.cfg.label));
    public static final int VERTICAL_MARGIN = 10;
    public static final int HORIZONTAL_MARGIN = 5;
    public static final int VERTICAL_AUDIO_MARGIN = 5;
    public final Panel main, video, audio, keybind;
    public Panel current;

    public void chpanel(Panel p) {
    	//Coord cc = this.c.add(this.sz.div(2));
    	if(current != null)
    	    current.hide();
    	(current = p).show();
    	// pack();
    	// move(cc.sub(this.sz.div(2)));
    }

    public class PButton extends Button {
    	public final Panel tgt;
    	public final int key;

    	public PButton(int w, String title, int key, Panel tgt) {
    	    super(w, title);
    	    this.tgt = tgt;
    	    this.key = key;
    	}

    	public void click() {
    	    chpanel(tgt);
    	}

    	public boolean keydown(java.awt.event.KeyEvent ev) {
    	    if((this.key != -1) && (ev.getKeyChar() == this.key)) {
    		click();
    		return(true);
    	    }
    	    return(false);
    	}
    }

    public class Panel extends Widget {
	public Panel() {
	    visible = false;
	    c = Coord.z;
	}
    }

    private void error(String msg) {
    	GameUI gui = getparent(GameUI.class);
    	if(gui != null)
    	    gui.error(msg);
    }

    public class VideoPanel extends Panel {
    	public VideoPanel(Panel back) {
    	    super();
    	    add(new PButton(200, "Back", 27, back), new Coord(210, 360));
    	    pack();
	    }

    	public class CPanel extends Widget {
    	    public GSettings prefs;

    	    public CPanel(GSettings gprefs) {
        		this.prefs = gprefs;
                final WidgetVerticalAppender appender = new WidgetVerticalAppender(withScrollport(this, new Coord(620, 350)));
                appender.setVerticalMargin(VERTICAL_MARGIN);
                appender.setHorizontalMargin(HORIZONTAL_MARGIN);
        		int y = 0;
        		/* XXXRENDER
        		add(new CheckBox("Per-fragment lighting") {
        			{a = cf.flight.val;}

        			public void set(boolean val) {
        			    if(val) {
        				try {
        				    cf.flight.set(true);
        				} catch(GLSettings.SettingException e) {
        				    error(e.getMessage());
        				    return;
        				}
        			    } else {
        				cf.flight.set(false);
        			    }
        			    a = val;
        			    cf.dirty = true;
        			}
        		    }, new Coord(0, y));
        		y += 25;
        		*/
        		appender.add(new CheckBox("Render shadows") {
        			{a = prefs.lshadow.val;}

        			public void set(boolean val) {
        			    try {
            				GSettings np = prefs.update(null, prefs.lshadow, val);
            				ui.setgprefs(prefs = np);
        			    } catch(GLSettings.SettingException e) {
            				error(e.getMessage());
            				return;
        			    }
        			    a = val;
        			}
        		    });
        		//y += 25;
        		appender.add(new Label("Render scale"));
        		{
        		    Label dpy = add(new Label(""), new Coord(165, y + 15));
        		    final int steps = 4;
        		    appender.add(new HSlider(160, -2 * steps, 2 * steps, (int)Math.round(steps * Math.log(prefs.rscale.val) / Math.log(2.0f))) {
        			    protected void added() {
        				dpy();
        				this.c.y = dpy.c.y + ((dpy.sz.y - this.sz.y) / 2);
        			    }
        			    void dpy() {
        				dpy.settext(String.format("%.2f\u00d7", Math.pow(2, this.val / (double)steps)));
        			    }
        			    public void changed() {
        				try {
        				    float val = (float)Math.pow(2, this.val / (double)steps);
        				    ui.setgprefs(prefs = prefs.update(null, prefs.rscale, val));
        				} catch(GLSettings.SettingException e) {
        				    error(e.getMessage());
        				    return;
        				}
        				dpy();
        			    }
        			});
        		}
        		//y += 25;
        		/* XXXRENDER
        		add(new CheckBox("Antialiasing") {
        			{a = cf.fsaa.val;}

        			public void set(boolean val) {
        			    try {
        				cf.fsaa.set(val);
        			    } catch(GLSettings.SettingException e) {
        				error(e.getMessage());
        				return;
        			    }
        			    a = val;
        			    cf.dirty = true;
        			}
        		    }, new Coord(0, y));
        		y += 25;
        		add(new Label("Anisotropic filtering"), new Coord(0, y));
        		if(cf.anisotex.max() <= 1) {
        		    add(new Label("(Not supported)"), new Coord(15, y + 15));
        		} else {
        		    final Label dpy = add(new Label(""), new Coord(165, y + 15));
        		    add(new HSlider(160, (int)(cf.anisotex.min() * 2), (int)(cf.anisotex.max() * 2), (int)(cf.anisotex.val * 2)) {
        			    protected void added() {
        				dpy();
        				this.c.y = dpy.c.y + ((dpy.sz.y - this.sz.y) / 2);
        			    }
        			    void dpy() {
        				if(val < 2)
        				    dpy.settext("Off");
        				else
        				    dpy.settext(String.format("%.1f\u00d7", (val / 2.0)));
        			    }
        			    public void changed() {
        				try {
        				    cf.anisotex.set(val / 2.0f);
        				} catch(GLSettings.SettingException e) {
        				    error(e.getMessage());
        				    return;
        				}
        				dpy();
        				cf.dirty = true;
        			    }
        			}, new Coord(0, y + 15));
        		}
        		*/
        		// y += 35;
        		// add(new Button(200, "Reset to defaults") {
        		// 	public void click() {
        		// 	    ui.gprefs = GSettings.defaults();
        		// 	    curcf.destroy();
        		// 	    curcf = null;
        		// 	}
        		// }, new Coord(0, 150));

                pack();
    	    }
    	}

    	private CPanel curcf = null;

    	public void draw(GOut g) {
    	    if((curcf == null) || (ui.gprefs != curcf.prefs)) {
        		if(curcf != null)
        		    curcf.destroy();
        		curcf = add(new CPanel(ui.gprefs), Coord.z);
    	    }
    	    super.draw(g);
    	}
    }

    private static final Text kbtt = RichText.render("$col[255,255,0]{Escape}: Cancel input\n" +
						     "$col[255,255,0]{Backspace}: Revert to default\n" +
						     "$col[255,255,0]{Delete}: Disable keybinding", 0);
        public class BindingPanel extends Panel {
    	private int addbtn(Widget cont, String nm, KeyBinding cmd, int y) {
    	    Widget btn = cont.add(new SetButton(175, cmd), 100, y);
    	    cont.adda(new Label(nm), 0, y + (btn.sz.y / 2), 0, 0.5);
    	    return(y + 30);
    	}

    	public BindingPanel(Panel back) {
    	    super();
    	    Widget cont = add(new Scrollport(new Coord(300, 300))).cont;
    	    int y = 0;
    	    cont.adda(new Label("Main menu"), cont.sz.x / 2, y, 0.5, 0); y += 20;
    	    y = addbtn(cont, "Inventory", GameUI.kb_inv, y);
    	    y = addbtn(cont, "Equipment", GameUI.kb_equ, y);
    	    y = addbtn(cont, "Character sheet", GameUI.kb_chr, y);
    	    y = addbtn(cont, "Map window", GameUI.kb_map, y);
    	    y = addbtn(cont, "Kith & Kin", GameUI.kb_bud, y);
    	    y = addbtn(cont, "Options", GameUI.kb_opt, y);
    	    y = addbtn(cont, "Search actions", GameUI.kb_srch, y);
    	    y = addbtn(cont, "Toggle chat", GameUI.kb_chat, y);
    	    y = addbtn(cont, "Quick chat", ChatUI.kb_quick, y);
    	    y = addbtn(cont, "Display claims", GameUI.kb_claim, y);
    	    y = addbtn(cont, "Display villages", GameUI.kb_vil, y);
    	    y = addbtn(cont, "Display realms", GameUI.kb_rlm, y);
    	    y = addbtn(cont, "Take screenshot", GameUI.kb_shoot, y);
    	    y = addbtn(cont, "Toggle UI", GameUI.kb_hide, y);
    	    y += 10;
    	    cont.adda(new Label("Camera control"), cont.sz.x / 2, y, 0.5, 0); y += 20;
    	    y = addbtn(cont, "Rotate left", MapView.kb_camleft, y);
    	    y = addbtn(cont, "Rotate right", MapView.kb_camright, y);
    	    y = addbtn(cont, "Zoom in", MapView.kb_camin, y);
    	    y = addbtn(cont, "Zoom out", MapView.kb_camout, y);
    	    y = addbtn(cont, "Reset", MapView.kb_camreset, y);
    	    y += 10;
    	    cont.adda(new Label("Walking speed"), cont.sz.x / 2, y, 0.5, 0); y += 20;
    	    y = addbtn(cont, "Increase speed", Speedget.kb_speedup, y);
    	    y = addbtn(cont, "Decrease speed", Speedget.kb_speeddn, y);
    	    for(int i = 0; i < 4; i++)
    		y = addbtn(cont, String.format("Set speed %d", i + 1), Speedget.kb_speeds[i], y);
    	    y += 10;
    	    cont.adda(new Label("Combat actions"), cont.sz.x / 2, y, 0.5, 0); y += 20;
    	    for(int i = 0; i < Fightsess.kb_acts.length; i++)
    		y = addbtn(cont, String.format("Combat action %d", i + 1), Fightsess.kb_acts[i], y);
    	    y = addbtn(cont, "Switch targets", Fightsess.kb_relcycle, y);
    	    y += 10;
    	    y = cont.sz.y + 10;
    	    adda(new PointBind(200), cont.sz.x / 2, y, 0.5, 0); y += 30;
    	    adda(new PButton(200, "Back", 27, back), cont.sz.x / 2, y, 0.5, 0); y += 30;
    	    pack();
    	}

    	public class SetButton extends KeyMatch.Capture {
    	    public final KeyBinding cmd;

    	    public SetButton(int w, KeyBinding cmd) {
    		super(w, cmd.key());
    		this.cmd = cmd;
    	    }

    	    public void set(KeyMatch key) {
    		super.set(key);
    		cmd.set(key);
    	    }

    	    protected KeyMatch mkmatch(KeyEvent ev) {
    		return(KeyMatch.forevent(ev, ~cmd.modign));
    	    }

    	    protected boolean handle(KeyEvent ev) {
    		if(ev.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
    		    cmd.set(null);
    		    super.set(cmd.key());
    		    return(true);
    		}
    		return(super.handle(ev));
    	    }

    	    public Object tooltip(Coord c, Widget prev) {
    		return(kbtt.tex());
    	    }
    	}
    }


    public static class PointBind extends Button {
    	public static final String msg = "Bind other elements...";
    	public static final Resource curs = Resource.local().loadwait("gfx/hud/curs/wrench");
    	private UI.Grab mg, kg;
    	private KeyBinding cmd;

    	public PointBind(int w) {
    	    super(w, msg, false);
    	    tooltip = RichText.render("Bind a key to an element not listed above, such as an action-menu " +
    				      "button. Click the element to bind, and then press the key to bind to it. " +
    				      "Right-click to stop rebinding.",
    				      300);
    	}

    	public void click() {
    	    if(mg == null) {
    		change("Click element...");
    		mg = ui.grabmouse(this);
    	    } else if(kg != null) {
    		kg.remove();
    		kg = null;
    		change(msg);
    	    }
    	}

    	private boolean handle(KeyEvent ev) {
    	    switch(ev.getKeyCode()) {
    	    case KeyEvent.VK_SHIFT: case KeyEvent.VK_CONTROL: case KeyEvent.VK_ALT:
    	    case KeyEvent.VK_META: case KeyEvent.VK_WINDOWS:
    		return(false);
    	    }
    	    int code = ev.getKeyCode();
    	    if(code == KeyEvent.VK_ESCAPE) {
    		return(true);
    	    }
    	    if(code == KeyEvent.VK_BACK_SPACE) {
    		cmd.set(null);
    		return(true);
    	    }
    	    if(code == KeyEvent.VK_DELETE) {
    		cmd.set(KeyMatch.nil);
    		return(true);
    	    }
    	    KeyMatch key = KeyMatch.forevent(ev, ~cmd.modign);
    	    if(key != null)
    		cmd.set(key);
    	    return(true);
    	}

    	public boolean mousedown(Coord c, int btn) {
    	    if(mg == null)
    		return(super.mousedown(c, btn));
    	    Coord gc = ui.mc;
    	    if(btn == 1) {
    		this.cmd = KeyBinding.Bindable.getbinding(ui.root, gc);
    		return(true);
    	    }
    	    if(btn == 3) {
    		mg.remove();
    		mg = null;
    		change(msg);
    		return(true);
    	    }
    	    return(false);
    	}

    	public boolean mouseup(Coord c, int btn) {
    	    if(mg == null)
    		return(super.mouseup(c, btn));
    	    Coord gc = ui.mc;
    	    if(btn == 1) {
    		if((this.cmd != null) && (KeyBinding.Bindable.getbinding(ui.root, gc) == this.cmd)) {
    		    mg.remove();
    		    mg = null;
    		    kg = ui.grabkeys(this);
    		    change("Press key...");
    		} else {
    		    this.cmd = null;
    		}
    		return(true);
    	    }
    	    if(btn == 3)
    		return(true);
    	    return(false);
    	}

    	public Resource getcurs(Coord c) {
    	    if(mg == null)
    		return(null);
    	    return(curs);
    	}

    	public boolean keydown(KeyEvent ev) {
    	    if(kg == null)
    		return(super.keydown(ev));
    	    if(handle(ev)) {
    		kg.remove();
    		kg = null;
    		cmd = null;
    		change("Click another element...");
    		mg = ui.grabmouse(this);
    	    }
    	    return(true);
    	}
    }

    public OptWnd(boolean gopts) {
    	super(new Coord(620, 400), "Options", true);
    	main = add(new Panel());
    	video = add(new VideoPanel(main));
    	audio = add(new Panel());
    	keybind = add(new BindingPanel(main));
        // display = add(new Panel());
        // map = add(new Panel());
        // general = add(new Panel());
        // combat = add(new Panel());
        // control = add(new Panel());
        // mapping = add(new Panel());
        // uis = add(new Panel());
        // quality = add(new Panel());
        // flowermenus = add(new Panel());
        // soundalarms = add(new Panel());
    	//int y;
        initMain(gopts);
        initAudio();
        // initDisplay();
        // initMinimap();
        // initGeneral();
        // initCombat();
        // initControl();
        // initUis();
        // initQuality();
        // initFlowermenus();
        // initSoundAlarms();
        // initKeyBind();
        // initMapping();

    	// main.add(new PButton(200, "Video settings", 'v', video), new Coord(0, 0));
    	// main.add(new PButton(200, "Audio settings", 'a', audio), new Coord(0, 30));
    	// main.add(new PButton(200, "Keybindings", 'k', keybind), new Coord(0, 60));
    	// if(gopts) {
    	//     main.add(new Button(200, "Switch character") {
    	// 	    public void click() {
    	// 		getparent(GameUI.class).act("lo", "cs");
    	// 	    }
    	// 	}, new Coord(0, 120));
    	//     main.add(new Button(200, "Log out") {
    	// 	    public void click() {
    	// 		getparent(GameUI.class).act("lo");
    	// 	    }
    	// 	}, new Coord(0, 150));
    	// }
    	// main.add(new Button(200, "Close") {
    	// 	public void click() {
    	// 	    OptWnd.this.hide();
    	// 	}
    	//     }, new Coord(0, 180));
    	// main.pack();
        //
    	// y = 0;
    	// audio.add(new Label("Master audio volume"), new Coord(0, y));
    	// y += 15;
    	// audio.add(new HSlider(200, 0, 1000, (int)(Audio.volume * 1000)) {
    	// 	public void changed() {
    	// 	    Audio.setvolume(val / 1000.0);
    	// 	}
    	//     }, new Coord(0, y));
    	// y += 30;
    	// audio.add(new Label("In-game event volume"), new Coord(0, y));
    	// y += 15;
    	// audio.add(new HSlider(200, 0, 1000, 0) {
    	// 	protected void attach(UI ui) {
    	// 	    super.attach(ui);
    	// 	    val = (int)(ui.audio.pos.volume * 1000);
    	// 	}
    	// 	public void changed() {
    	// 	    ui.audio.pos.setvolume(val / 1000.0);
    	// 	}
    	//     }, new Coord(0, y));
    	// y += 20;
    	// audio.add(new Label("Ambient volume"), new Coord(0, y));
    	// y += 15;
    	// audio.add(new HSlider(200, 0, 1000, 0) {
    	// 	protected void attach(UI ui) {
    	// 	    super.attach(ui);
    	// 	    val = (int)(ui.audio.amb.volume * 1000);
    	// 	}
    	// 	public void changed() {
    	// 	    ui.audio.amb.setvolume(val / 1000.0);
    	// 	}
    	//     }, new Coord(0, y));
    	// y += 35;
    	// audio.add(new PButton(200, "Back", 27, main), new Coord(0, 180));
    	// audio.pack();

    	chpanel(main);
    }
    private void initMain(boolean gopts) {
        main.add(new PButton(200, "Video settings", 'v', video), new Coord(0, 0));
        main.add(new PButton(200, "Audio settings", 'a', audio), new Coord(0, 30));
        //main.add(new PButton(200, "Display settings", 'd', display), new Coord(0, 60));
        // main.add(new PButton(200, "Minimap settings", 'm', map), new Coord(0, 90));
        // main.add(new PButton(200, "General settings", 'g', general), new Coord(210, 0));
        // main.add(new PButton(200, "Combat settings", 'c', combat), new Coord(210, 30));
        // main.add(new PButton(200, "Control settings", 'k', control), new Coord(210, 60));
        // main.add(new PButton(200, "Mapping settings", 'e', mapping), new Coord(210, 90));
        // main.add(new PButton(200, "UI settings", 'u', uis), new Coord(210, 120));
        // main.add(new PButton(200, "Quality settings", 'q', quality), new Coord(420, 0));
        // main.add(new PButton(200, "Menu settings", 'f', flowermenus), new Coord(420, 30));
        // main.add(new PButton(200, "Sound alarms", 's', soundalarms), new Coord(420, 60));
        // main.add(new PButton(200, "Key Bindings", 'b', keybind), new Coord(420, 90));
        // main.add(new PButton(200, "Established Shortcuts", 'l', shorts), new Coord(420,120));
        // main.add(new PButton(200, "Slush Additions", 'h', slush), new Coord(0,120));
        if (gopts) {
            main.add(new Button(200, "Switch character") {
                public void click() {
                    GameUI gui = gameui();
                    gui.act("lo", "cs");
                    if (gui != null & gui.map != null)
                        gui.map.canceltasks();
                }
            }, new Coord(210, 300));
            main.add(new Button(200, "Log out") {
                public void click() {
                    GameUI gui = gameui();
                    gui.act("lo");
                    if (gui != null & gui.map != null)
                        gui.map.canceltasks();
                }
            }, new Coord(210, 330));
        }
        main.add(new Button(200, "Close") {
            public void click() {
                OptWnd.this.hide();
            }
        }, new Coord(210, 360));
        main.pack();
    }

    private void initAudio() {
        initAudioFirstColumn();
        audio.add(new PButton(200, "Back", 27, main), new Coord(210, 360));
        audio.pack();
    }

    private void initAudioFirstColumn() {
        final WidgetVerticalAppender appender = new WidgetVerticalAppender(withScrollport(audio, new Coord(620, 350)));
        appender.setVerticalMargin(0);
        appender.add(new Label("Master audio volume"));
        appender.setVerticalMargin(VERTICAL_AUDIO_MARGIN);
        appender.add(new HSlider(200, 0, 1000, (int) (Audio.volume * 1000)) {
            public void changed() {
                Audio.setvolume(val / 1000.0);
            }
        });
        appender.setVerticalMargin(0);
        appender.add(new Label("In-game event volume"));
        appender.setVerticalMargin(VERTICAL_AUDIO_MARGIN);
        appender.add(new HSlider(200, 0, 1000, 0) {
            protected void attach(UI ui) {
                super.attach(ui);
                val = (int) (ui.audio.pos.volume * 1000);
            }

            public void changed() {
                ui.audio.pos.setvolume(val / 1000.0);
            }
        });
        appender.setVerticalMargin(0);
        appender.add(new Label("Ambient volume"));
        appender.setVerticalMargin(VERTICAL_AUDIO_MARGIN);
        appender.add(new HSlider(200, 0, 1000, 0) {
            protected void attach(UI ui) {
                super.attach(ui);
                val = (int) (ui.audio.amb.volume * 1000);
            }

            public void changed() {
                ui.audio.amb.setvolume(val / 1000.0);
            }
        });
        // appender.setVerticalMargin(0);
        // appender.add(new Label("Timers alarm volume"));
        // appender.setVerticalMargin(VERTICAL_AUDIO_MARGIN);
        // appender.add(new HSlider(200, 0, 1000, 0) {
        //     protected void attach(UI ui) {
        //         super.attach(ui);
        //         val = (int) (Config.timersalarmvol * 1000);
        //     }
        //
        //     public void changed() {
        //         double vol = val / 1000.0;
        //         Config.timersalarmvol = vol;
        //         Utils.setprefd("timersalarmvol", vol);
        //     }
        // });
        // appender.setVerticalMargin(0);
        // appender.add(new Label("'Chip' sound volume"));
        // appender.setVerticalMargin(VERTICAL_AUDIO_MARGIN);
        // appender.add(new HSlider(200, 0, 1000, 0) {
        //     protected void attach(UI ui) {
        //         super.attach(ui);
        //         val = (int) (Config.sfxchipvol * 1000);
        //     }
        //
        //     public void changed() {
        //         double vol = val / 1000.0;
        //         Config.sfxchipvol = vol;
        //         Utils.setprefd("sfxchipvol", vol);
        //     }
        // });
        // appender.setVerticalMargin(0);
        // appender.add(new Label("Quern sound volume"));
        // appender.setVerticalMargin(VERTICAL_AUDIO_MARGIN);
        // appender.add(new HSlider(200, 0, 1000, 0) {
        //     protected void attach(UI ui) {
        //         super.attach(ui);
        //         val = (int) (Config.sfxquernvol * 1000);
        //     }
        //
        //     public void changed() {
        //         double vol = val / 1000.0;
        //         Config.sfxquernvol = vol;
        //         Utils.setprefd("sfxquernvol", vol);
        //     }
        // });
        // appender.setVerticalMargin(0);
        // appender.add(new Label("'Whip' sound volume"));
        // appender.setVerticalMargin(VERTICAL_AUDIO_MARGIN);
        // appender.add(new HSlider(200, 0, 1000, 0) {
        //     protected void attach(UI ui) {
        //         super.attach(ui);
        //         val = (int) (Config.sfxwhipvol * 1000);
        //     }
        //
        //     public void changed() {
        //         double vol = val / 1000.0;
        //         Config.sfxwhipvol = vol;
        //         Utils.setprefd("sfxwhipvol", vol);
        //     }
        // });
        // appender.setVerticalMargin(0);
        // appender.add(new Label("'Squeak' sound volume"));
        // appender.setVerticalMargin(VERTICAL_AUDIO_MARGIN);
        // appender.add(new HSlider(200, 0, 1000, 0) {
        //     protected void attach(UI ui) {
        //         super.attach(ui);
        //         val = (int) (Config.sfxsquvol * 1000);
        //     }
        //
        //     public void changed() {
        //         double vol = val / 1000.0;
        //         Config.sfxsquvol = vol;
        //         Utils.setprefd("sfxsquvol", vol);
        //     }
        // });
        // appender.setVerticalMargin(0);
        // appender.add(new Label("'Clap/Whistle' sound volume"));
        // appender.setVerticalMargin(VERTICAL_AUDIO_MARGIN);
        // appender.add(new HSlider(200, 0, 1000, 0) {
        //     protected void attach(UI ui) {
        //         super.attach(ui);
        //         val = (int) (Config.sfxclwhvol * 1000);
        //     }
        //
        //     public void changed() {
        //         double vol = val / 1000.0;
        //         Config.sfxclwhvol = vol;
        //         Utils.setprefd("sfxclwhvol", vol);
        //     }
        // });
        // appender.setVerticalMargin(0);
        // appender.add(new Label("Fireplace sound volume (req. restart)"));
        // appender.setVerticalMargin(VERTICAL_AUDIO_MARGIN);
        // appender.add(new HSlider(200, 0, 1000, 0) {
        //     protected void attach(UI ui) {
        //         super.attach(ui);
        //         val = (int) (Config.sfxfirevol * 1000);
        //     }
        //
        //     public void changed() {
        //         double vol = val / 1000.0;
        //         Config.sfxfirevol = vol;
        //         Utils.setprefd("sfxfirevol", vol);
        //     }
        // });
        // appender.setVerticalMargin(0);
        // appender.add(new Label("Bees sound volume (req. logout)"));
        // appender.setVerticalMargin(VERTICAL_AUDIO_MARGIN);
        // appender.add(new HSlider(200, 0, 1000, 0) {
        //     protected void attach(UI ui) {
        //         super.attach(ui);
        //         val = (int) (Config.sfxbeevol * 1000);
        //     }
        //
        //     public void changed() {
        //         double vol = val / 1000.0;
        //         Config.sfxbeevol = vol;
        //         Utils.setprefd("sfxbeevol", vol);
        //     }
        // });
    }

    static private Scrollport.Scrollcont withScrollport(Widget widget, Coord sz) {
        final Scrollport scroll = new Scrollport(sz);
        widget.add(scroll, new Coord(0, 0));
        return scroll.cont;
    }

    public OptWnd() {
	this(true);
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
	if((sender == this) && (msg == "close")) {
	    hide();
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }

    public void show() {
	chpanel(main);
	super.show();
    }
}
