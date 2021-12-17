
package jcog.signal.wave2d.vectorize;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import static jcog.signal.wave2d.vectorize.SVGUtils.svgpathstring;
import static jcog.signal.wave2d.vectorize.SVGUtils.tosvgcolorstr;


public class ImageTracer {

    static final String versionnumber = "1.1.3";

    public ImageTracer() {
    }


    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.out.println("ERROR: there's no input filename. Basic usage: \r\n\r\njava -jar ImageTracer.jar <filename>" +
                        "\r\n\r\nor\r\n\r\njava -jar ImageTracer.jar help");


                
                


            } else if (arraycontains(args, "help") > -1) {
                System.out.println("Example usage:\r\n\r\njava -jar ImageTracer.jar <filename> outfilename test.svg " +
                        "ltres 1 qtres 1 pathomit 1 numberofcolors 128 colorquantcycles 15 " +
                        "scale 1 roundcoords 1 lcpr 0 qcpr 0 desc 1 viewbox 0  blurradius 0 blurdelta 20 \r\n" +
                        "\r\nOnly <filename> is mandatory, if some of the other optional parameters are missing, they will be setAt to these defaults. " +
                        "\r\nWarning: if outfilename is not specified, then <filename>.svg will be overwritten." +
                        "\r\nSee https://github.com/jankovicsandras/imagetracerjava for details. \r\nThis is version " + versionnumber);
            } else {

                
                String outfilename = args[0] + ".svg";
                HashMap<String, Float> options = new HashMap<>();
                String[] parameternames = {
                        "ltres", "qtres", "pathomit", "numberofcolors", "colorquantcycles", "scale", "roundcoords", "lcpr", "qcpr", "desc", "viewbox", "outfilename", "blurammount"};
                int j = -1;
                float f = -1;
                for (String parametername : parameternames) {
                    j = arraycontains(args, parametername);
                    if (j > -1) {
                        if (parametername == "outfilename") {
                            if (j < (args.length - 1)) {
                                outfilename = args[j + 1];
                            }
                        } else {
                            f = parsenext(args, j);
                            if (f > -1) {
                                options.put(parametername, f);
                            }
                        }
                    }
                }

                
                File file = new File(outfilename);
                
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(imageToSVG(args[0], options));
                bw.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static int arraycontains(String[] arr, String str) {
        for (int j = 0; j < arr.length; j++) {
            if (arr[j].toLowerCase().equals(str)) {
                return j;
            }
        }
        return -1;
    }


    private static float parsenext(String[] arr, int i) {
        if (i < (arr.length - 1)) {
            try {
                return Float.parseFloat(arr[i + 1]);
            } catch (Exception e) {
            }
        }
        //return -1;
        return Float.NaN;
    }


    
    public static class IndexedImage {
        public final int width;
        public final int height;
        public final int[][] array; 
        public final byte[][] palette;
        public ArrayList<ArrayList<ArrayList<Double[]>>> layers;

        public IndexedImage(int[][] marray, byte[][] mpalette) {
            array = marray;
            palette = mpalette;
            width = marray[0].length - 2;
            height = marray.length - 2;
        }

        protected void render(HashMap<String, Float> options, float w, float h, BiConsumer<ArrayList<Double[]>, byte[]> path) {
            
            TreeMap<Double, int[]> zindex = new TreeMap<>();


            for (int k = 0; k < this.layers.size(); k++) {

                
                ArrayList<ArrayList<Double[]>> lk = this.layers.get(k);

                for (int pcnt = 0; pcnt < lk.size(); pcnt++) {

                    
                    Double[] lkp0 = lk.get(pcnt).get(0);

                    double label = (lkp0[2] * w) + lkp0[1];
                    int finalPcnt = pcnt;
                    int finalK = k;
                    
                    zindex.computeIfAbsent(label, (l)-> new int[] {finalK, finalPcnt} );
                }

            }

            

            
            
            for (Map.Entry<Double, int[]> entry : zindex.entrySet()) {
                int[] v = entry.getValue();
                path.accept(this.layers.get(v[0]).get(v[1]), this.palette[v[0]]);
            }

        }

        
        
        public String toSVG(HashMap<String, Float> options) {
            
            float w = (int) (this.width * options.get("scale")), h = (int) (this.height * options.get("scale"));

            String viewboxorviewport = options.get("viewbox") != 0 ? "viewBox=\"0 0 " + w + ' ' + h + "\" " : "width=\"" + w + "\" height=\"" + h + "\" ";
            StringBuilder svgstr = new StringBuilder("<svg " + viewboxorviewport + "version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" ");
            if (options.get("desc") != 0) {
                svgstr.append("desc=\"Created with ImageTracer.java version ").append(ImageTracer.versionnumber).append("\" ");
            }
            svgstr.append('>');

            render(options, w, h, (path, color)-> svgpathstring(svgstr,
                    "",
                    path,
                    tosvgcolorstr(color),
                    options));



            
            svgstr.append("</svg>");

            return svgstr.toString();

        }
    }


    
    
    
    private static byte bytetrans(byte b) {
		return (byte) (b < 0 ? b + 128 : b - 128);
    }

    private static byte[][] getPalette(BufferedImage image, Map<String, Float> options) {
        int numberofcolors = options.get("numberofcolors").intValue();
        int[][] pixels = new int[image.getWidth()][image.getHeight()];

        for (int i = 0; i < image.getWidth(); i++)
            for (int j = 0; j < image.getHeight(); j++) {
                pixels[i][j] = image.getRGB(i, j);
            }
        int[] palette = Quantize.quantizeImage(pixels, numberofcolors);
        byte[][] bytepalette = new byte[numberofcolors][4];

        for (int i = 0; i < palette.length; i++) {
            //TODO use Bitmap2D decode... functions rather than new Color
            Color c = new Color(palette[i]);
            bytepalette[i][0] = (byte) c.getRed();
            bytepalette[i][1] = (byte) c.getGreen();
            bytepalette[i][2] = (byte) c.getBlue();
            bytepalette[i][3] = 0;
        }
        return bytepalette;
    }

    
    
    
    
    

    
    private static String imageToSVG(String filename, HashMap<String, Float> options) throws IOException {


        

        HashMap<String, Float> o = checkoptions(options);

        return new ImageData(ImageIO.read(new File(filename)))
                .trace(o)
                .toSVG(o);
    }

















    
    private static HashMap<String, Float> checkoptions(HashMap<String, Float> options) {
        if (options == null) {
            options = new HashMap<>();
        }
        
        if (!options.containsKey("ltres")) {
            options.put("ltres", 10.0f);
        }
        if (!options.containsKey("qtres")) {
            options.put("qtres", 10.0f);
        }
        if (!options.containsKey("pathomit")) {
            options.put("pathomit", 1.0f);
        }
        
        if (!options.containsKey("numberofcolors")) {
            options.put("numberofcolors", 128.0f);
        }
        if (!options.containsKey("colorquantcycles")) {
            options.put("colorquantcycles", 15.0f);
        }
        
        if (!options.containsKey("scale")) {
            options.put("scale", 1.0f);
        }
        if (!options.containsKey("roundcoords")) {
            options.put("roundcoords", 1.0f);
        }
        if (!options.containsKey("lcpr")) {
            options.put("lcpr", 0.0f);
        }
        if (!options.containsKey("qcpr")) {
            options.put("qcpr", 0.0f);
        }
        if (!options.containsKey("desc")) {
            options.put("desc", 1.0f);
        }
        if (!options.containsKey("viewbox")) {
            options.put("viewbox", 0.0f);
        }
        
        if (!options.containsKey("blurradius")) {
            options.put("blurradius", 5.0f);
        }
        if (!options.containsKey("blurdelta")) {
            options.put("blurdelta", 50.0f);
        }

        return options;
    }


    
    public static class ImageData {
        public final int width;
        public final int height;
        public final byte[] data; 
        @Deprecated private final BufferedImage image;

        public ImageData(int mwidth, int mheight, byte[] mdata) {
            width = mwidth;
            height = mheight;
            data = mdata;
            this.image = null;
        }

        public ImageData(BufferedImage image) {
            this.image = image;
            this.width = image.getWidth();
            this.height = image.getHeight();
            int[] rawdata = image.getRGB(0, 0, width, height, null, 0, width);
            byte[] data = new byte[rawdata.length * 4];
            for (int i = 0; i < rawdata.length; i++) {
                int r = rawdata[i];
                data[(i * 4) + 3] = bytetrans((byte) (r >>> 24));
                data[(i * 4)] = bytetrans((byte) (r >>> 16));
                data[(i * 4) + 1] = bytetrans((byte) (r >>> 8));
                data[(i * 4) + 2] = bytetrans((byte) r);
            }
            this.data = data;

        }

        public IndexedImage trace(HashMap<String, Float> options) {

            byte[][] palette = getPalette(image, options);

            
            IndexedImage ii = VectorizingUtils.colorquantization(this, palette, options);
            
            int[][][] rawlayers = VectorizingUtils.layering(ii);
            
            ArrayList<ArrayList<ArrayList<Integer[]>>> bps = VectorizingUtils.batchpathscan(rawlayers, (int) (Math.floor(options.get("pathomit"))));
            
            ArrayList<ArrayList<ArrayList<Double[]>>> bis = VectorizingUtils.batchinternodes(bps);
            
            ii.layers = VectorizingUtils.batchtracelayers(bis, options.get("ltres"), options.get("qtres"));
            return ii;

        }
    }
}