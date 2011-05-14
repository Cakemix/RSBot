package org.rsbot.gui;

import org.rsbot.Configuration;
import org.rsbot.Configuration.OperatingSystem;
import org.rsbot.bot.Bot;
import org.rsbot.log.TextAreaLogHandler;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.internal.ScriptHandler;
import org.rsbot.script.internal.event.ScriptListener;
import org.rsbot.script.methods.Environment;
import org.rsbot.script.provider.ScriptDeliveryNetwork;
import org.rsbot.script.provider.ScriptDownloader;
import org.rsbot.script.util.WindowUtil;
import org.rsbot.service.Monitoring;
import org.rsbot.service.Monitoring.Type;
import org.rsbot.service.TwitterUpdates;
import org.rsbot.service.WebQueue;
import org.rsbot.util.UpdateChecker;
import org.rsbot.util.io.ScreenshotUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Jacmob
 */
public class BotGUI extends JFrame implements ActionListener, ScriptListener {
	public static final int PANEL_WIDTH = 765, PANEL_HEIGHT = 503, LOG_HEIGHT = 120;
	private static final long serialVersionUID = -5411033752001988794L;
	private static final Logger log = Logger.getLogger(BotGUI.class.getName());
	private BotPanel panel;
	private JScrollPane scrollableBotPanel;
	private BotToolBar toolBar;
	private BotMenuBar menuBar;
	private JScrollPane textScroll;
	private BotHome home;
	private final List<Bot> bots = new ArrayList<Bot>();
	private boolean showAds = true;
	private boolean disableConfirmations = false;
	private final int botsIndex = 2;
	private TrayIcon tray = null;
	private java.util.Timer shutdown = null;

	public BotGUI() {
		init();
		pack();
		setTitle(null);
		setLocationRelativeTo(getOwner());
		setMinimumSize(getSize());
		setResizable(true);
		menuBar.loadPrefs();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JPopupMenu.setDefaultLightWeightPopupEnabled(false);
				ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
				if (showAds) {
					new SplashAd(BotGUI.this).display();
				}
				UpdateChecker.notify(BotGUI.this);
				if (Configuration.Twitter.ENABLED) {
					TwitterUpdates.loadTweets(Configuration.Twitter.MESSAGES);
				}
				new Thread() {
					@Override
					public void run() {
						ScriptDeliveryNetwork.getInstance().start();
					}
				}.start();
				Monitoring.start();
				addBot();
				updateScriptControls();
				System.gc();
			}
		});
	}

	@Override
	public void setTitle(final String title) {
		String t = Configuration.NAME + " v" + Configuration.getVersionFormatted();
		final int v = Configuration.getVersion(), l = UpdateChecker.getLatestVersion();
		if (v > l) {
			t += " beta";
		}
		if (title != null) {
			t = title + " - " + t;
		}
		super.setTitle(t);
	}

	public void actionPerformed(final ActionEvent evt) {
		final String action = evt.getActionCommand();
		final String menu, option;
		final int z = action.indexOf('.');
		if (z == -1) {
			menu = action;
			option = "";
		} else {
			menu = action.substring(0, z);
			option = action.substring(z + 1);
		}
		if (menu.equals("Close")) {
			if (confirmRemoveBot()) {
				final int idx = Integer.parseInt(option);
				removeBot(bots.get(idx - botsIndex));
			}
		} else if (menu.equals(Messages.FILE)) {
			if (option.equals(Messages.NEWBOT)) {
				addBot();
			} else if (option.equals(Messages.CLOSEBOT)) {
				if (confirmRemoveBot()) {
					removeBot(getCurrentBot());
				}
			} else if (option.equals(Messages.ADDSCRIPT)) {
				final String pretext = "";
				final String key = (String) JOptionPane.showInputDialog(this, "Enter the script URL (e.g. pastebin link):",
						option, JOptionPane.QUESTION_MESSAGE, null, null, pretext);
				if (!(key == null || key.trim().isEmpty())) {
					ScriptDownloader.save(key);
				}
			} else if (option.equals(Messages.RUNSCRIPT)) {
				final Bot current = getCurrentBot();
				if (current != null) {
					showScriptSelector(current);
				}
			} else if (option.equals(Messages.SERVICEKEY)) {
				serviceKeyQuery(option);
			} else if (option.equals(Messages.STOPSCRIPT)) {
				final Bot current = getCurrentBot();
				if (current != null) {
					showStopScript(current);
				}
			} else if (option.equals(Messages.PAUSESCRIPT)) {
				final Bot current = getCurrentBot();
				if (current != null) {
					pauseScript(current);
				}
			} else if (option.equals(Messages.SAVESCREENSHOT)) {
				final Bot current = getCurrentBot();
				if (current != null) {
					ScreenshotUtil.saveScreenshot(current, current.getMethodContext().game.isLoggedIn());
				}
			} else if (option.equals(Messages.HIDEBOT)) {
				setTray();
			} else if (option.equals(Messages.EXIT)) {
				cleanExit(false);
			}
		} else if (menu.equals(Messages.EDIT)) {
			if (option.equals(Messages.ACCOUNTS)) {
				AccountManager.getInstance().showGUI();
			} else if (option.equals(Messages.DISABLEADS)) {
				showAds = !((JCheckBoxMenuItem) evt.getSource()).isSelected();
			} else if (option.equals(Messages.DISABLEMONITORING)) {
				Monitoring.setEnabled(!((JCheckBoxMenuItem) evt.getSource()).isSelected());
				if (!Monitoring.isEnabled()) {
					log.info("Monitoring data is used to improve development, please enable it to help us");
				}
			} else if (option.equals(Messages.DISABLECONFIRMATIONS)) {
				disableConfirmations = ((JCheckBoxMenuItem) evt.getSource()).isSelected();
			} else if (option.equals(Messages.AUTOSHUTDOWN)) {
				final boolean enabled = ((JCheckBoxMenuItem) evt.getSource()).isSelected();
				if (!enabled) {
					if (shutdown != null) {
						shutdown.cancel();
						shutdown.purge();
					}
					shutdown = null;
				} else {
					final long interval = 10 * 60 * 1000; // 10mins
					shutdown = new java.util.Timer(true);
					shutdown.schedule(new TimerTask() {
						@Override
						public void run() {
							for (final Bot bot : bots) {
								if (bot.getScriptHandler().getRunningScripts().size() != 0) {
									return;
								}
							}
							log.info("Shutting down in 1 minute...");
							try {
								Thread.sleep(1 * 60 * 1000);
							} catch (InterruptedException ignored) {
							}
							if (!menuBar.isTicked(option)) {
								log.info("Shutdown cancelled");
							} else if (Configuration.getCurrentOperatingSystem() == OperatingSystem.WINDOWS) {
								try {
									Runtime.getRuntime().exec("shutdown.exe", new String[]{"-s"});
									cleanExit(true);
								} catch (IOException ignored) {
									log.severe("Could not shutdown system");
								}
							}
						}
					}, interval, interval);
				}
			} else {
				final Bot current = getCurrentBot();
				if (current != null) {
					if (option.equals(Messages.FORCEINPUT)) {
						final boolean selected = ((JCheckBoxMenuItem) evt.getSource()).isSelected();
						current.overrideInput = selected;
						updateScriptControls();
					} else if (option.equals(Messages.LESSCPU)) {
						lessCpu(true);
					} else if (option.equals(Messages.DISABLEANTIRANDOMS)) {
						current.disableRandoms = ((JCheckBoxMenuItem) evt.getSource()).isSelected();
					} else if (option.equals(Messages.DISABLEAUTOLOGIN)) {
						current.disableAutoLogin = ((JCheckBoxMenuItem) evt.getSource()).isSelected();
					}
				}
			}
		} else if (menu.equals(Messages.VIEW)) {
			final Bot current = getCurrentBot();
			final boolean selected = ((JCheckBoxMenuItem) evt.getSource()).isSelected();
			if (option.equals(Messages.HIDETOOLBAR)) {
				toggleViewState(toolBar, selected);
			} else if (option.equals(Messages.HIDELOGPANE)) {
				toggleViewState(textScroll, selected);
			} else if (current != null) {
				if (option.equals(Messages.ALLDEBUGGING)) {
					for (final String key : BotMenuBar.DEBUG_MAP.keySet()) {
						final Class<?> el = BotMenuBar.DEBUG_MAP.get(key);
						if (menuBar.getCheckBox(key).isVisible()) {
							final boolean wasSelected = menuBar.getCheckBox(key).isSelected();
							menuBar.getCheckBox(key).setSelected(selected);
							if (selected) {
								if (!wasSelected) {
									current.addListener(el);
								}
							} else {
								if (wasSelected) {
									current.removeListener(el);
								}
							}
						}
					}
				} else {
					final Class<?> el = BotMenuBar.DEBUG_MAP.get(option);
					menuBar.getCheckBox(option).setSelected(selected);
					if (selected) {
						current.addListener(el);
					} else {
						menuBar.getCheckBox(Messages.ALLDEBUGGING).setSelected(false);
						current.removeListener(el);
					}
				}
			}
		} else if (menu.equals(Messages.HELP)) {
			if (option.equals(Messages.SITE)) {
				openURL(Configuration.Paths.URLs.SITE);
			} else if (option.equals(Messages.PROJECT)) {
				openURL(Configuration.Paths.URLs.PROJECT);
			} else if (option.equals("About")) {
				JOptionPane.showMessageDialog(this, new String[]{"An open source bot developed by the community.", "Visit " + Configuration.Paths.URLs.SITE + "/ for more information."}, "About", JOptionPane.INFORMATION_MESSAGE);
			} else if (option.equals(Messages.DEVUPDATE)) {
				UpdateChecker.internalDeveloperUpdate(BotGUI.this);
			}
		} else if (menu.equals("Tab")) {
			final Bot curr = getCurrentBot();
			menuBar.setBot(curr);
			panel.setBot(curr);
			panel.repaint();
			toolBar.setHome(curr == null);
			setTitle(curr == null ? null : curr.getAccountName());
			updateScriptControls();
		}
	}

	private void updateScriptControls() {
		boolean idle = true, paused = false;
		final Bot bot = getCurrentBot();

		if (bot != null) {
			final Map<Integer, Script> scriptMap = bot.getScriptHandler().getRunningScripts();
			if (scriptMap.size() > 0) {
				idle = false;
				paused = scriptMap.values().iterator().next().isPaused();
			} else {
				idle = true;
			}
		}

		menuBar.getMenuItem(Messages.RUNSCRIPT).setVisible(idle);
		menuBar.getMenuItem(Messages.STOPSCRIPT).setVisible(!idle);
		menuBar.getMenuItem(Messages.PAUSESCRIPT).setEnabled(!idle);
		menuBar.setPauseScript(paused);

		if (idle) {
			toolBar.setOverrideInput(false);
			menuBar.setOverrideInput(false);
			toolBar.setInputState(Environment.INPUT_KEYBOARD | Environment.INPUT_MOUSE);
			toolBar.setScriptButton(BotToolBar.RUN_SCRIPT);
		} else {
			toolBar.setOverrideInput(bot.overrideInput);
			toolBar.setOverrideInput(bot.overrideInput);
			toolBar.setInputState(bot.inputFlags);
			toolBar.setScriptButton(paused ? BotToolBar.RESUME_SCRIPT : BotToolBar.PAUSE_SCRIPT);
		}

		toolBar.updateInputButton();
	}

	private void serviceKeyQuery(final String option) {
		final String currentKey = ScriptDeliveryNetwork.getInstance().getKey();
		final String key = (String) JOptionPane.showInputDialog(this, null, option, JOptionPane.QUESTION_MESSAGE, null, null, currentKey);
		if (key == null || key.length() == 0) {
			log.info("Services have been disabled");
		} else if (key.length() != 40) {
			log.warning("Invalid service key");
		} else {
			log.info("Services have been linked to {0}");
		}
	}

	private void lessCpu(boolean on) {
		disableRendering(on || menuBar.isTicked(Messages.LESSCPU));
	}

	public void disableRendering(final boolean mode) {
		for (final Bot bot : bots) {
			bot.disableRendering = mode;
		}
	}

	public BotPanel getPanel() {
		return panel;
	}

	public Bot getBot(final Object o) {
		final ClassLoader cl = o.getClass().getClassLoader();
		for (final Bot bot : bots) {
			if (cl == bot.getLoader().getClient().getClass().getClassLoader()) {
				panel.offset();
				return bot;
			}
		}
		return null;
	}

	public void addBot() {
		final int max = 6;
		if (bots.size() >= max && Configuration.RUNNING_FROM_JAR) {
			log.warning("Cannot run more than " + Integer.toString(max) + " bots");
			return;
		}
		final Bot bot = new Bot();
		bots.add(bot);
		toolBar.addTab();
		bot.getScriptHandler().addScriptListener(this);
		new Thread(new Runnable() {
			public void run() {
				bot.start();
				home.setBots(bots);
			}
		}).start();
	}

	public void removeBot(final Bot bot) {
		final int idx = bots.indexOf(bot);
		if (idx >= 0) {
			toolBar.removeTab(idx + botsIndex);
		}
		bots.remove(idx);
		bot.getScriptHandler().stopAllScripts();
		bot.getScriptHandler().removeScriptListener(this);
		bot.getBackgroundScriptHandler().stopAllScripts();
		home.setBots(bots);
		new Thread(new Runnable() {
			public void run() {
				bot.stop();
				System.gc();
			}
		}).start();
	}

	void pauseScript(final Bot bot) {
		final ScriptHandler sh = bot.getScriptHandler();
		final Map<Integer, Script> running = sh.getRunningScripts();
		if (running.size() > 0) {
			final int id = running.keySet().iterator().next();
			sh.pauseScript(id);
		}
	}

	private Bot getCurrentBot() {
		final int idx = toolBar.getCurrentTab() - botsIndex;
		if (idx >= 0) {
			return bots.get(idx);
		}
		return null;
	}

	private void showScriptSelector(final Bot bot) {
		if (AccountManager.getAccountNames() == null || AccountManager.getAccountNames().length == 0) {
			JOptionPane.showMessageDialog(this, "No accounts found! Please create one before using the bot.");
			AccountManager.getInstance().showGUI();
		} else if (bot.getMethodContext() == null) {
			log.warning("The client is still loading");
		} else {
			new ScriptSelector(this, bot).showGUI();
		}
	}

	private void showStopScript(final Bot bot) {
		final ScriptHandler sh = bot.getScriptHandler();
		final Map<Integer, Script> running = sh.getRunningScripts();
		if (running.size() > 0) {
			final int id = running.keySet().iterator().next();
			final Script s = running.get(id);
			final ScriptManifest prop = s.getClass().getAnnotation(ScriptManifest.class);
			final int result = JOptionPane.showConfirmDialog(this, "Would you like to stop the script " + prop.name() + "?", "Script", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				sh.stopScript(id);
				updateScriptControls();
			}
		}
	}

	private void toggleViewState(final Component component, final boolean visible) {
		final Dimension size = getSize();
		size.height += component.getSize().height * (visible ? -1 : 1);
		component.setVisible(!visible);
		setMinimumSize(size);
		if ((getExtendedState() & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH) {
			pack();
		}
	}

	private void init() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				if (cleanExit(false)) {
					dispose();
				}
			}
		});
		addWindowStateListener(new WindowStateListener() {
			public void windowStateChanged(final WindowEvent arg0) {
				switch (arg0.getID()) {
					case WindowEvent.WINDOW_ICONIFIED:
						lessCpu(true);
						break;
					case WindowEvent.WINDOW_DEICONIFIED:
						lessCpu(false);
						break;
				}
			}
		});
		setIconImage(Configuration.getImage(Configuration.Paths.Resources.ICON));
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception ignored) {
		}
		WindowUtil.setFrame(this);
		home = new BotHome();
		panel = new BotPanel(home);
		menuBar = new BotMenuBar(this);
		toolBar = new BotToolBar(this, menuBar);
		panel.setFocusTraversalKeys(0, new HashSet<AWTKeyStroke>());
		menuBar.setBot(null);
		setJMenuBar(menuBar);
		textScroll = new JScrollPane(TextAreaLogHandler.TEXT_AREA, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		textScroll.setBorder(null);
		textScroll.setPreferredSize(new Dimension(PANEL_WIDTH, LOG_HEIGHT));
		textScroll.setVisible(true);
		scrollableBotPanel = new JScrollPane(panel);
		add(toolBar, BorderLayout.NORTH);
		add(scrollableBotPanel, BorderLayout.CENTER);
		add(textScroll, BorderLayout.SOUTH);
	}

	public void scriptStarted(final ScriptHandler handler, final Script script) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				final Bot bot = handler.getBot();
				if (bot == getCurrentBot()) {
					bot.inputFlags = Environment.INPUT_KEYBOARD;
					bot.overrideInput = false;
					updateScriptControls();
					final String acct = bot.getAccountName();
					toolBar.setTabLabel(bots.indexOf(bot) + botsIndex, acct == null ? "RuneScape" : acct);
					setTitle(acct);
				}
			}
		});
	}

	public void scriptStopped(final ScriptHandler handler, final Script script) {
		final Bot bot = handler.getBot();
		if (bot == getCurrentBot()) {
			bot.inputFlags = Environment.INPUT_KEYBOARD | Environment.INPUT_MOUSE;
			bot.overrideInput = false;
			updateScriptControls();
			toolBar.setTabLabel(bots.indexOf(bot) + botsIndex, "RuneScape");
			setTitle(null);
		}
	}

	public void scriptResumed(final ScriptHandler handler, final Script script) {
		if (handler.getBot() == getCurrentBot()) {
			updateScriptControls();
		}
	}

	public void scriptPaused(final ScriptHandler handler, final Script script) {
		if (handler.getBot() == getCurrentBot()) {
			updateScriptControls();
		}
	}

	public void inputChanged(final Bot bot, final int mask) {
		bot.inputFlags = mask;
		toolBar.setInputState(mask);
		updateScriptControls();
	}

	public static void openURL(final String url) {
		final Configuration.OperatingSystem os = Configuration.getCurrentOperatingSystem();
		try {
			if (os == Configuration.OperatingSystem.MAC) {
				final Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
				final Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
				openURL.invoke(null, url);
			} else if (os == Configuration.OperatingSystem.WINDOWS) {
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			} else {
				final String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape", "google-chrome", "chromium-browser"};
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++) {
					if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0) {
						browser = browsers[count];
					}
				}
				if (browser == null) {
					throw new Exception("Could not find web browser");
				} else {
					Runtime.getRuntime().exec(new String[]{browser, url});
				}
			}
		} catch (final Exception e) {
			log.warning("Unable to open " + url);
		}
	}

	private boolean confirmRemoveBot() {
		if (!disableConfirmations) {
			final int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to close this bot?", Messages.CLOSEBOT, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			return result == JOptionPane.OK_OPTION;
		} else {
			return true;
		}
	}

	public boolean cleanExit(final boolean silent) {
		if (silent) {
			disableConfirmations = true;
		}
		if (!disableConfirmations) {
			disableConfirmations = true;
			for (final Bot bot : bots) {
				if (bot.getAccountName() != null) {
					disableConfirmations = true;
					break;
				}
			}
		}
		boolean doExit = true;
		if (!disableConfirmations) {
			final String message = "Are you sure you want to exit?";
			final int result = JOptionPane.showConfirmDialog(this, message, Messages.EXIT, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (result != JOptionPane.OK_OPTION) {
				doExit = false;
			}
		}
		WebQueue.Destroy();
		setVisible(false);
		Monitoring.pushState(Type.ENVIRONMENT, "ADS", "SHOW", Boolean.toString(showAds));
		if (doExit) {
			menuBar.savePrefs();
			Monitoring.stop();
			System.exit(0);
		} else {
			setVisible(true);
		}
		return doExit;
	}

	public void setTray() {
		if (tray == null) {
			final Image image = Configuration.getImage(Configuration.Paths.Resources.ICON);
			tray = new TrayIcon(image, Configuration.NAME, null);
			tray.setImageAutoSize(true);
			tray.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent arg0) {
				}

				public void mouseEntered(MouseEvent arg0) {
				}

				public void mouseExited(MouseEvent arg0) {
				}

				public void mouseReleased(MouseEvent arg0) {
				}

				public void mousePressed(MouseEvent arg0) {
					SystemTray.getSystemTray().remove(tray);
					setVisible(true);
					lessCpu(false);
				}
			});
		}
		try {
			SystemTray.getSystemTray().add(tray);
		} catch (Exception ignored) {
			log.warning("Unable to hide window");
		}
		setVisible(false);
		lessCpu(true);
	}
}
