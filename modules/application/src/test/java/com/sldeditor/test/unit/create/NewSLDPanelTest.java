/*
 * SLD Editor - The Open Source Java SLD Editor
 *
 * Copyright (C) 2018, SCISYS UK Limited
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

package com.sldeditor.test.unit.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sldeditor.common.SLDDataInterface;
import com.sldeditor.create.NewSLDPanel;
import java.util.List;
import javax.swing.ComboBoxModel;
import org.junit.jupiter.api.Test;

/**
 * The Class NewSLDPanelTest.
 *
 * <p>Set to @Disabled currently because it takes focus away from the user when the unit test is run
 *
 * @author Robert Ward (SCISYS)
 */
public class NewSLDPanelTest {

    /** The Class TestNewSLDPanel. */
    class TestNewSLDPanel extends NewSLDPanel {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** Instantiates a new test new sld panel. */
        public TestNewSLDPanel() {
            super();
        }

        /**
         * Sets the data.
         *
         * @param index the new data
         */
        public void setData(int index) {
            comboBoxNewSLD.setSelectedIndex(index);
        }

        /**
         * Gets the option data.
         *
         * @return the data
         */
        public ComboBoxModel<String> getData() {
            return comboBoxNewSLD.getModel();
        }

        /** Ok button. */
        public void okButton() {
            okButtonPressed();
        }

        /** Cancel button. */
        public void cancelButton() {
            cancelButtonPressed();
        }

        /*
         * (non-Javadoc)
         *
         * @see com.sldeditor.create.NewSLDPanel#getSelectedSLD()
         */
        @Override
        protected List<SLDDataInterface> getSelectedSLD() {
            return super.getSelectedSLD();
        }
    }

    /** Test. */
    @Test
    public void test() {
        TestNewSLDPanel panel = new TestNewSLDPanel();

        panel.setData(0);

        // Press cancel
        panel.cancelButton();
        assertNull(panel.getSelectedSLD());

        for (int index = 0; index < panel.getData().getSize(); index++) {
            panel.setData(index);
            panel.okButton();
            List<SLDDataInterface> sldData = panel.getSelectedSLD();
            assertEquals(1, sldData.size());
            assertEquals(panel.getData().getElementAt(index), sldData.get(0).getLayerName());
        }
    }
}
