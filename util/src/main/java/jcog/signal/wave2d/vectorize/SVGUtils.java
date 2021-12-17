package jcog.signal.wave2d.vectorize;

import java.util.HashMap;
import java.util.List;

public enum SVGUtils {
	;


	public static float roundtodec(float val, int places){
		float scale = (float) Math.pow(10, places);
		return Math.round(val * scale) / scale;
	}


	
	public static void svgpathstring (StringBuilder sb, String desc, List<Double[]> segments, String colorstr, HashMap<String,Float> options){
		float scale = options.get("scale"), lcpr = options.get("lcpr"), qcpr = options.get("qcpr");
		int roundcoords = (int) Math.floor(options.get("roundcoords"));
		
		sb.append("<path ").append(desc).append(colorstr).append("d=\"" ).append("M ").append(segments.get(0)[1]*scale).append(' ').append(segments.get(0)[2]*scale).append(' ');

		if( roundcoords == -1 ){
			for (Double[] segment : segments) {
				if (segment[0] == 1.0) {
					sb.append("L ").append(segment[3] * scale).append(' ').append(segment[4] * scale).append(' ');
				} else {
					sb.append("Q ").append(segment[3] * scale).append(' ').append(segment[4] * scale).append(' ').append(segment[5] * scale).append(' ').append(segment[6] * scale).append(' ');
				}
			}
		}else{
			for (Double[] segment : segments) {
				if (segment[0] == 1.0) {
					sb.append("L ").append(roundtodec((float) (segment[3] * scale), roundcoords)).append(' ')
							.append(roundtodec((float) (segment[4] * scale), roundcoords)).append(' ');
				} else {
					sb.append("Q ").append(roundtodec((float) (segment[3] * scale), roundcoords)).append(' ')
							.append(roundtodec((float) (segment[4] * scale), roundcoords)).append(' ')
							.append(roundtodec((float) (segment[5] * scale), roundcoords)).append(' ')
							.append(roundtodec((float) (segment[6] * scale), roundcoords)).append(' ');
				}
			}
		}

		sb.append("Z\" />");


		for (Double[] segment : segments) {
			if ((lcpr > 0) && (segment[0] == 1.0)) {
				sb.append("<circle cx=\"").append(segment[3] * scale).append("\" cy=\"").append(segment[4] * scale).append("\" r=\"").append(lcpr).append("\" fill=\"white\" stroke-width=\"").append(lcpr * 0.2).append("\" stroke=\"black\" />");
			}
			if ((qcpr > 0) && (segment[0] == 2.0)) {
				sb.append("<circle cx=\"").append(segment[3] * scale).append("\" cy=\"").append(segment[4] * scale).append("\" r=\"").append(qcpr).append("\" fill=\"cyan\" stroke-width=\"").append(qcpr * 0.2).append("\" stroke=\"black\" />");
				sb.append("<circle cx=\"").append(segment[5] * scale).append("\" cy=\"").append(segment[6] * scale).append("\" r=\"").append(qcpr).append("\" fill=\"white\" stroke-width=\"").append(qcpr * 0.2).append("\" stroke=\"black\" />");
				sb.append("<line x1=\"").append(segment[1] * scale).append("\" y1=\"").append(segment[2] * scale).append("\" x2=\"").append(segment[3] * scale).append("\" y2=\"").append(segment[4] * scale).append("\" stroke-width=\"").append(qcpr * 0.2).append("\" stroke=\"cyan\" />");
				sb.append("<line x1=\"").append(segment[3] * scale).append("\" y1=\"").append(segment[4] * scale).append("\" x2=\"").append(segment[5] * scale).append("\" y2=\"").append(segment[6] * scale).append("\" stroke-width=\"").append(qcpr * 0.2).append("\" stroke=\"cyan\" />");
			}
		}

	}


	

	
	


	static String tosvgcolorstr (byte[] c){
		return "fill=\"rgb("+(c[0]+128)+ ',' +(c[1]+128)+ ',' +(c[2]+128)+")\" stroke=\"rgb("+(c[0]+128)+ ',' +(c[1]+128)+ ',' +(c[2]+128)+")\" stroke-width=\"1\" opacity=\""+((c[3]+128)/255.0)+"\" ";
	}


}
