package jcog.markov;


import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.File;
import java.io.IOException;

public class MarkovMidiTest {
//	public static final byte[] notes = {0x3C, 0x3E, 0x40, 0x41, 0x43};
//
//	private static void makeSong(String filename)
//		throws InvalidMidiDataException, MidiUnavailableException, IOException {
//		Sequence s = new Sequence(Sequence.PPQ, 96);
//		Track t = s.createTrack();
//		Receiver rcr = MidiSystem.getReceiver();
//
//		long ticker = 200;
//
//		for (int i = 0; i < notes.length; i++) {
//			ShortMessage playMsg = new ShortMessage();
//			ShortMessage stopMsg = new ShortMessage();
//			playMsg.setMessage(ShortMessage.NOTE_ON, 0, notes[i], 0x40);
//			stopMsg.setMessage(ShortMessage.NOTE_OFF, 0, notes[i], 0x64);
//
//			rcr.send(playMsg, i * ticker);
//			rcr.send(stopMsg, (i + 1) * ticker);
//
//			t.add(new MidiEvent(playMsg, i * ticker));
//			t.add(new MidiEvent(stopMsg, (i + 1) * ticker));
//		}
//
//		MidiSystem.write(s, 1, new File(filename));
//	}

	public static void main(String[] args) throws InvalidMidiDataException, IOException {
		String other = "/tmp/x.mid";
		File f = new File(other);

		Sequence seq = MidiSystem.getSequence(f);


//		Track[] tracks = seq.getTracks();

//		System.out.println("Tracks: " + tracks.length);

		//for (int i = 30; i <= 35; i++) {
		MarkovMIDI track = new MarkovMIDI();
		track.learnSequence(seq);
		//track.learnTrack(tracks[1]);
		System.out.print("Writing /tmp/output.mid...\n");

		track.exportTrack("/tmp/output.mid", f, 256 * 1024);
		//}

	}
}
