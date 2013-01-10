package org.esa.beam.visat.toolviews.imageinfo;

import org.esa.beam.framework.datamodel.ColorPaletteDef;
import org.esa.beam.framework.datamodel.Scaling;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: dshea
 * User: aabduraz
 * Date: 4/10/12
 * Time: 9:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class ColorPaletteChooser extends JComboBox {
    private final int COLORBAR_HEIGHT = 15;
    private final int COLORBAR_WIDTH = 204;
    private final String DEFAULT_GRAY_COLOR_PALETTE_FILE_NAME = "defaultGrayColor.cpd";

    private File colorPaletteDir;
    private Dimension colorBarDimension;
    private ComboBoxModel colorModel;
    private ArrayList<ImageIcon> icons;

    private ColorHashMap colorBarMap;
    private double colorBarMin;
    private double colorBarMax;
    private boolean isDiscrete;
    private int currentColorBarIndex;

    public ColorPaletteChooser(File colorPaletteDir) {
        super();
        colorBarDimension = new Dimension(COLORBAR_WIDTH, COLORBAR_HEIGHT);
        this.colorPaletteDir = colorPaletteDir;
        colorBarMap = new ColorHashMap();
        updateDisplay();
        setColorPaletteDir(colorPaletteDir);
        setEditable(false);
        setRenderer(new ComboBoxRenderer());
    }

    public ColorPaletteChooser(File colorPaletteDir, ColorPaletteDef defaultColorPaletteDef) {
        super();
        colorBarDimension = new Dimension(COLORBAR_WIDTH, COLORBAR_HEIGHT);
        this.colorPaletteDir = colorPaletteDir;
        colorBarMap = new ColorHashMap();
        createDefaultGrayColorPaletteFile(defaultColorPaletteDef);
        updateDisplay();
        setColorPaletteDir(colorPaletteDir);
        setEditable(false);
        setRenderer(new ComboBoxRenderer());
    }

    public void distributeSlidersEvenly(ColorPaletteDef colorPaletteDef) {
        final double pos1 = colorPaletteDef.getMinDisplaySample();
        final double pos2 = colorPaletteDef.getMaxDisplaySample();
        final double delta = pos2 - pos1;
        final double evenSpace = delta / (colorPaletteDef.getNumPoints() - 1);
        for (int i = 1; i < colorPaletteDef.getNumPoints() - 1; i++) {
            final double value = pos1 + evenSpace * i;
            colorPaletteDef.getPointAt(i).setSample(value);
        }
    }

    private void drawPalette(Graphics2D g2, ColorPaletteDef colorPaletteDef, Rectangle paletteRect) {
        int paletteX1 = paletteRect.x;
        int paletteX2 = paletteRect.x + paletteRect.width;

        g2.setStroke(new BasicStroke(1.0f));
        Color[] colorPalette = colorPaletteDef.createColorPalette(Scaling.IDENTITY);
        int divisor = paletteX2 - paletteX1;
        for (int x = paletteX1; x < paletteX2; x++) {

            int palIndex = ((colorPalette.length * (x - paletteX1)) / divisor);

            if (palIndex < 0) {
                palIndex = 0;
            }
            if (palIndex >= colorPalette.length) {
                palIndex = colorPalette.length - 1;
            }
            g2.setColor(colorPalette[palIndex]);
            g2.drawLine(x, paletteRect.y, x, paletteRect.y + paletteRect.height);
        }
    }

    private ImageIcon createGrayColorBarIcon(ColorPaletteDef colorPaletteDef, Dimension dimension) {
        BufferedImage bufferedImage = new BufferedImage(dimension.width, dimension.height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        drawPalette(g2, colorPaletteDef, new Rectangle(dimension));
        ImageIcon icon = new ImageIcon(bufferedImage);
        colorBarMap.put(colorPaletteDef.createColorPalette(Scaling.IDENTITY), icon);
        icon.setDescription(DEFAULT_GRAY_COLOR_PALETTE_FILE_NAME);
        return icon;
    }

    private String colorBarMinMaxDescription() {
        return "  cpd file min: " + colorBarMin + "   cpd file max: " + colorBarMax;
    }

    private void createDefaultGrayColorPaletteFile(ColorPaletteDef defaultGrayColorPaletteDef) {

        File grayColorFile = new File(colorPaletteDir, DEFAULT_GRAY_COLOR_PALETTE_FILE_NAME);
        try {
            defaultGrayColorPaletteDef.setAutoDistribute(true);
            ColorPaletteDef.storeColorPaletteDef(defaultGrayColorPaletteDef, grayColorFile);
        } catch (IOException ioe) {
            System.err.println("Default Gray Color File is not created!");
        }
    }

    private void drawPalette(Graphics2D g2, File paletteFile, Rectangle paletteRect) throws IOException {
        ColorPaletteDef colorPaletteDef = ColorPaletteDef.loadColorPaletteDefForColorBar(paletteFile);
        updateColorBarMinMax(colorPaletteDef);
        distributeSlidersEvenly(colorPaletteDef);
        drawPalette(g2, colorPaletteDef, paletteRect);
    }

    private ImageIcon createColorBarIcon(ColorPaletteDef colorPaletteDef, Dimension dimension) throws IOException {
        BufferedImage bufferedImage = new BufferedImage(dimension.width, dimension.height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        distributeSlidersEvenly(colorPaletteDef);
        drawPalette(g2, colorPaletteDef, new Rectangle(dimension));
        return new ImageIcon(bufferedImage);
    }

    private ImageIcon createColorBarIcon(File cpdFile, Dimension dimension) throws IOException {
        BufferedImage bufferedImage = new BufferedImage(dimension.width, dimension.height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        drawPalette(g2, cpdFile, new Rectangle(dimension));
        ImageIcon icon = new ImageIcon(bufferedImage);
        icon.setDescription(cpdFile.getName());
        ColorPaletteDef colorPaletteDef = ColorPaletteDef.loadColorPaletteDefForColorBar(cpdFile);
        colorBarMap.put(colorPaletteDef.createColorPalette(Scaling.IDENTITY), icon);
        updateColorBarMinMax(colorPaletteDef);
        return icon;
    }

    private void updateColorBarMinMax(ColorPaletteDef colorPaletteDef) {
        colorBarMin = colorPaletteDef.getMinDisplaySample();
        colorBarMax = colorPaletteDef.getMaxDisplaySample();
        isDiscrete = colorPaletteDef.isDiscrete();
    }

    ComboBoxModel createColorBarModel() {
        ArrayList<ImageIcon> icons = new ArrayList<ImageIcon>();
        File[] files = colorPaletteDir.listFiles();
        ImageIcon defaultIcon = null;
        for (File file : files) {
            try {
                ImageIcon icon = createColorBarIcon(file, colorBarDimension);
                icons.add(icon);
                if (file.getName().indexOf(DEFAULT_GRAY_COLOR_PALETTE_FILE_NAME) != -1) {
                    defaultIcon = icon;
                }
            } catch (IOException e) {
            }
        }
        Collections.sort(icons, new Comparator<ImageIcon>() {
            @Override
            public int compare(ImageIcon o1, ImageIcon o2) {
                return o1.getDescription().compareTo(o2.getDescription());
            }
        });
        this.icons = icons;
        this.icons.remove(defaultIcon);
        this.icons.add(0, defaultIcon);
        DefaultComboBoxModel colorBarModel = new DefaultComboBoxModel(icons.toArray(new ImageIcon[icons.size()]));
        colorBarModel.setSelectedItem(defaultIcon);
        return colorBarModel;
    }

    private void updateDisplay() {
        colorModel = createColorBarModel();
        setModel(colorModel);
    }

    public void updateColorPalette(ColorPaletteDef colorPaletteDef) {
        ImageIcon currentColorBarIcon;

        if (colorPaletteDef.getNumColors() == 3) {
            createDefaultGrayColorPaletteFile(colorPaletteDef);
            currentColorBarIcon = createGrayColorBarIcon(colorPaletteDef, colorBarDimension);
            icons.add(currentColorBarIcon);
            colorModel.setSelectedItem(currentColorBarIcon);
            setModel(colorModel);
            //currentColorBarIndex = 0;
        } else {
            currentColorBarIcon = colorBarMap.getImageIcon(colorPaletteDef.createColorPalette(Scaling.IDENTITY));

            if (currentColorBarIcon != null) {
                colorModel.setSelectedItem(currentColorBarIcon);
                setModel(colorModel);
                //currentColorBarIndex = ((DefaultComboBoxModel)colorModel).getIndexOf((ImageIcon)colorModel.getSelectedItem());
            }
        }
    }

    private void updateColorBarModel() {
        System.out.println("isDiscrete Changed!");
    }

    protected void updateColorBar(ColorPaletteDef colorPaletteDef) {

        System.out.println("isDiscrete Changed!");
        //ImageIcon currentColorBarIcon = (ImageIcon) colorModel.getSelectedItem();
        ImageIcon newIcon = new ImageIcon();
        try {
          newIcon = createColorBarIcon(colorPaletteDef,colorBarDimension);
        } catch(IOException ioe) {

        }
        System.out.println(((ImageIcon)(colorModel.getSelectedItem())).getDescription() + "     " +  (colorModel.getSelectedItem()).getClass().getName());
        //ImageIcon currentColorBarIcon = colorBarMap.getImageIcon(colorPaletteDef.createColorPalette(Scaling.IDENTITY));
        System.out.println("current image location: " + currentColorBarIndex);
        //System.out.println("current image desceiption: " + currentColorBarIcon.getDescription());
        int currentColorBarLocation = ((DefaultComboBoxModel)colorModel).getIndexOf((ImageIcon)colorModel.getSelectedItem());
        System.out.println("current image location: " + currentColorBarLocation);
        currentColorBarLocation = ((DefaultComboBoxModel)colorModel).getIndexOf(colorModel.getSelectedItem());
        //currentColorBarIndex = ((DefaultComboBoxModel) colorModel).getIndexOf(currentColorBarIcon);
        System.out.println("current image location: " + currentColorBarLocation);

        ((DefaultComboBoxModel) colorModel).removeElementAt(currentColorBarLocation);
        ((DefaultComboBoxModel) colorModel).insertElementAt(newIcon, currentColorBarLocation);
         colorModel.setSelectedItem(colorModel.getElementAt(currentColorBarLocation));

//        BufferedImage bufferedImage = new BufferedImage(colorBarDimension.width, colorBarDimension.height,
//                BufferedImage.TYPE_INT_RGB);
//        Graphics2D g2 = bufferedImage.createGraphics();
//        drawPalette(g2, colorPaletteDef, new Rectangle(colorBarDimension));
//        currentColorBarIcon.setImage(bufferedImage);
//        //setModel(colorModel);
        validate();
        repaint();
//        ArrayList<ImageIcon> icons = new ArrayList<ImageIcon>(colorBarMap.values());
//        Collections.sort(icons, new Comparator<ImageIcon>() {
//            @Override
//            public int compare(ImageIcon o1, ImageIcon o2) {
//                return o1.getDescription().compareTo(o2.getDescription());
//            }
//        });
//        this.icons = icons;
//        this.icons.remove(oldIcon);
//        this.icons.add(newIcon);
//        DefaultComboBoxModel colorBarModel = new DefaultComboBoxModel(icons.toArray(new ImageIcon[icons.size()]));
//        colorBarModel.setSelectedItem(newIcon);
//        return colorBarModel;

    }

    public File getColorPaletteDir() {
        return colorPaletteDir;
    }

    public void setColorPaletteDir(File colorPaletteDir) {
        this.colorPaletteDir = colorPaletteDir;
        updateDisplay();
    }

    public Dimension getColorBarDimension() {
        return colorBarDimension;
    }

    public void setColorBarDimension(Dimension colorBarDimension) {
        this.colorBarDimension = colorBarDimension;
        updateDisplay();
    }

    public ColorPaletteDef getSelectedColorPaletteDef() {
        Object obj = getSelectedItem();
        File paletteFile = new File(colorPaletteDir, obj.toString());
        ColorPaletteDef colorPaletteDef = null;
        try {
            colorPaletteDef = ColorPaletteDef.loadColorPaletteDefForColorBar(paletteFile);
            updateColorBarMinMax(colorPaletteDef);
        } catch (IOException e) {
        }
        return colorPaletteDef;
    }

    public double getColorBarMin() {
        return colorBarMin;
    }

    public double getColorBarMax() {
        return colorBarMax;
    }

    private class ComboBoxRenderer extends JLabel implements ListCellRenderer {

        public ComboBoxRenderer() {
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
        }

        /*
        * This method finds the image and text corresponding
        * to the selected value and returns the label, set up
        * to display the text and image.
        */
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            ImageIcon icon = (ImageIcon) value;
            BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth() + 2, icon.getIconHeight() + 2,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = bufferedImage.createGraphics();
            if (isSelected) {
                g2.setColor(Color.darkGray);
            } else {
                g2.setColor(list.getParent().getBackground());
            }
            g2.fillRect(0, 0, icon.getIconWidth() + 2, icon.getIconHeight() + 2);
            g2.drawImage(icon.getImage(), 1, 1, null);

            setIcon(new ImageIcon(bufferedImage));
            setToolTipText(icon.getDescription());

            return this;
        }

    }

    private class ColorHashMap extends HashMap<Color[], ImageIcon> {
        ColorHashMap() {
            super();
        }

        protected ImageIcon getImageIcon(Color[] currentColors) {
            Set<Color[]> keys = keySet();
            Iterator itr = keys.iterator();
            Color[] colors;
            while (itr.hasNext()) {
                colors = (Color[]) itr.next();
                if (Arrays.equals(colors, currentColors)) {
                    return get(colors);
                }
            }
            return null;
        }
    }


}
