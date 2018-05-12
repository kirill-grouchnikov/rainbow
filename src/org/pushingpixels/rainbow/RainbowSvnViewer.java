/*
 * Copyright (c) 2005-2018 Rainbow Kirill Grouchnikov 
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

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.pushingpixels.flamingo.api.bcb.JBreadcrumbBar;
import org.pushingpixels.spoonbill.svn.BreadcrumbMultiSvnSelector;
import org.pushingpixels.substance.api.SubstanceSlices.AnimationFacet;
import org.pushingpixels.substance.api.SubstanceCortex;
import org.pushingpixels.substance.api.skin.BusinessSkin;
import org.pushingpixels.substance.flamingo.SubstanceFlamingoPlugin;

/**
 * SVG viewer application.
 * 
 * @author Kirill Grouchnikov
 * @author Alexander Potochkin
 */
public class RainbowSvnViewer extends RainbowViewer {

    @Override
    protected JBreadcrumbBar<?> getBar() {
        BreadcrumbMultiSvnSelector selector = new BreadcrumbMultiSvnSelector(
                new BreadcrumbMultiSvnSelector.SvnRepositoryInfo("Oxygen",
                        "svn://anonsvn.kde.org/home/kde/trunk/KDE/kdeartwork/IconThemes/primary/",
                        "anonymous", "anonymous".toCharArray()),
                new BreadcrumbMultiSvnSelector.SvnRepositoryInfo("Kalzium",
                        "svn://anonsvn.kde.org/home/kde/trunk/KDE/kdeedu/kalzium/data/",
                        "anonymous", "anonymous".toCharArray()),
                new BreadcrumbMultiSvnSelector.SvnRepositoryInfo("Crystal",
                        "svn://anonsvn.kde.org/home/kde/", "anonymous", "anonymous".toCharArray()));
        selector.setThrowsExceptions(true);
        selector.addExceptionHandler((Throwable t) -> MessageListDialog
                .showMessageDialog(RainbowSvnViewer.this, "Error", t));
        return selector;
    }

    /**
     * The main method to run the SVG viewer.
     * 
     * @param args
     *            Ignored.
     */
    public static void main(String... args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        SubstanceCortex.GlobalScope.setTimelineDuration(1000);
        SubstanceCortex.GlobalScope.registerComponentPlugin(new SubstanceFlamingoPlugin());
        SubstanceCortex.GlobalScope.allowAnimations(AnimationFacet.GHOSTING_ICON_ROLLOVER);

        SwingUtilities.invokeLater(() -> {
            SubstanceCortex.GlobalScope.setSkin(new BusinessSkin());
            RainbowSvnViewer test = new RainbowSvnViewer();
            test.setSize(700, 400);
            test.setLocationRelativeTo(null);
            test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            test.setVisible(true);
        });
    }
}