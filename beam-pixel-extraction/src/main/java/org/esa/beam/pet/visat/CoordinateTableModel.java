/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.pet.visat;

import org.esa.beam.framework.datamodel.PinDescriptor;
import org.esa.beam.framework.datamodel.Placemark;
import org.esa.beam.visat.toolviews.placemark.AbstractPlacemarkTableModel;

class CoordinateTableModel extends AbstractPlacemarkTableModel {

    CoordinateTableModel() {
        super(PinDescriptor.INSTANCE, null, null, null);
    }

    @Override
    public String[] getStandardColumnNames() {
        return new String[]{"Name", "Latitude", "Longitude"};
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
            case 2:
                return Float.class;
            default:
                throw new IllegalArgumentException(String.format("Invalid columnIndex = %d", columnIndex));
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        final Placemark placemark = getPlacemarkAt(rowIndex);
        switch (columnIndex) {
            case 0:
                placemark.setName((String) value);
                break;
            case 1:
                setGeoPosLat(value, placemark);
                break;
            case 2:
                setGeoPosLon(value, placemark);
                break;
            default:
                throw new IllegalArgumentException(String.format("Invalid columnIndex = %d", columnIndex));
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    protected Object getStandardColumnValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return getPlacemarkAt(rowIndex).getName();
            case 1:
                return getPlacemarkAt(rowIndex).getGeoPos().getLat();
            case 2:
                return getPlacemarkAt(rowIndex).getGeoPos().getLon();
            default:
                throw new IllegalArgumentException(String.format("Invalid columnIndex = %d", columnIndex));
        }
    }
}