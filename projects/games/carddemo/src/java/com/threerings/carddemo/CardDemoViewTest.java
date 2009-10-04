//
// $Id$

package com.threerings.carddemo;

import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JComponent;

import com.threerings.media.util.LinePath;
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

    protected JComponent createInterface (ToyBoxContext ctx)
    {
        return _view = new CardDemoView(ctx, new CardDemoController());
    }

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
