package jcog.net.http;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Joris
 */
class HttpUtilTest
{
        HttpUtilTest()
        {
        }

        @Test
        void testBinarySizeUTF8()
        {
                testBinarySizeUTF8_string("$");
                testBinarySizeUTF8_string("¢");
                testBinarySizeUTF8_string("€");
                testBinarySizeUTF8_string("$Â¢â‚¬");
                testBinarySizeUTF8_string("\uD834\uDD1E");
        }
        
        private static void testBinarySizeUTF8_string(String test)
        {
                assertEquals(test.getBytes(StandardCharsets.UTF_8).length, HttpUtil.binarySizeUTF8(test));
        }
        
        @Test
        void testFindCRLF()
        {
                ByteBuffer buf = ByteBuffer.allocate(100);
                buf.put((byte) 't');
                buf.put((byte) 'e');
                buf.put((byte) 's');
                buf.put((byte) 't');
                buf.put((byte) '\r');
                buf.put((byte) '\n');
                buf.put((byte) 'b');
                buf.put((byte) 'l');
                buf.put((byte) 'a');
                buf.flip();
                assertEquals(4, HttpUtil.findCRLF(buf, 0));
                assertEquals(4, HttpUtil.findCRLF(buf, 1));
                assertEquals(4, HttpUtil.findCRLF(buf, 2));
                assertEquals(4, HttpUtil.findCRLF(buf, 3));
                assertEquals(4, HttpUtil.findCRLF(buf, 4));
                assertEquals(-1, HttpUtil.findCRLF(buf, 5));
                
                buf.clear();
                buf.put((byte) '\r');
                buf.put((byte) '\n');
                buf.flip();
                assertEquals(0, HttpUtil.findCRLF(buf, 0));
                assertEquals(-1, HttpUtil.findCRLF(buf, 1));
                
        }
        
        @Test
        void testFindCRLFIgnoreLWS()
        {
                ByteBuffer buf = ByteBuffer.allocate(100);
                buf.put((byte) 't'); 
                buf.put((byte) 'e'); 
                buf.put((byte) 's'); 
                buf.put((byte) 't'); 
                buf.put((byte) '\r');
                buf.put((byte) '\n');
                buf.put((byte) ' '); 
                buf.put((byte) 'a'); 
                buf.put((byte) 'b'); 
                buf.put((byte) 'c'); 
                buf.put((byte) '\r');
                buf.put((byte) '\n');
                buf.put((byte) '\r');
                buf.put((byte) '\n');
                buf.flip();
                
                assertEquals(10, HttpUtil.findCRLFIgnoreLWS(buf, 0));
                assertEquals(10, HttpUtil.findCRLFIgnoreLWS(buf, 3));
                assertEquals(10, HttpUtil.findCRLFIgnoreLWS(buf, 4));
                assertEquals(10, HttpUtil.findCRLFIgnoreLWS(buf, 5));
                assertEquals(10, HttpUtil.findCRLFIgnoreLWS(buf, 6));
                assertEquals(10, HttpUtil.findCRLFIgnoreLWS(buf, 9));
                assertEquals(10, HttpUtil.findCRLFIgnoreLWS(buf, 10));
                assertEquals(-1, HttpUtil.findCRLFIgnoreLWS(buf, 11));
                
                buf.clear();
                buf.put((byte) '\r');
                buf.put((byte) '\n');
                buf.put((byte) 'C');
                buf.flip();
                assertEquals(0, HttpUtil.findCRLFIgnoreLWS(buf, 0));
                assertEquals(-1, HttpUtil.findCRLFIgnoreLWS(buf, 1));
                
        }
        
        
        @Test
        void testReadLine()
        {
                StringBuilder dest = new StringBuilder();
                
                ByteBuffer buf = ByteBuffer.allocate(100);
                buf.put((byte) 't');
                buf.put((byte) 'e');
                buf.put((byte) 's');
                buf.put((byte) 't');
                buf.put((byte) '\r');
                buf.put((byte) '\n');
                buf.put((byte) ' ');
                buf.put((byte) 'b');
                buf.put((byte) 'l');
                buf.put((byte) 'a');
                buf.put((byte) '\r');
                buf.put((byte) '\n');
                buf.put((byte) '\r');
                buf.put((byte) '\n');
                buf.flip();
                
                dest.setLength(0);
                assertTrue(HttpUtil.readLine(dest, buf, false));
                assertEquals("test", dest.toString());
                
                dest.setLength(0);
                assertTrue(HttpUtil.readLine(dest, buf, false));
                assertEquals(" bla", dest.toString());
                
                dest.setLength(0);
                assertTrue(HttpUtil.readLine(dest, buf, false));
                assertEquals("", dest.toString());
                
                
                buf.position(0);
                
                dest.setLength(0);
                assertTrue(HttpUtil.readLine(dest, buf, true));
                assertEquals("test\r\n bla", dest.toString());
                
                dest.setLength(0);
                assertFalse(HttpUtil.readLine(dest, buf, true));
        }
}