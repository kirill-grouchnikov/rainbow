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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.pushingpixels.ibis.SvgStreamTranscoder;

import jsyntaxpane.syntaxkits.JavaSyntaxKit;
import jsyntaxpane.syntaxkits.XmlSyntaxKit;

/**
 * Utilities class.
 * 
 * @author Kirill Grouchnikov
 * @author Alexander Potochkin
 */
public class RainbowUtils {
    /**
     * Last chosen folder for saving Java2D code.
     */
    private static File lastChosenFolder;

    /**
     * Processes the click on SVG icon button.
     * 
     * @param svgBytes
     *            SVG bytes.
     * @param svgName
     *            SVG file name (not necessarily on the hard disk).
     */
    public static void processSvgButtonClick(byte[] svgBytes, String svgName) {
        try {
            final JFrame fileFrame = new JFrame(svgName);
            fileFrame.setSize(600, 500);
            fileFrame.setLocationRelativeTo(null);
            fileFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            fileFrame.setLayout(new BorderLayout());

            final JTabbedPane jtp = new JTabbedPane();
            jtp.setTabPlacement(SwingConstants.LEFT);

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL fontURL = classLoader.getResource("resource/VeraMono.ttf");
            InputStream streamFont = fontURL.openStream();
            Font font = Font.createFont(Font.TRUETYPE_FONT, streamFont);
            font = font.deriveFont(1.0f + UIManager.getFont("TextArea.font").getSize2D());

            String svgContents = new String(svgBytes);
            JEditorPane xmlEditorPane = new JEditorPane();
            JScrollPane xmlScroller = new JScrollPane(xmlEditorPane);
            xmlEditorPane.setEditorKit(new XmlSyntaxKit());
            xmlEditorPane.setFont(font);
            xmlEditorPane.setEditable(false);
            xmlEditorPane.setBackground(Color.WHITE);
            xmlEditorPane.setText(svgContents);
            xmlEditorPane.moveCaretPosition(0);
            jtp.add("SVG contents", xmlScroller);

            fileFrame.add(jtp, BorderLayout.CENTER);
            fileFrame.setVisible(true);

            final String javaClassFilename = getSvgClassName(svgName);

            final ByteArrayOutputStream javaBaos = new ByteArrayOutputStream();
            final PrintWriter pw = new PrintWriter(javaBaos);

            SvgStreamTranscoder transcoder = new SvgStreamTranscoder(
                    new ByteArrayInputStream(svgBytes), javaClassFilename);

            transcoder.setPrintWriter(pw);
            transcoder.transcode(RainbowUtils.class.getResourceAsStream(
                    "/org/pushingpixels/ibis/SvgTranscoderTemplateResizable.templ"));

            String javaContents = new String(javaBaos.toByteArray());
            JEditorPane javaEditorPane = new JEditorPane();
            JScrollPane javaScroller = new JScrollPane(javaEditorPane);
            javaEditorPane.setEditorKit(new JavaSyntaxKit());
            javaEditorPane.setFont(font);
            javaEditorPane.setEditable(false);
            javaEditorPane.setBackground(Color.WHITE);
            javaEditorPane.setText(javaContents);
            javaEditorPane.moveCaretPosition(0);

            // JTextArea javaTextArea = new JTextArea();
            // javaTextArea.append(new String(javaBaos.toByteArray()));
            // javaTextArea.moveCaretPosition(0);
            // javaTextArea.setFont(font);

            JPanel javaPanel = new JPanel(new BorderLayout());
            javaPanel.add(javaScroller, BorderLayout.CENTER);
            JButton saveAs = new JButton("Save as...");
            saveAs.addActionListener((ActionEvent e) -> {
                SwingUtilities.invokeLater(() -> {
                    JFileChooser fileChooser = new JFileChooser(lastChosenFolder);
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    fileChooser.setSelectedFile(new File(javaClassFilename + ".java"));
                    fileChooser.addChoosableFileFilter(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            if (pathname.isDirectory())
                                return true;
                            return pathname.getAbsolutePath().endsWith(".java");
                        }

                        @Override
                        public String getDescription() {
                            return "Java source files";
                        }
                    });
                    int returnVal = fileChooser.showDialog(fileFrame, "Save");
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        lastChosenFolder = file.getParentFile();

                        try (FileWriter javaFileWriter = new FileWriter(file)) {
                            String javaContent = new String(javaBaos.toByteArray());
                            javaFileWriter.write(javaContent);
                            System.out.println("Saved Java2D code to " + file.getAbsolutePath());
                        } catch (IOException ioe) {
                        }
                    }
                });
            });
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(saveAs);
            javaPanel.add(buttonPanel, BorderLayout.SOUTH);
            jtp.add("Java2D code", javaPanel);

            jtp.add("Image Editor",
                    new ImageEditor(svgBytes, lastChosenFolder, svgName, fileFrame));
        } catch (Throwable t) {
            t.printStackTrace();
            MessageListDialog mld = MessageListDialog.showMessageDialog(null, "Exception caught",
                    t);
            mld.setToExitOnDispose(false);
        }
    }

    /**
     * Returns the Java class name for the SVG file name.
     * 
     * @param svgName
     *            SVG file name.
     * @return Java class name for the SVG file name.
     */
    public static String getSvgClassName(String svgName) {
        int lastDotIndex = svgName.lastIndexOf('.');
        String svgClassName = svgName.substring(0, lastDotIndex);
        svgClassName = svgClassName.replace('-', '_');
        svgClassName = svgClassName.replace(' ', '_');
        return svgClassName;
    }
}
