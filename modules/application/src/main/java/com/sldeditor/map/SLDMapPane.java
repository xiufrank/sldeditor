/*
 * SLD Editor - The Open Source Java SLD Editor
 *
 * Copyright (C) 2016, SCISYS UK Limited
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sldeditor.map;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.HashMap;
import java.util.Map;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.label.LabelCacheImpl;
import org.geotools.renderer.lite.LabelCache;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.swing.AbstractMapPane;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.RenderingExecutor;

/**
 * Class based on JMapPane but updated to allow anti alias flags to be set.
 *
 * @author Robert Ward (SCISYS)
 */
/**
 * A lightweight map pane which uses a single renderer and backing image. Used by {@linkplain
 * JMapFrame} for the GeoTools tutorial applications.
 *
 * @author Michael Bedward
 * @author Ian Turton
 * @version $Id$
 * @since 2.6
 * @source $URL$
 */
public class SLDMapPane extends AbstractMapPane {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The renderer. */
    private transient GTRenderer renderer;

    /** The base image. */
    private transient BufferedImage baseImage;

    /** The base image graphics. */
    private transient Graphics2D baseImageGraphics;

    /** Creates a new map pane. */
    public SLDMapPane() {
        this(null);
    }

    /**
     * Creates a new map pane.
     *
     * @param content the map content containing the layers to display (may be {@code null})
     */
    public SLDMapPane(MapContent content) {
        this(content, null, null);
    }

    /**
     * Creates a new map pane. Any or all arguments may be {@code null}
     *
     * @param content the map content containing the layers to display
     * @param executor the rendering executor to manage drawing
     * @param renderer the renderer to use for drawing layers
     */
    public SLDMapPane(MapContent content, RenderingExecutor executor, GTRenderer renderer) {
        super(content, executor);
        doSetRenderer(renderer);
    }

    /** {@inheritDoc}. */
    @Override
    public void setMapContent(MapContent content) {
        super.setMapContent(content);
        if (content != null && renderer != null) {
            // If the new map content had layers to draw, and this pane is visible,
            // then the map content will already have been set with the renderer
            //
            if (renderer.getMapContent() != content) { // just check reference equality
                renderer.setMapContent(mapContent);
            }
        }
    }

    /**
     * Gets the renderer, creating a default one if required.
     *
     * @return the renderer
     */
    public GTRenderer getRenderer() {
        if (renderer == null) {
            doSetRenderer(new StreamingRenderer());
        }
        return renderer;
    }

    /**
     * Sets the renderer to be used by this map pane.
     *
     * @param renderer the renderer to use
     */
    public void setRenderer(GTRenderer renderer) {
        doSetRenderer(renderer);
    }

    /**
     * Do set renderer.
     *
     * @param newRenderer the new renderer
     */
    private void doSetRenderer(GTRenderer newRenderer) {
        if (newRenderer != null) {
            Map<Object, Object> hints = newRenderer.getRendererHints();
            if (hints == null) {
                hints = new HashMap<>();
            }

            if (newRenderer instanceof StreamingRenderer) {
                if (hints.containsKey(StreamingRenderer.LABEL_CACHE_KEY)) {
                    labelCache = (LabelCache) hints.get(StreamingRenderer.LABEL_CACHE_KEY);
                } else {
                    labelCache = new LabelCacheImpl();
                    hints.put(StreamingRenderer.LABEL_CACHE_KEY, labelCache);
                }
            }

            newRenderer.setRendererHints(hints);

            if (mapContent != null) {
                newRenderer.setMapContent(mapContent);
            }
        }

        renderer = newRenderer;
    }

    /**
     * Retrieve the map pane's current base image.
     *
     * <p>The map pane caches the most recent rendering of map layers as an image to avoid
     * time-consuming rendering requests whenever possible. The base image will be re-drawn whenever
     * there is a change to map layer data, style or visibility; and it will be replaced by a new
     * image when the pane is resized.
     *
     * <p>This method returns a <b>live</b> reference to the current base image. Use with caution.
     *
     * @return a live reference to the current base image
     */
    public RenderedImage getBaseImage() {
        return this.baseImage;
    }

    /**
     * Paint component.
     *
     * @param g the g
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (drawingLock.tryLock()) {
            try {
                if (baseImage != null) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.drawImage(baseImage, imageOrigin.x, imageOrigin.y, null);
                }
            } finally {
                drawingLock.unlock();
            }
        }
    }

    /**
     * Draw layers.
     *
     * @param createNewImage the create new image
     */
    @Override
    protected void drawLayers(boolean createNewImage) {
        drawingLock.lock();
        try {
            if (mapContent != null
                    && !mapContent.getViewport().isEmpty()
                    && acceptRepaintRequests.get()) {

                Rectangle r = getVisibleRect();
                if (baseImage == null || createNewImage) {
                    baseImage =
                            GraphicsEnvironment.getLocalGraphicsEnvironment()
                                    .getDefaultScreenDevice()
                                    .getDefaultConfiguration()
                                    .createCompatibleImage(
                                            r.width, r.height, Transparency.TRANSLUCENT);

                    if (baseImageGraphics != null) {
                        baseImageGraphics.dispose();
                    }

                    baseImageGraphics = baseImage.createGraphics();
                    baseImageGraphics.setRenderingHints(
                            new RenderingHints(
                                    RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON));
                    clearLabelCache.set(true);

                } else {
                    baseImageGraphics.setBackground(getBackground());
                    baseImageGraphics.clearRect(0, 0, r.width, r.height);
                }

                if (mapContent != null && !mapContent.layers().isEmpty()) {
                    getRenderingExecutor()
                            .submit(mapContent, getRenderer(), baseImageGraphics, this);
                }
            }
        } finally {
            drawingLock.unlock();
        }
    }

    /** Reset renderer. */
    public void resetRenderer() {
        if (renderingExecutor != null) {
            renderingExecutor.shutdown();
        }
        renderingExecutor = null;
        mapContent.dispose();
        doSetRenderer(null);
        setMapContent(null);
    }
}
