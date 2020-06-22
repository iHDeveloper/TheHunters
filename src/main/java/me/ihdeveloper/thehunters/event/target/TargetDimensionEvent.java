/*
 * MIT License
 *
 * Copyright (c) 2020 iHDeveloper ( Hamza Aljuaid )
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package me.ihdeveloper.thehunters.event.target;

import me.ihdeveloper.thehunters.Dimension;
import me.ihdeveloper.thehunters.GamePlayer;
import org.bukkit.event.HandlerList;

public class TargetDimensionEvent extends TargetEvent {

    private static HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private Dimension dimension;

    public TargetDimensionEvent(GamePlayer player) {
        super(player);

        String world = player.getEntity().getWorld().getName();

        if (Dimension.THE_END.check(world)) {
            dimension = Dimension.THE_END;
        } else if (Dimension.NETHER.check(world)) {
            dimension = Dimension.NETHER;
        } else if (Dimension.WORLD.check(world)) {
            dimension = Dimension.WORLD;
        } else {
            dimension = Dimension.UNKNOWN;
        }
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public Dimension getDimension() {
        return dimension;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

}