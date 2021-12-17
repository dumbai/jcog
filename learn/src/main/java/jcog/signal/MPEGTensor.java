//package jcog.signal;
//
//
////import org.jcodec.api.FrameGrab;
////import org.jcodec.api.awt.AWTSequenceEncoder;
////import org.jcodec.common.io.SeekableByteChannel;
////import org.jcodec.common.model.Rational;
////import org.jcodec.scale.AWTUtil;
//
//import java.awt.image.BufferedImage;
//import java.nio.channels.SeekableByteChannel;
//import java.util.function.Predicate;
//
//public enum MPEGTensor {
//	;
//
//	static void encode(Iterable<BufferedImage> imgs, int fps, SeekableByteChannel out) throws Exception {
////		SeekableByteChannel out = null;
////		try {
////			out = NIOUtils.writableFileChannel("/tmp/output.mp4");
//			AWTSequenceEncoder e = new AWTSequenceEncoder(out, Rational.R(fps,1));
//			for (BufferedImage i : imgs)
//				e.encodeImage(i);
//			e.finish(); // Finalize the encoding, i.e. clear the buffers, write the header, etc.
////		} finally {
////			NIOUtils.closeQuietly(out);
////		}
//	}
//
//	static void decode(SeekableByteChannel input, double startSec, int frameCount, Predicate<BufferedImage> each) throws Exception {
//
//		/* 		double startSec = 51.632;
//		int frameCount = 10;
// 		*/
//
////		File file = new File("video.mp4");
////		FileChannelWrapper chan = NIOUtils.readableChannel(file);
//
//		FrameGrab grab = FrameGrab.createFrameGrab(input);
//		grab.seekToSecondPrecise(startSec);
//
//		for (int i=0;i<frameCount;i++) {
//			if (!each.test(AWTUtil.toBufferedImage(grab.getNativeFrame())))
//				break;
//
//			//System.out.println(picture.getWidth() + "x" + picture.getHeight() + " " + picture.getColor());
//			//ImageIO.write(bufferedImage, "png", new File("frame"+i+".png"));
//		}
//	}
//}