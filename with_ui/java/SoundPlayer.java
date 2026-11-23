import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

// Plays the game sounds through the computer speakers.
// The sounds are made in code (no sound files needed).
// Each sound plays on its own thread so the game never waits for it.
public class SoundPlayer {

	private final float RATE = 44100;   // samples per second

	public void tick() {             // countdown tick
		play(new int[] {700}, 60);
	}

	public void draw() {             // the DRAW! signal
		play(new int[] {1400}, 500);
	}

	public void shot() {             // gunshot when a hit counts
		play(new int[] {-1}, 350);
	}

	public void foul() {             // low buzz
		play(new int[] {150}, 700);
	}

	public void win() {              // short tune for a round win
		play(new int[] {523, 659, 784}, 150);
	}

	public void tie() {
		play(new int[] {400, 400}, 200);
	}

	public void champion() {         // longer tune for the match winner
		play(new int[] {523, 659, 784, 1047, 784, 1047}, 180);
	}

	// plays the notes one after another on a new thread
	// a note of -1 is a gunshot noise instead of a tone
	private void play(final int[] notes, final int ms) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				for (int i = 0; i < notes.length; i++) {
					playNote(notes[i], ms);
				}
			}
		});
		t.start();
	}

	private void playNote(int freq, int ms) {
		try {
			byte[] sound = new byte[(int) (RATE * ms / 1000)];
			Random random = new Random();

			for (int i = 0; i < sound.length; i++) {
				double fade = 1.0 - (double) i / sound.length;   // gets quieter at the end
				if (freq == -1) {
					sound[i] = (byte) ((random.nextInt(200) - 100) * fade);
				} else {
					sound[i] = (byte) (Math.sin(2 * Math.PI * freq * i / RATE) * 90 * fade);
				}
			}

			AudioFormat format = new AudioFormat(RATE, 8, 1, true, false);
			SourceDataLine line = AudioSystem.getSourceDataLine(format);
			line.open(format);
			line.start();
			line.write(sound, 0, sound.length);
			line.drain();
			line.close();
		} catch (Exception e) {
			// no speakers available: the game still works without sound
		}
	}
}
