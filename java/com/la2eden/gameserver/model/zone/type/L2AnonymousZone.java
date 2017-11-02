package com.la2eden.gameserver.model.zone.type;

import com.la2eden.gameserver.model.actor.L2Character;
import com.la2eden.gameserver.model.zone.L2ZoneType;
import com.la2eden.gameserver.model.zone.ZoneId;

/**
 * @author Enkel
 */
public class L2AnonymousZone extends L2ZoneType
{
    public L2AnonymousZone(int id)
    {
        super(id);
    }

    @Override
    protected void onEnter(L2Character character)
    {
        character.setInsideZone(ZoneId.ANONYMOUS, true);
    }

    @Override
    protected void onExit(L2Character character)
    {
        character.setInsideZone(ZoneId.ANONYMOUS, false);
    }
}