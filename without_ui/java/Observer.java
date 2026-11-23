// Observer interface (Observer design pattern).
// Any class that wants the sensor values from the guns implements this.
public interface Observer {
	void update(int playerId, int holster, int trigger, int hit, long time);
}
