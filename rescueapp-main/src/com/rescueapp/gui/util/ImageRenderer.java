package com.rescueapp.gui.util;

import javax.imageio.ImageIO; // Use ImageIO for better format support
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage; // Use BufferedImage
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A custom TableCellRenderer that loads images from URLs in a background thread
 * (SwingWorker) to avoid freezing the UI. It caches images and handles errors.
 */
@SuppressWarnings("serial")
public class ImageRenderer extends DefaultTableCellRenderer {

    // Cache to store loaded images (using Icon for flexibility)
    private static final Map<String, Icon> imageCache = new HashMap<>();
    // Set to track which URLs are currently being loaded
    private static final Set<String> loadingUrls = new HashSet<>();
    // Placeholder Icon while loading
    private static final Icon LOADING_ICON = UIManager.getIcon("OptionPane.informationIcon"); // Or create a custom small loading icon
    // Placeholder Icon for failed loads
    private static final Icon ERROR_ICON = UIManager.getIcon("OptionPane.errorIcon"); // Or create a custom error icon

    public ImageRenderer() {
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER); // Center vertically too
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        // Start with default label settings
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setText(null); // No text by default
        label.setIcon(null); // Reset icon

        String imageUrl = (value instanceof String) ? (String) value : null;

        // Handle null or empty URL
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            label.setText("No URL");
            label.setIcon(ERROR_ICON);
            return label;
        }

        // --- Image Loading and Caching ---
        if (imageCache.containsKey(imageUrl)) {
            // 1. Image found in cache
            Icon cachedIcon = imageCache.get(imageUrl);
            if (cachedIcon != null) {
                label.setIcon(cachedIcon); // Use cached image
            } else {
                label.setText("Error"); // URL was previously tried and failed
                label.setIcon(ERROR_ICON);
            }
        } else if (loadingUrls.contains(imageUrl)) {
            // 2. Image is currently loading
            label.setText("Loading...");
            label.setIcon(LOADING_ICON);
        } else {
            // 3. Start loading the image
            label.setText("Loading...");
            label.setIcon(LOADING_ICON);
            loadingUrls.add(imageUrl); // Mark as loading

            // Use SwingWorker to load in the background
            ImageLoader worker = new ImageLoader(table, imageUrl, row, column);
            worker.execute();
        }

        return label;
    }

    /**
     * SwingWorker to load and scale an image in a background thread.
     */
    private static class ImageLoader extends SwingWorker<Icon, Void> {
        private final JTable table;
        private final String urlString;
        private final int targetHeight = 90; // Target height for the image icon
        private final int row;
        private final int col;


        public ImageLoader(JTable table, String url, int row, int col) {
            this.table = table;
            this.urlString = url;
            this.row = row;
            this.col = col;
        }

        @Override
        protected Icon doInBackground() throws Exception {
            System.out.println("ImageLoader: Starting load for URL: " + urlString); // Logging
            URL url = null;
            try {
                // Ensure URL includes protocol
                String correctedUrl = urlString;
                if (!urlString.toLowerCase().startsWith("http://") && !urlString.toLowerCase().startsWith("https://")) {
                    correctedUrl = "http://" + urlString; // Attempt adding http://
                    System.out.println("ImageLoader: Corrected URL to: " + correctedUrl);
                }
                url = new URL(correctedUrl);

                // --- Use HttpURLConnection for more control ---
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0"); // Some sites block default Java user agent
                conn.setConnectTimeout(5000); // 5 second timeout
                conn.setReadTimeout(10000); // 10 second timeout
                conn.connect();

                int responseCode = conn.getResponseCode();
                System.out.println("ImageLoader: Response code for " + correctedUrl + ": " + responseCode); // Logging

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedImage originalImage = ImageIO.read(conn.getInputStream());
                    conn.getInputStream().close(); // Close stream

                    if (originalImage != null) {
                        System.out.println("ImageLoader: Image read successfully for: " + correctedUrl); // Logging
                        // --- Scaling Logic ---
                        int originalWidth = originalImage.getWidth();
                        int originalHeight = originalImage.getHeight();
                        int newWidth = (originalWidth * targetHeight) / originalHeight; // Maintain aspect ratio

                        Image scaledImage = originalImage.getScaledInstance(newWidth, targetHeight, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImage);
                    } else {
                         System.err.println("ImageLoader: ImageIO.read returned null for: " + correctedUrl); // Logging
                        return null; // Image format not supported or invalid
                    }
                } else {
                     System.err.println("ImageLoader: HTTP error " + responseCode + " for URL: " + correctedUrl); // Logging
                    return null; // HTTP error
                }
            } catch (Exception e) {
                System.err.println("ImageLoader: Exception loading image [" + urlString + "]: " + e.getMessage()); // Logging with original URL
                // e.printStackTrace(); // Optional: Print full stack trace
                return null; // Return null on any error
            } finally{
                // Ensure connection is closed if opened
            }
        }

        @Override
        protected void done() {
            Icon loadedIcon = null;
            try {
                loadedIcon = get(); // Get the result from doInBackground
                if (loadedIcon != null) {
                    System.out.println("ImageLoader: Successfully loaded and scaled: " + urlString); // Logging
                    imageCache.put(urlString, loadedIcon); // Cache the successful result
                } else {
                     System.err.println("ImageLoader: Failed to get icon after loading (returned null): " + urlString); // Logging
                    imageCache.put(urlString, null); // Cache failure (null) so we don't retry constantly
                }
            } catch (InterruptedException e) {
                 System.err.println("ImageLoader: Loading interrupted for: " + urlString); // Logging
                 imageCache.put(urlString, null); // Cache failure
                 Thread.currentThread().interrupt(); // Re-set interrupt flag
            } catch (ExecutionException e) {
                 System.err.println("ImageLoader: Execution exception for: " + urlString); // Logging
                 e.printStackTrace(); // Show the underlying cause
                 imageCache.put(urlString, null); // Cache failure
            } finally {
                loadingUrls.remove(urlString); // No longer loading this URL

                // --- Crucial: Update the specific cell in the table on the EDT ---
                // Check if the row is still visible before repainting
                if (row < table.getRowCount()) {
                    // More targeted update:
                     SwingUtilities.invokeLater(() -> {
                          if (row < table.getRowCount() && col < table.getColumnCount()) {
                              System.out.println("ImageLoader: Triggering repaint for cell [" + row + "," + col + "]"); // Logging
                              table.getModel().setValueAt(urlString, row, col); // Force model update for the cell
                              // table.repaint(table.getCellRect(row, col, false)); // Can also try repaint
                          }
                     });
                } else {
                     System.out.println("ImageLoader: Row " + row + " is no longer valid, skipping repaint."); // Logging
                }
            }
        }
    }
}