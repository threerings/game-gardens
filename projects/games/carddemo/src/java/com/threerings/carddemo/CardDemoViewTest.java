//
// ToyBox library - framework for matchmaking networked games
// Copyright (C) 2005-2011 Three Rings Design, Inc., All Rights Reserved
// http://github.com/threerings/game-gardens
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.carddemo;

import javax.swing.JComponent;

import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.data.Hand;

import com.threerings.toybox.util.GameViewTest;
import com.threerings.toybox.util.ToyBoxContext;

/**
 * Tests the card demo view.
 */
public class CardDemoViewTest extends GameViewTest
{
    public static void main (String[] args)
    {
        CardDemoViewTest test = new CardDemoViewTest();
        test.display();
    }

    @Override
    protected JComponent createInterface (ToyBoxContext ctx)
    {
        return _view = new CardDemoView(ctx, new CardDemoController());
    }

    @Override
    protected void initInterface ()
    {
        // position the hand at the bottom center of the view
        _view.setHandLocation(_view.getWidth()/2, _view.getHeight()-CardDemoView.CARD.height);
        // spread the cards out by 1/4th the card width
        _view.setHandSpacing(CardDemoView.CARD.width/4);

        Hand hand = new Hand();
        hand.addAll(new Card[] { new Card(3, Card.SPADES), new Card(6, Card.HEARTS) });
        _view.setHand(hand, 500);
    }

    protected CardDemoView _view;
}
