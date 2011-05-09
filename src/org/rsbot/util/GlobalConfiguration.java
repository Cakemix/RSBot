package org.rsbot.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;

import javax.swing.filechooser.FileSystemView;

import org.rsbot.log.LogFormatter;
import org.rsbot.log.SystemConsoleHandler;
import org.rsbot.log.TextAreaLogHandler;

public class GlobalConfiguration {

	public enum OperatingSystem {
		MAC, WINDOWS, LINUX, UNKNOWN
	}

	public static class Paths {
		public static class Resources {
			public static final String ROOT = "resources";
			public static final String SCRIPTS = Paths.SCRIPTS_NAME_SRC + "/";
			public static final String ROOT_IMG = ROOT + "/images";
			public static final String ICON = ROOT_IMG + "/icon.png";
			public static final String ICON_APPADD = ROOT_IMG + "/application_add.png";
			public static final String ICON_APPDELETE = ROOT_IMG + "/application_delete.png";
			public static final String ICON_DELETE = ROOT_IMG + "/delete.png";
			public static final String ICON_PLAY = ROOT_IMG + "/control_play_blue.png";
			public static final String ICON_PAUSE = ROOT_IMG + "/control_pause.png";
			public static final String ICON_ADD = ROOT_IMG + "/add.png";
			public static final String ICON_HOME = ROOT_IMG + "/home.png";
			public static final String ICON_BOT = ROOT_IMG + "/bot.png";
			public static final String ICON_CLOSE = ROOT_IMG + "/close.png";
			public static final String ICON_TICK = ROOT_IMG + "/tick.png";
			public static final String ICON_MOUSE = ROOT_IMG + "/mouse.png";
			public static final String ICON_PHOTO = ROOT_IMG + "/photo.png";
			public static final String ICON_REPORTKEY = ROOT_IMG + "/report_key.png";
			public static final String ICON_INFO = ROOT_IMG + "/information.png";
			public static final String ICON_KEYBOARD = ROOT_IMG + "/keyboard.png";
			public static final String ICON_CONNECT = ROOT_IMG + "/connect.png";
			public static final String ICON_DISCONNECT = ROOT_IMG + "/disconnect.png";
			public static final String ICON_START = ROOT_IMG + "/control_play.png";
			public static final String ICON_SCRIPT_ADD = ROOT_IMG + "/script_add.png";
			public static final String ICON_SCRIPT_BDL = ROOT_IMG + "/script_bdl.png";
			public static final String ICON_SCRIPT_DRM = ROOT_IMG + "/script_drm.png";
			public static final String ICON_SCRIPT_PRE = ROOT_IMG + "/script_pre.png";
			public static final String ICON_SCRIPT_SRC = ROOT_IMG + "/script_src.png";
			public static final String ICON_USEREDIT = ROOT_IMG + "/user_edit.png";
			public static final String ICON_WEBLINK = ROOT_IMG + "/world_link.png";

			public static final String VERSION = ROOT + "/version.txt";
		}

		public static class URLs {
			private static final String BASE = "http://links.powerbot.org/";
			public static final String DOWNLOAD = BASE + "download";
			public static final String UPDATE = BASE + "modscript";
			public static final String WEB = BASE + "webwalker.gz";
			public static final String VERSION = BASE + "version.txt";
			public static final String PROJECT = BASE + "git-project";
			public static final String SITE = BASE + "site";
			public static final String SDN_CONTROL = BASE + "sdn-control";
			public static final String AD_INFO = BASE + "botad-info";
		}

		public static final String ROOT = new File(".").getAbsolutePath();

		public static final String COMPILE_SCRIPTS_BAT = "Compile-Scripts.bat";
		public static final String COMPILE_SCRIPTS_SH = "compile-scripts.sh";
		public static final String COMPILE_FIND_JDK = "FindJDK.bat";

		public static final String SCRIPTS_NAME_SRC = "scripts";
		public static final String SCRIPTS_NAME_OUT = "Scripts";

		public static String getAccountsFile() {
			final String path;
			if (GlobalConfiguration.getCurrentOperatingSystem() == OperatingSystem.WINDOWS) {
				path = System.getenv("APPDATA") + File.separator
				+ GlobalConfiguration.NAME + "_Accounts.ini";
			} else {
				path = Paths.getUnixHome() + File.separator + "."
				+ GlobalConfiguration.NAME_LOWERCASE + "acct";
			}
			return path;
		}

		public static String getHomeDirectory() {
			final String env = System.getenv(GlobalConfiguration.NAME.toUpperCase() + "_HOME");
			if (env == null || env.isEmpty()) {
				return (GlobalConfiguration.getCurrentOperatingSystem() == OperatingSystem.WINDOWS ?
						FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath() :
							Paths.getUnixHome()) + File.separator + GlobalConfiguration.NAME;
			} else {
				return env;
			}
		}

		public static String getLogsDirectory() {
			return Paths.getHomeDirectory() + File.separator + "Logs";
		}

		public static String getMenuCache() {
			return Paths.getSettingsDirectory() + File.separator + "Menu.txt";
		}

		public static String getPathCache() {
			return Paths.getSettingsDirectory() + File.separator + "path.txt";
		}

		public static String getBootCache() {
			return Paths.getSettingsDirectory() + File.separator + "boot.txt";
		}

		public static String getUIDsFile() {
			return Paths.getSettingsDirectory() + File.separator + "uid.txt";
		}

		public static String getScreenshotsDirectory() {
			return Paths.getHomeDirectory() + File.separator + "Screenshots";
		}

		public static String getScriptsDirectory() {
			return Paths.getHomeDirectory() + File.separator + Paths.SCRIPTS_NAME_OUT;
		}

		public static String getScriptsSourcesDirectory() {
			return Paths.getScriptsDirectory() + File.separator + "Sources";
		}

		public static String getScriptsPrecompiledDirectory() {
			return Paths.getScriptsDirectory() + File.separator + "Precompiled";
		}

		public static String getScriptsNetworkDirectory() {
			return Paths.getScriptsDirectory() + File.separator + "Network";
		}

		public static String getCacheDirectory() {
			return Paths.getHomeDirectory() + File.separator + "Cache";
		}

		public static String getScriptCacheDirectory() {
			return getCacheDirectory() + File.separator + "Scripts";
		}

		public static String getScriptsExtractedCache() {
			return Paths.getCacheDirectory() + File.separator + "script.dat";
		}

		public static String getVersionCache() {
			return Paths.getCacheDirectory() + File.separator + "info.dat";
		}

		public static String getModScriptCache() {
			return Paths.getCacheDirectory() + File.separator + "ms.dat";
		}

		public static String getClientCache() {
			return Paths.getCacheDirectory() + File.separator + "client.dat";
		}

		public static String getEventsLog() {
			return Paths.getCacheDirectory() + File.separator + "events.log";
		}

		public static String getWebCache() {
			return Paths.getCacheDirectory() + File.separator + "web.dat";
		}

		public static String getBankCache() {
			return Paths.getCacheDirectory() + File.separator + "bank.dat";
		}

		public static String getSettingsDirectory() {
			return Paths.getHomeDirectory() + File.separator + "Settings";
		}

		public static String getMenuBarPrefs() {
			return Paths.getSettingsDirectory() + File.separator + "Menu.txt";
		}

		public static String getUnixHome() {
			final String home = System.getProperty("user.home");
			return home == null ? "~" : home;
		}
	}

	public static final String NAME = "RSBot";
	public static final String NAME_LOWERCASE = NAME.toLowerCase();
	private static final OperatingSystem CURRENT_OS;
	public static boolean RUNNING_FROM_JAR = false;
	public static final boolean SCRIPT_DRM = true;


	public static class Twitter {
		public static final boolean ENABLED = true;
		public static final String NAME = "rsbotorg";
		public static final String HASHTAG = "#" + NAME_LOWERCASE;
		public static final int MESSAGES = 3;
	}

	static {
		final URL resource = GlobalConfiguration.class.getClassLoader().getResource(Paths.Resources.VERSION);
		if (resource != null) {
			GlobalConfiguration.RUNNING_FROM_JAR = true;
		}
		final String os = System.getProperty("os.name");
		if (os.contains("Mac")) {
			CURRENT_OS = OperatingSystem.MAC;
		} else if (os.contains("Windows")) {
			CURRENT_OS = OperatingSystem.WINDOWS;
		} else if (os.contains("Linux")) {
			CURRENT_OS = OperatingSystem.LINUX;
		} else {
			CURRENT_OS = OperatingSystem.UNKNOWN;
		}
		final ArrayList<String> dirs = new ArrayList<String>();
		dirs.add(Paths.getHomeDirectory());
		dirs.add(Paths.getLogsDirectory());
		dirs.add(Paths.getCacheDirectory());
		dirs.add(Paths.getSettingsDirectory());
		if (GlobalConfiguration.RUNNING_FROM_JAR) {
			dirs.add(Paths.getScriptsDirectory());
			dirs.add(Paths.getScriptsSourcesDirectory());
			dirs.add(Paths.getScriptsPrecompiledDirectory());
		}
		for (final String name : dirs) {
			final File dir = new File(name);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
		final Properties logging = new Properties();
		final String logFormatter = LogFormatter.class.getCanonicalName();
		final String fileHandler = FileHandler.class.getCanonicalName();
		logging.setProperty("handlers", TextAreaLogHandler.class.getCanonicalName() + "," + fileHandler);
		logging.setProperty(".level", "INFO");
		logging.setProperty(SystemConsoleHandler.class.getCanonicalName() + ".formatter", logFormatter);
		logging.setProperty(fileHandler + ".formatter", logFormatter);
		logging.setProperty(TextAreaLogHandler.class.getCanonicalName() + ".formatter", logFormatter);
		logging.setProperty(fileHandler + ".pattern", Paths.getLogsDirectory() + File.separator + "%u.%g.log");
		logging.setProperty(fileHandler + ".count", "10");
		final ByteArrayOutputStream logout = new ByteArrayOutputStream();
		try {
			logging.store(logout, "");
			LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(logout.toByteArray()));
		} catch (final Exception ignored) {
		}
		if (GlobalConfiguration.RUNNING_FROM_JAR) {
			String path = resource.toString();
			try {
				path = URLDecoder.decode(path, "UTF-8");
			} catch (final UnsupportedEncodingException ignored) {
			}
			final String prefix = "jar:file:/";
			if (path.indexOf(prefix) == 0) {
				path = path.substring(prefix.length());
				path = path.substring(0, path.indexOf('!'));
				if (File.separatorChar != '/') {
					path = path.replace('/', File.separatorChar);
				}
				try {
					final File pathfile = new File(Paths.getPathCache());
					if (pathfile.exists()) {
						pathfile.delete();
					}
					pathfile.createNewFile();
					final Writer out = new BufferedWriter(new FileWriter(Paths.getPathCache()));
					out.write(path);
					out.close();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static URL getResourceURL(final String path) throws MalformedURLException {
		return RUNNING_FROM_JAR ? GlobalConfiguration.class.getResource("/" + path) : new File(path).toURI().toURL();
	}

	public static Image getImage(final String resource) {
		try {
			return Toolkit.getDefaultToolkit().getImage(getResourceURL(resource));
		} catch (final Exception e) {
		}
		return null;
	}

	public static OperatingSystem getCurrentOperatingSystem() {
		return GlobalConfiguration.CURRENT_OS;
	}

	static String httpUserAgent = null;

	public static String getHttpUserAgent() {
		if (httpUserAgent != null) {
			return httpUserAgent;
		}
		String os = "Windows NT 6.1";
		if (GlobalConfiguration.getCurrentOperatingSystem() == GlobalConfiguration.OperatingSystem.MAC) {
			os = "Macintosh; Intel Mac OS X 10_6_6";
		} else if (GlobalConfiguration.getCurrentOperatingSystem() != GlobalConfiguration.OperatingSystem.WINDOWS) {
			os = "X11; Linux x86_64";
		}
		final StringBuilder buf = new StringBuilder(125);
		buf.append("Mozilla/5.0 (").append(os).append(")");
		buf.append(" AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.60 Safari/534.24");
		httpUserAgent = buf.toString();
		return httpUserAgent;
	}

	public static HttpURLConnection getHttpConnection(final URL url) throws IOException {
		final HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		con.addRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		con.addRequestProperty("Accept-Encoding", "gzip,deflate");
		con.addRequestProperty("Accept-Language", "en-us,en;q=0.5");
		con.addRequestProperty("Host", url.getHost());
		con.addRequestProperty("User-Agent", getHttpUserAgent());
		con.setConnectTimeout(10000);
		return con;
	}

	public static int getVersion() {
		InputStreamReader is = null;
		BufferedReader reader = null;
		try {
			is = new InputStreamReader(RUNNING_FROM_JAR ?
					GlobalConfiguration.class.getClassLoader().getResourceAsStream(
							Paths.Resources.VERSION) : new FileInputStream(Paths.Resources.VERSION));
			reader = new BufferedReader(is);
			final String s = reader.readLine().trim();
			return Integer.parseInt(s);
		} catch (final Exception e) {
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (final IOException ioe) {
			}
		}
		return -1;
	}

	public static String getVersionFormatted() {
		return getVersionFormatted(getVersion());
	}

	public static String getVersionFormatted(final int version) {
		final float v = (float) version / 100;
		String s = Float.toString(v);
		final int z = s.indexOf('.');
		if (z == -1) {
			s += ".00";
		} else {
			final String exp = s.substring(z + 1);
			if (exp.length() == 1) {
				s += "0";
			}
		}
		return s;
	}
}
