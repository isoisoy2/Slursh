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

import java.net.URL;
import java.io.PrintStream;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.awt.event.KeyEvent;

import haven.error.ErrorHandler;
import static haven.Utils.getprop;

public class Config {
    // public static String authuser = getprop("haven.authuser", null);
    // public static String authserv = getprop("haven.authserv", null);
    // public static String defserv = getprop("haven.defserv", "127.0.0.1");
    // public static URL resurl = geturl("haven.resurl", "");
    // public static boolean dbtext = getprop("haven.dbtext", "off").equals("on");
    // public static boolean profile = getprop("haven.profile", "off").equals("on");
    // public static boolean profilegpu = getprop("haven.profilegpu", "off").equals("on");
    // public static String resdir = getprop("haven.resdir", System.getenv("HAFEN_RESDIR"));
    // public static boolean nopreload = getprop("haven.nopreload", "no").equals("yes");
    // public static int mainport = getint("haven.mainport", 1870);
    // public static int authport = getint("haven.authport", 1871);
    // public static URL screenurl = geturl("haven.screenurl", "");
    public static String authuser = null;
    public static String authserv = null;
    public static String defserv = null;
    public static URL resurl = null;
    public static boolean dbtext = false;
    public static boolean profile = false;
    public static boolean profilegpu = false;
    public static String resdir = getprop("haven.resdir", System.getenv("HAFEN_RESDIR"));
    public static boolean nopreload = false;
    public static int mainport = 1870;
    public static int authport = 1871;
    public static URL screenurl = geturl("http://game.havenandhearth.com/mt/ss");


    public static URL mapurl = geturl("haven.mapurl", "");

    public static URL cachebase = geturl("haven.cachebase", "");
    public static URL mapbase = geturl("haven.mapbase", "");
    public static boolean bounddb = getprop("haven.bounddb", "off").equals("on");
    public static boolean par = false;
    public static boolean fscache = getprop("haven.fscache", "on").equals("on");
    public static String loadwaited = getprop("haven.loadwaited", null);
    public static String allused = getprop("haven.allused", null);

    public static boolean softres = getprop("haven.softres", "on").equals("on");
    public static byte[] authck = null;
    public static String prefspec = "hafen";
    public static final String confid = "slursh";


    public static List<LoginData> logins = new ArrayList<LoginData>();
    public static String version;
    public static String gitrev;

    public static int fontsizechat = Utils.getprefi("fontsizechat", 14);
    public static boolean fontaa = Utils.getprefb("fontaa", false);
    public static boolean usefont = Utils.getprefb("usefont", false);
    public static String font = Utils.getpref("font", "SansSerif");
    public static int fontadd = Utils.getprefi("fontadd", 0);

    public static boolean enableorthofullzoom = Utils.getprefb("enableorthofullzoom", false);
    public static boolean mapshowviewdist = Utils.getprefb("mapshowviewdist", false);
    public static boolean mapshowgrid = Utils.getprefb("mapshowgrid", false);

    public static boolean resinfo = Utils.getprefb("resinfo", false);


    static {
    	String p;
    	if((p = getprop("haven.authck", null)) != null)
    	    authck = Utils.hex2byte(p);

        try {
            InputStream in = ErrorHandler.class.getResourceAsStream("/buildinfo");
            try {
                if (in != null) {
                    java.util.Scanner s = new java.util.Scanner(in);
                    String[] binfo = s.next().split(",");
                    version = binfo[0];
                    gitrev = binfo[1];
                }
            } finally {
                in.close();
            }
        } catch (Exception e) {}

        loadLogins();
    }

    private static void loadLogins() {
        try {
            String loginsjson = Utils.getpref("logins", null);
            if (loginsjson == null)
                return;
            JSONArray larr = new JSONArray(loginsjson);
            for (int i = 0; i < larr.length(); i++) {
                JSONObject l = larr.getJSONObject(i);
                logins.add(new LoginData(l.get("name").toString(), l.get("pass").toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveLogins() {
        try {
            List<String> larr = new ArrayList<String>();
            for (LoginData ld : logins) {
                String ldjson = new JSONObject(ld, new String[] {"name", "pass"}).toString();
                larr.add(ldjson);
            }
            String jsonobjs = "";
            for (String s : larr)
                jsonobjs += s + ",";
            if (jsonobjs.length() > 0)
                jsonobjs = jsonobjs.substring(0, jsonobjs.length()-1);
            Utils.setpref("logins", "[" + jsonobjs + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static int getint(String name, int def) {
    	String val = getprop(name, null);
    	if(val == null)
    	    return(def);
    	return(Integer.parseInt(val));
    }

    private static URL geturl(String url) {
        if (url.equals(""))
            return null;
        try {
            return new URL(url);
        } catch(java.net.MalformedURLException e) {
            throw(new RuntimeException(e));
        }
    }

    private static URL geturl(String name, String def) {
    	String val = getprop(name, def);
    	if(val.equals(""))
    	    return(null);
    	try {
    	    return(new URL(val));
    	} catch(java.net.MalformedURLException e) {
    	    throw(new RuntimeException(e));
    	}
    }

    private static void usage(PrintStream out) {
	out.println("usage: haven.jar [OPTIONS] [SERVER[:PORT]]");
	out.println("Options include:");
	out.println("  -h                 Display this help");
	out.println("  -d                 Display debug text");
	out.println("  -P                 Enable profiling");
	out.println("  -G                 Enable GPU profiling");
	out.println("  -U URL             Use specified external resource URL");
	out.println("  -r DIR             Use specified resource directory (or HAVEN_RESDIR)");
	out.println("  -A AUTHSERV[:PORT] Use specified authentication server");
	out.println("  -u USER            Authenticate as USER (together with -C)");
	out.println("  -C HEXCOOKIE       Authenticate with specified hex-encoded cookie");
    }

    public static void cmdline(String[] args) {
	PosixArgs opt = PosixArgs.getopt(args, "hdPGU:r:A:u:C:");
	if(opt == null) {
	    usage(System.err);
	    System.exit(1);
	}
	for(char c : opt.parsed()) {
	    switch(c) {
	    case 'h':
		usage(System.out);
		System.exit(0);
		break;
	    case 'd':
		dbtext = true;
		break;
	    case 'P':
		profile = true;
		break;
	    case 'G':
		profilegpu = true;
		break;
	    case 'r':
		resdir = opt.arg;
		break;
	    case 'A':
		int p = opt.arg.indexOf(':');
		if(p >= 0) {
		    authserv = opt.arg.substring(0, p);
		    authport = Integer.parseInt(opt.arg.substring(p + 1));
		} else {
		    authserv = opt.arg;
		}
		break;
	    case 'U':
		try {
		    resurl = new URL(opt.arg);
		} catch(java.net.MalformedURLException e) {
		    System.err.println(e);
		    System.exit(1);
		}
		break;
	    case 'u':
		authuser = opt.arg;
		break;
	    case 'C':
		authck = Utils.hex2byte(opt.arg);
		break;
	    }
	}
	if(opt.rest.length > 0) {
	    int p = opt.rest[0].indexOf(':');
	    if(p >= 0) {
		defserv = opt.rest[0].substring(0, p);
		mainport = Integer.parseInt(opt.rest[0].substring(p + 1));
	    } else {
		defserv = opt.rest[0];
	    }
	}
    }

    static {
	Console.setscmd("stats", new Console.Command() {
		public void run(Console cons, String[] args) {
		    dbtext = Utils.parsebool(args[1]);
		}
	    });
	Console.setscmd("par", new Console.Command() {
		public void run(Console cons, String[] args) {
		    par = Utils.parsebool(args[1]);
		}
	    });
	Console.setscmd("profile", new Console.Command() {
		public void run(Console cons, String[] args) {
		    if(args[1].equals("none") || args[1].equals("off")) {
			profile = profilegpu = false;
		    } else if(args[1].equals("cpu")) {
			profile = true;
		    } else if(args[1].equals("gpu")) {
			profilegpu = true;
		    } else if(args[1].equals("all")) {
			profile = profilegpu = true;
		    }
		}
	    });
    }
}
