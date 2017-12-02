/*
 * Copyright (c) 2005-2010 Rainbow Kirill Grouchnikov. All Rights Reserved.
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
 *  o Neither the name of Flamingo Kirill Grouchnikov nor the names of 
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * A dialog for displaying a list of messages (in a scroll panel).
 * 
 * @author Kirill Grouchnikov
 */
public final class MessageListDialog extends JDialog {
    private boolean toExitOnDispose;

    /**
     * Simple constructor. Made private to enforce use of
     * {@link MessageListDialog#showMessageDialog(java.awt.Frame, String,
     * java.util.LinkedList<java.lang.String>)} and
     * {@link MessageListDialog#showMessageDialog(java.awt.Frame, String, Throwable)}
     * .
     * 
     * @param owner
     *            The owner frame (<code>this</code> dialog is modal).
     * @param mainMessage
     *            The main message (displayed in the top portion of the dialog).
     * @param messages
     *            The list of messages to display in a scroll panel.
     */
    private MessageListDialog(Frame owner, String mainMessage, LinkedList<String> messages) {
        super(owner, "Message list");

        JLabel messageLabel = new JLabel(mainMessage);
        JList messageList = new JList(messages.toArray());
        messageList.setForeground(Color.red);
        messageList.setFont(UIManager.getFont("Panel.font").deriveFont(Font.BOLD));
        JScrollPane mesScrollPane = new JScrollPane(messageList);
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener((ActionEvent e) -> {
            SwingUtilities.invokeLater(() -> {
                dispose();
                if (toExitOnDispose)
                    System.exit(0);
            });
        });

        this.setLayout(new BorderLayout());
        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        upperPanel.add(messageLabel);
        bottomPanel.add(closeButton);

        this.add(upperPanel, BorderLayout.NORTH);
        this.add(mesScrollPane, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        int width = 400;
        int height = 250;
        Dimension thisDim = new Dimension(width, height);
        this.setSize(thisDim);
        this.setPreferredSize(thisDim);
        this.setResizable(true);

        if (owner != null) {
            Dimension ownerDim = owner.getSize();
            Point ownerLoc = owner.getLocation();

            int xc = ownerLoc.x + ownerDim.width / 2;
            int yc = ownerLoc.y + ownerDim.height / 2;
            this.setLocation(xc - width / 2, yc - height / 2);
        } else {
            // retrieve the physical screen dimension
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            // center the configuration screen in the physical screen
            this.setLocation((d.width - width) / 2, (d.height - height) / 2);
        }

        this.getRootPane().setDefaultButton(closeButton);
        this.toExitOnDispose = false;
    }

    /**
     * Shows a message dialog.
     * 
     * @param owner
     *            The owner frame (<code>this</code> dialog is modal).
     * @param mainMessage
     *            The main message (displayed in the top portion of the dialog).
     * @param messages
     *            The list of messages to display in a scroll panel.
     */
    public static MessageListDialog showMessageDialog(Frame owner, String mainMessage,
            LinkedList<String> messages) {
        MessageListDialog mld = new MessageListDialog(owner, mainMessage, messages);
        mld.setModal(owner != null);
        mld.setVisible(true);
        return mld;
    }

    /**
     * Shows a message dialog for exception.
     * 
     * @param owner
     *            The owner frame (<code>this</code> dialog is modal).
     * @param message
     *            The main message (displayed in the top portion of the dialog).
     * @param throwable
     *            An exception. The stack trace of the exception will be shown
     *            in a scroll panel.
     */
    public static MessageListDialog showMessageDialog(Frame owner, String message,
            Throwable throwable) {
        LinkedList<String> errors = new LinkedList<String>();
        while (true) {
            String mainMessage = throwable.getClass().getName() + " : " + throwable.getMessage();
            errors.addLast(mainMessage);
            StackTraceElement[] stack = throwable.getStackTrace();
            for (StackTraceElement ste : stack) {
                String className = ste.getClassName();
                String methodName = ste.getMethodName();
                String lineNumber = "" + ste.getLineNumber();
                String steMessage = "      in " + className + "." + methodName + "(); [line "
                        + lineNumber + "]";
                errors.addLast(steMessage);
            }
            throwable = throwable.getCause();
            if (throwable == null)
                break;
            errors.addLast("Caused by");
        }
        return showMessageDialog(owner, message, errors);
    }

    public void setToExitOnDispose(boolean toExitOnDispose) {
        this.toExitOnDispose = toExitOnDispose;
    }
}