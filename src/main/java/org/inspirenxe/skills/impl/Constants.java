package org.inspirenxe.skills.impl;

import org.apache.commons.lang3.text.WordUtils;

import java.text.DecimalFormat;

public interface Constants {

    interface Plugin {

        String ID = "skills";
        String NAME = WordUtils.capitalize(ID);
    }

    interface Format {
        DecimalFormat PRETTY_EXP = new DecimalFormat("###,###.##");
    }
}
