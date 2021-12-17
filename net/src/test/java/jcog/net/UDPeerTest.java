package jcog.net;

import jcog.Util;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UDPeerTest {
    
    @ParameterizedTest
    @ValueSource(ints={0,1})
    void testDiscoverDoesntSelfConnect(int pingSelf) throws IOException {
        UDPeer x = new UDPeer();
        x.fps(8);
         if (pingSelf==1) {
            x.ping(x.addr);
        }
        for (int i = 0; i < 8; i++) {
            assertFalse(x.them.contains(x.me));
            assertFalse(x.connected());
            Util.sleepMS(100);
        }
        x.stop();
    }

    @Test
    void testDiscoverableByLANMulticast() throws IOException {

        UDPeer x = new UDPeer();
        x.fps(4);

        UDPeer y = new UDPeer();
        y.fps(4);


        Util.sleepMS(5000);

        assertTrue(x.them.contains(y.me));
        assertFalse(x.them.contains(x.me));
        assertTrue(y.them.contains(x.me));
        assertFalse(y.them.contains(y.me));

        x.stop();
        y.stop();
    }
}