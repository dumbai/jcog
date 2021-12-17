package jcog.markov;

import jcog.data.list.Lst;
import jcog.random.XoRoShiRo128PlusRandom;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class MarkovMIDI extends MarkovSampler<MarkovMIDI.MidiMessageWrapper> {

    private final MarkovChain<Long> chain;

    public MarkovMIDI() {
        super(new MarkovChain<>(), new XoRoShiRo128PlusRandom());
        chain = new MarkovChain<>();
    }

//    public void importMIDI(File file) throws InvalidMidiDataException, IOException {
//        Sequence s = MidiSystem.getSequence(file);
//        learnSequence(s, MidiSystem.getMidiFileFormat(file));
//    }

    public void learnSequence(Sequence s) {
        for (Track track : s.getTracks())
            learnTrack(track, 16);
    }

    public void learnTrack(Track t, int arity) {
        int trackSize = t.size();
        if (trackSize == 0) return;

        Collection<Long> times = new Lst<>();
        long lastTick = t.get(0).getTick();
        times.add(lastTick);

//        int resolution = fmt.getResolution();
//        long beats = event.getTick() / resolution;
//        int adds = 0;

        //TODO
        //filter any long pauses (more than x% of the entire length)
        //double maxPause =

        Collection<MidiMessageWrapper> msgs = new Lst<>();
        for (int i = 1; i < trackSize; i++) {
            MidiEvent event = t.get(i);
            long eTick = event.getTick();
            long delta = eTick - lastTick;
            times.add(delta);
            lastTick = eTick;

            msgs.add(new MidiMessageWrapper(event.getMessage()));
        }

        if (!msgs.isEmpty())
            model.learn(arity, msgs);

        if (!times.isEmpty())
            chain.learn(arity, times);
    }

    public void exportTrack(String filename, float divisionType, int resolution, int fileType)
            throws InvalidMidiDataException, IOException {
        exportTrack(new File(filename), divisionType, resolution, fileType, 0);
    }

    public void exportTrack(String filename, float divisionType, int resolution, int fileType, int maxLength)
            throws InvalidMidiDataException, IOException {
        exportTrack(new File(filename), divisionType, resolution, fileType, maxLength);
    }

    public void exportTrack(File file, float divisionType, int resolution, int fileType, int maxLength)
            throws InvalidMidiDataException, IOException {

        int tracks = 16;
        int length = 1024;

        Sequence s = new Sequence(divisionType, resolution, tracks);

        for (int i = 0; i < tracks; i++) {


            Track t = s.createTrack();

			MarkovSampler<Long> lengths = (MarkovSampler<Long>) new MarkovSampler(chain);

            MidiMessageWrapper m;
            int ticks = 0;
            int n = 0;

            reset();
            while ((m = nextLoop()) != null && n++ < length) {

                ticks += (long) lengths.nextLoop();

                if (!t.add(new MidiEvent(m.getMessage(), ticks))) {

                }
            }
        }

        MidiSystem.write(s, fileType, file);

    }

    public void exportTrack(String s, File inputFile, int maxLen) throws InvalidMidiDataException, IOException {
        MidiFileFormat fmt = MidiSystem.getMidiFileFormat(inputFile);
        exportTrack(s, fmt.getDivisionType(), fmt.getResolution(), fmt.getType(), maxLen);
    }

    public static class MidiMessageWrapper implements Comparable<MidiMessageWrapper> {
        private final MidiMessage mMessage;

        public MidiMessageWrapper(MidiMessage msg) {
            mMessage = msg;
        }

        public MidiMessage getMessage() {
            return mMessage;
        }

        public int hashCode() {
            if (mMessage == null || mMessage.getLength() == 0) return 0;
            String str = toString();
            return str.hashCode();
        }

        @Override
        public int compareTo(MidiMessageWrapper other) {
            byte[] mymsg = mMessage.getMessage();
            byte[] theirmsg = mMessage.getMessage();

            return Arrays.compare(mymsg,theirmsg);
//
//            for (int i = 0; i < mymsg.length && i < theirmsg.length; i++) {
//                if (mymsg[i] > theirmsg[i]) {
//                    return 1;
//                } else if (theirmsg[i] > mymsg[i]) return -1;
//            }
//
//            return Integer.compare(mymsg.length, theirmsg.length);
        }

        public boolean equals(Object o) {
            try {
                if (this == o) return true;
                MidiMessageWrapper other = (MidiMessageWrapper) o;
                byte[] mine = mMessage.getMessage();
                byte[] theirs = other.getMessage().getMessage();
                return Arrays.equals(mine, theirs);

            } catch (Exception e) {
                return false;
            }
        }

        public String toString() {
            String out = "";
            for (int i = 0; i < mMessage.getLength(); i++) {
                out += String.format("%x", mMessage.getMessage()[i]);
                if (i < mMessage.getLength() - 1) out += " ";
            }
            return out;
        }

    }
}
