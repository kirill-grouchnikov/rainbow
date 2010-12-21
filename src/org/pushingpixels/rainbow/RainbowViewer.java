/*
 * Copyright (c) 2005-2010 Rainbow Kirill Grouchnikov 
 * and Alexander Potochkin. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *    
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *    
 *  o Neither the name of Rainbow, Kirill Grouchnikov 
 *    and Alexander Potochkin nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.pushingpixels.rainbow;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;

import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.ext.MouseScrollableUI;
import org.jdesktop.jxlayer.plaf.ext.SpotLightUI;
import org.pushingpixels.flamingo.api.bcb.*;
import org.pushingpixels.flamingo.api.bcb.core.BreadcrumbFileSelector;
import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.lafwidget.animation.AnimationConfigurationManager;
import org.pushingpixels.lafwidget.animation.AnimationFacet;
import org.pushingpixels.rainbow.layout.*;
import org.pushingpixels.substance.api.DecorationAreaType;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.BusinessSkin;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * SVG viewer application.
 * 
 * @author Kirill Grouchnikov
 * @author Alexander Potochkin
 */
public class RainbowViewer extends JFrame implements ProgressListener {
	/**
	 * Breadcrumb bar for navigating the local hard disk.
	 */
	private JBreadcrumbBar bar;

	/**
	 * Panel with SVG icons.
	 */
	private RainbowFileViewPanel svgFileViewPanel;

	/**
	 * {@link JXLayer} painter.
	 */
	private SpotLightUI spotLightLayerUI;

	/**
	 * Find field (a-la SpotLight).
	 */
	private JTextField findField;

	/**
	 * Slider to resize the SVG icons.
	 */
	// private JSlider iconSizeSlider;
	/**
	 * Status label (in the status bar).
	 */
	private JLabel statusLabel;

	/**
	 * Progress bar for tracking the asycnhronous loading of SVG images (in the
	 * status bar).
	 */
	private JProgressBar statusProgressBar;

	/**
	 * Number of files that match the current find string.
	 */
	private int matchingFileCount;

	/**
	 * Current icon size.
	 */
	private int currIconSize;

	/**
	 * File system view.
	 */
	protected static FileSystemView fsv = FileSystemView.getFileSystemView();

	/**
	 * Creates a new SVG viewer application.
	 */
	@SuppressWarnings("unchecked")
	public RainbowViewer() {
		super("Rainbow (Darkstone) - SVG File Viewer");

		this.bar = this.getBar();
		this.bar.getModel().addPathListener(new BreadcrumbPathListener() {
			@Override
			public void breadcrumbPathEvent(final BreadcrumbPathEvent event) {
				SwingUtilities.invokeLater(new Runnable() {
					@SuppressWarnings("unchecked")
					public void run() {
						svgFileViewPanel.cancelMainWorker();
						final List<BreadcrumbItem<File>> newPath = bar
								.getModel().getItems();
						if (newPath.size() > 0) {
							SwingWorker<List<KeyValuePair>, Void> worker = new SwingWorker<List<KeyValuePair>, Void>() {
								@Override
								protected List<KeyValuePair> doInBackground()
										throws Exception {
									return bar.getCallback().getLeafs(newPath);
								}

								@Override
								protected void done() {
									try {
										svgFileViewPanel.setFolder(get());
										svgFileViewPanel
												.setIconDimension(currIconSize);
										updateSpotLightPainter();
									} catch (Exception exc) {
									}
								}
							};
							worker.execute();
						}
						updateSpotLightPainter();
						return;
					}
				});
			}
		});

		this.setLayout(new BorderLayout());

		FormLayout toolbarLayout = new FormLayout(
				"fill:pref:grow, 2dlu, fill:pref, 2dlu, fill:pref", "");
		DefaultFormBuilder toolbarBuilder = new DefaultFormBuilder(
				toolbarLayout);
		JToolBar toolbar = new JToolBar();
		SubstanceLookAndFeel.setDecorationType(toolbar,
				DecorationAreaType.HEADER);
		toolbar.setLayout(new BorderLayout());

		toolbarBuilder.append(bar);

		// toolbar.add(bar, BorderLayout.CENTER);

		this.findField = new JTextField();
		this.findField.setColumns(8);
		this.findField.getDocument().addDocumentListener(
				new DocumentListener() {
					public void changedUpdate(DocumentEvent e) {
						this.modified(e);
					}

					public void insertUpdate(DocumentEvent e) {
						this.modified(e);
					}

					public void removeUpdate(DocumentEvent e) {
						this.modified(e);
					}

					private void modified(DocumentEvent e) {
						updateSpotLightPainter();
					}
				});
		this.findField.setToolTipText("Type search string");
		toolbarBuilder.append(this.findField);

		Icon searchIcon = new ImageIcon(Thread.currentThread()
				.getContextClassLoader().getResource(
						"org/pushingpixels/rainbow/system-search.png"));
		JLabel searchIconLabel = new JLabel(searchIcon);
		toolbarBuilder.append(searchIconLabel);

		toolbar.add(toolbarBuilder.getPanel(), BorderLayout.CENTER);

		this.add(toolbar, BorderLayout.NORTH);
		toolbar.setFloatable(false);

		int initialSize = 64;
		this.svgFileViewPanel = new RainbowFileViewPanel<File>(this.bar,
				initialSize, this);
		LayoutManager panelLm = this.svgFileViewPanel.getLayout();
		if (panelLm instanceof TransitionLayout) {
			TransitionLayout transitionLm = (TransitionLayout) panelLm;
			transitionLm
					.addTransitionLayoutListener(new TransitionLayoutListener() {
						public void onTransitionLayoutEvent(
								TransitionLayoutEvent event) {
							if (event.getID() == TransitionLayoutEvent.TRANSITION_STARTED) {
								// System.out.println("TRANSITION_STARTED");
								spotLightLayerUI.reset();
							}
							if (event.getID() == TransitionLayoutEvent.TRANSITION_ENDED) {
								// System.out.println("TRANSITION_ENDED");
								updateSpotLightPainter();
							}
						}
					});
		}

		JXLayer<JComponent> layer = new JXLayer<JComponent>(svgFileViewPanel);
		spotLightLayerUI = new SpotLightUI(2);
		layer.setUI(spotLightLayerUI);

		JScrollPane jsp = new JScrollPane(layer,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(new JXLayer<JScrollPane>(jsp, new MouseScrollableUI()));

		FormLayout statusBarLayout = new FormLayout(
				"fill:pref:grow(1), 1dlu, "
						+ "fill:min(pref;100px):grow(1), 1dlu, fill:min(pref;200px):grow(1)");
		DefaultFormBuilder statusBarBuilder = new DefaultFormBuilder(
				statusBarLayout);
		this.statusLabel = new JLabel();
		statusBarBuilder.append(this.statusLabel);

		this.statusProgressBar = new JProgressBar();
		statusBarBuilder.append(this.statusProgressBar);
		this.statusProgressBar.setVisible(false);

		this.currIconSize = initialSize;
		statusBarBuilder.append(IconSizePanel.getPanel(this, initialSize));

		JPanel statusBarPanel = statusBarBuilder.getPanel();
		SubstanceLookAndFeel.setDecorationType(statusBarPanel,
				DecorationAreaType.GENERAL);
		this.add(statusBarPanel, BorderLayout.SOUTH);
	}

	protected JBreadcrumbBar<?> getBar() {
		return new BreadcrumbFileSelector();
	}

	/**
	 * Updates the {@link JXLayer} painter.
	 */
	private void updateSpotLightPainter() {
		this.matchingFileCount = 0;
		String text = this.findField.getText();
		if (text.length() == 0) {
			// disable JXLayer shaper and painter
			spotLightLayerUI.setShadowEnabled(false);
			spotLightLayerUI.reset();
			this.matchingFileCount = this.svgFileViewPanel.getButtonCount();
		} else {
			Map<String, JCommandButton> buttonMapping = svgFileViewPanel
					.getButtonMap();
			// System.out.println("Matching:");
			spotLightLayerUI.reset();
			for (Map.Entry<String, JCommandButton> buttonEntry : buttonMapping
					.entrySet()) {
				if (buttonEntry.getKey().contains(text)) {
					this.matchingFileCount++;
					Rectangle bounds = buttonEntry.getValue().getBounds();
					// System.out.println("\t" + buttonEntry.getKey() + ":"
					// + bounds);
					bounds.grow(-1, 0);
					RoundRectangle2D roundBounds = new RoundRectangle2D.Double(
							bounds.x, bounds.y, bounds.width, bounds.height,
							10, 10);
					spotLightLayerUI.addShape(roundBounds);
				} else {
					// if the search string is modified while the mouse pointer
					// is over an "active" button that becomes inactive as the
					// result of search string modification, we need to remove
					// the rollover status from the button to have the
					// consistent UI behaviour.
					buttonEntry.getValue().getActionModel().setRollover(false);
				}
			}
			// enable it back - will fire the repaint of the layer
			spotLightLayerUI.setShadowEnabled(true);
		}
		this.updateStatus();
	}

	/**
	 * Updates the status label.
	 */
	private void updateStatus() {
		int svgFileCount = this.svgFileViewPanel.getButtonCount();
		statusLabel.setText("Folder contains " + svgFileCount + " SVG files ("
				+ this.matchingFileCount + " matching filter)");

	}

	public void onProgress(final ProgressEvent evt) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int min = evt.getMinimum();
				int max = evt.getMaximum();
				int progress = evt.getProgress();
				if (progress == 0) {
					// started
					statusProgressBar.setMinimum(min);
					statusProgressBar.setMaximum(max);
					statusProgressBar.setValue(0);
					statusProgressBar.setVisible(true);
				} else {
					if (progress == max) {
						// ended
						statusProgressBar.setVisible(false);
						statusProgressBar.setValue(0);
					} else {
						statusProgressBar.setValue(progress);
					}
				}
			}
		});
	}

	/**
	 * Sets the icon size for all the icons currently showing.
	 * 
	 * @param iconSize
	 *            New icon size.
	 */
	public void setIconSize(int iconSize) {
		this.currIconSize = iconSize;
		svgFileViewPanel.setIconDimension(currIconSize);
	}

	/**
	 * Gets the current icon size.
	 * 
	 * @return Current icon size.
	 */
	public int getIconSize() {
		return this.currIconSize;
	}

	/**
	 * The main method to run the SVG viewer.
	 * 
	 * @param args
	 *            Ignored.
	 */
	public static void main(String... args) {
		AnimationConfigurationManager.getInstance().allowAnimations(
				AnimationFacet.GHOSTING_ICON_ROLLOVER);

		JFrame.setDefaultLookAndFeelDecorated(true);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				SubstanceLookAndFeel.setSkin(new BusinessSkin());
				RainbowViewer test = new RainbowViewer();
				test.setSize(700, 400);
				test.setLocationRelativeTo(null);
				test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				test.setVisible(true);
			}
		});
	}
}