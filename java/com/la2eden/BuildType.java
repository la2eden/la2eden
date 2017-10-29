package com.la2eden;

/**
 * This file is part of the La2Eden project.
 *
 * @author All Unser Miranda
 */
public enum BuildType
{
    FREE(0),
    VIP(1);

    private int _type;

    BuildType(int type) {
        _type = type;
    }
}
